package org.opentripplanner.routing.algorithm.intermediateplaces;

import com.google.common.collect.Lists;
import org.opentripplanner.model.IntermediateGenericLocation;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.model.plan.Leg;
import org.opentripplanner.model.plan.Place;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.standalone.server.Router;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

class IntermediatePlacesArriveByCalculator implements IntermediatePlacesCalculator {

    private final RoutingRequest request;

    private final List<GenericLocationWrapper> locations;

    private final List<ItinerarySequence> itinerarySequences;

    private int locationIndex;

    IntermediatePlacesArriveByCalculator(RoutingRequest request) {
        this.request = request;
        this.locations = IntermediatePlacesCalculator.collectLocationsFromRequest(request);
        this.locationIndex = locations.size();
        this.itinerarySequences = new ArrayList<>();
    }

    @Override public void nextLocation() {
        locationIndex--;
    }

    @Override public boolean hasNextLocation() {
        return locationIndex > 1;
    }

    @Override public GenericLocationWrapper getCurrentFrom() {
        return locations.get(locationIndex - 1);
    }

    @Override public GenericLocationWrapper getCurrentTo() {
        return locations.get(locationIndex);
    }

    @Override public GenericLocationWrapper getCurrentMergeEnd() {
        return getCurrentTo();
    }

    @Override public List<ItinerarySequence> getItinerarySequences() {
        return itinerarySequences;
    }

    @Override public Function<Itinerary, ItinerarySequence> itinerarySequenceProvider() {
        return IntermediatePlacesArriveByCalculator.ArriveByItinerarySequence::new;
    }

    @Override public Collection<LocationAndSearchWindow> collectLocationAndSearchWindowsForNextRequest() {
        var location = getCurrentTo();

        // Initial iteration
        if (locationIndex == locations.size() - 1) {
            LocationAndSearchWindow locationAndSearchWindow = new LocationAndSearchWindow(location.location());
            return List.of(locationAndSearchWindow);
        }

        Duration minWaitTime = location.getMinWaitTime();

        TreeMap<Place, LocationAndSearchWindow> laswByPlace = new TreeMap<>(PLACE_COMPARATOR);
        for (ItinerarySequence itinerarySequence : itinerarySequences) {
            Place initialPlace = itinerarySequence.getInitialPlace();
            Optional<Place> foundPlace = laswByPlace.keySet().stream()
                    .filter(sp -> sp.sameLocation(initialPlace)).findFirst();

            Instant startTime = itinerarySequence.getStartTime();
            LocationAndSearchWindow lasw;

            if (foundPlace.isEmpty()) {
                IntermediateGenericLocation placeLocation = IntermediatePlacesCalculator
                        .createIntermediateGenericLocation(location, initialPlace);
                lasw = new LocationAndSearchWindow(placeLocation);
                laswByPlace.put(initialPlace, lasw);
            } else {
                lasw = laswByPlace.get(foundPlace.get());
            }

            lasw.extendToInclude(startTime.minus(minWaitTime));
        }

        return laswByPlace.values();
    }

    @Override public RoutingRequest createRequestFromLocationAndSearchWindow(Router router,
            LocationAndSearchWindow locationAndSearchWindow) {
        var currentFrom = getCurrentFrom().location();

        if (locationAndSearchWindow.isUnbounded()) {
            return IntermediatePlacesCalculator
                    .createIntermediateRequest(router, request, currentFrom,
                            locationAndSearchWindow.requestLocation(), request.dateTime,
                            request.searchWindow);
        }

        return IntermediatePlacesCalculator.createIntermediateRequest(router, request, currentFrom,
                locationAndSearchWindow.requestLocation(),
                locationAndSearchWindow.requestArriveByTime(),
                locationAndSearchWindow.requestSearchWindow());
    }

    private static abstract class AbstractArriveByItinerarySequence implements ItinerarySequence {

        @Override public Optional<ItinerarySequence> extendWith(Itinerary previousItinerary,
                Duration minWaitTime, Duration maxWaitTime) {

            Duration newWaitTime;
            Instant newStartTime;
            Instant newEndTime;

            if (!previousItinerary.lastLeg().to.sameLocation(getInitialPlace())) {
                return Optional.empty();
            }

            if (previousItinerary.streetOnly) {
                newWaitTime = minWaitTime;
                newStartTime = getStartTime().minus(newWaitTime)
                        .minusSeconds(previousItinerary.durationSeconds);
                newEndTime = getEndTime();
            } else {
                if (isStreetOnly()) {
                    Duration existingDuration = Duration.between(getStartTime(), getEndTime());
                    newWaitTime = minWaitTime;
                    newStartTime = previousItinerary.firstLeg().startTime.toInstant();
                    newEndTime = previousItinerary.lastLeg().endTime.toInstant().plus(newWaitTime)
                            .plus(existingDuration);
                } else {
                    // Since durations are subtracted, and minWaitTime is shorter than maxWaitTime,
                    // the values have to be reversed
                    Instant minEndTime = getStartTime().minus(maxWaitTime);
                    Instant maxEndTime = getStartTime().minus(minWaitTime);
                    Instant nextItineraryEndTime = previousItinerary.lastLeg().endTime.toInstant();

                    if (nextItineraryEndTime.isBefore(minEndTime) || nextItineraryEndTime
                            .isAfter(maxEndTime)) {
                        return Optional.empty();
                    }

                    newWaitTime = Duration.ofSeconds(0);
                    newStartTime = previousItinerary.firstLeg().startTime.toInstant();
                    newEndTime = getEndTime();
                }
            }

            return Optional
                    .of(new IntermediatePlacesArriveByCalculator.ArriveByExtendedItinerarySequence(
                            this, previousItinerary, newStartTime, newEndTime, newWaitTime));
        }
    }

    private static class ArriveByItinerarySequence
            extends IntermediatePlacesArriveByCalculator.AbstractArriveByItinerarySequence {

        private final Itinerary itinerary;

        public ArriveByItinerarySequence(Itinerary itinerary) {
            this.itinerary = itinerary;
        }

        @Override public Instant getStartTime() {
            return itinerary.firstLeg().startTime.toInstant();
        }

        @Override public Instant getEndTime() {
            return itinerary.lastLeg().endTime.toInstant();
        }

        @Override public Place getInitialPlace() {
            return itinerary.firstLeg().from;
        }

        @Override public Place getFinalPlace() {
            return itinerary.lastLeg().to;
        }

        @Override public boolean isStreetOnly() {
            return itinerary.streetOnly;
        }

        @Override public List<Itinerary> getItineraries() {
            return Lists.newArrayList(itinerary);
        }

        @Override public List<Duration> getWaitTimes() {
            return Lists.newArrayList();
        }

        @Override public Itinerary asItinerary() {
            return itinerary;
        }
    }

    private static class ArriveByExtendedItinerarySequence
            extends IntermediatePlacesArriveByCalculator.AbstractArriveByItinerarySequence {

        private final ItinerarySequence parent;

        private final Itinerary itinerary;

        private final Instant startTime;

        private final Instant endTime;

        private final Duration waitTime;

        ArriveByExtendedItinerarySequence(ItinerarySequence parent, Itinerary itinerary,
                Instant startTime, Instant endTime, Duration waitTime) {
            this.parent = parent;
            this.itinerary = itinerary;
            this.startTime = startTime;
            this.endTime = endTime;
            this.waitTime = waitTime;
        }

        @Override public Place getInitialPlace() {
            return itinerary.firstLeg().from;
        }

        @Override public Place getFinalPlace() {
            return parent.getFinalPlace();
        }

        @Override public Instant getStartTime() {
            return startTime;
        }

        @Override public Instant getEndTime() {
            return endTime;
        }

        @Override public boolean isStreetOnly() {
            return itinerary.streetOnly && parent.isStreetOnly();
        }

        @Override public List<Itinerary> getItineraries() {
            List<Itinerary> itineraries = parent.getItineraries();
            itineraries.add(0, itinerary);
            return itineraries;
        }

        @Override public List<Duration> getWaitTimes() {
            List<Duration> waitTimes = parent.getWaitTimes();
            waitTimes.add(0, waitTime);
            return waitTimes;
        }

        @Override public Itinerary asItinerary() {
            List<Leg> legs = new ArrayList<>();
            List<Duration> waitTimes = getWaitTimes();
            List<Itinerary> itineraries = getItineraries();

            long previousStart = 0;
            boolean streetOnlyEnd = true;
            for (int i = itineraries.size() - 1; i >= 0; i--) {
                Itinerary itinerary = itineraries.get(i);

                int currentLegOffset = 0;
                int existingLegOffset = 0;

                if (i + 1 < itineraries.size()) {
                    long offset = itinerary.lastLeg().endTime.getTimeInMillis() - previousStart
                            + waitTimes.get(i).getSeconds() * 1000;

                    if (streetOnlyEnd && !itinerary.streetOnly) {
                        existingLegOffset = (int) offset;
                    }

                    if (itinerary.streetOnly) {
                        currentLegOffset = -1 * (int) offset;
                    }
                }

                if (existingLegOffset != 0) {
                    for (Leg leg : legs) {
                        leg.timeShift(existingLegOffset);
                    }
                }

                List<Leg> legList = itinerary.legs;
                for (int j = legList.size() - 1; j >= 0; j--) {
                    Leg leg = legList.get(j);
                    Leg copy = new Leg(leg);
                    copy.timeShift(currentLegOffset);
                    legs.add(0, copy);

                    previousStart = copy.startTime.getTimeInMillis();
                }

                streetOnlyEnd = streetOnlyEnd && itinerary.streetOnly;
            }

            return ItinerarySequence.mergeItineraryWithLegs(itineraries, legs);
        }
    }
}
