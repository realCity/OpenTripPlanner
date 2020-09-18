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

class IntermediatePlacesDepartAtCalculator implements IntermediatePlacesCalculator {

    private final RoutingRequest request;

    private final List<GenericLocationWrapper> locations;

    private final List<ItinerarySequence> itinerarySequences;

    private int locationIndex;

    IntermediatePlacesDepartAtCalculator(RoutingRequest request) {
        this.request = request;
        this.locations = IntermediatePlacesCalculator.collectLocationsFromRequest(request);
        this.locationIndex = 0;
        this.itinerarySequences = new ArrayList<>();
    }

    @Override public void nextLocation() {
        locationIndex++;
    }

    @Override public boolean hasNextLocation() {
        return locationIndex + 1 < locations.size();
    }

    @Override public GenericLocationWrapper getCurrentFrom() {
        return locations.get(locationIndex - 1);
    }

    @Override public GenericLocationWrapper getCurrentTo() {
        return locations.get(locationIndex);
    }

    @Override public GenericLocationWrapper getCurrentMergeEnd() {
        return getCurrentFrom();
    }

    @Override public List<ItinerarySequence> getItinerarySequences() {
        return itinerarySequences;
    }

    @Override public Function<Itinerary, ItinerarySequence> itinerarySequenceProvider() {
        return IntermediatePlacesDepartAtCalculator.DepartAtItinerarySequence::new;
    }

    @Override public Collection<LocationAndSearchWindow> collectLocationAndSearchWindowsForNextRequest() {
        var location = getCurrentFrom();

        // Initial iteration
        if (locationIndex == 1) {
            LocationAndSearchWindow locationAndSearchWindow = new LocationAndSearchWindow(location.location());
            return List.of(locationAndSearchWindow);
        }

        Duration minWaitTime = location.getMinWaitTime();

        TreeMap<Place, LocationAndSearchWindow> laswByPlace = new TreeMap<>(PLACE_COMPARATOR);
        for (ItinerarySequence itinerarySequence : itinerarySequences) {
            Place finalPlace = itinerarySequence.getFinalPlace();
            Optional<Place> foundPlace = laswByPlace.keySet().stream()
                    .filter(sp -> sp.sameLocation(finalPlace)).findFirst();

            Instant endTime = itinerarySequence.getEndTime();
            LocationAndSearchWindow lasw;

            if (foundPlace.isEmpty()) {
                IntermediateGenericLocation placeLocation = IntermediatePlacesCalculator
                        .createIntermediateGenericLocation(location, finalPlace);
                lasw = new LocationAndSearchWindow(placeLocation);
                laswByPlace.put(finalPlace, lasw);
            } else {
                lasw = laswByPlace.get(foundPlace.get());
            }

            lasw.extendToInclude(endTime.plus(minWaitTime));
        }

        return laswByPlace.values();
    }

    @Override public RoutingRequest createRequestFromLocationAndSearchWindow(Router router,
            LocationAndSearchWindow locationAndSearchWindow) {
        var currentTo = getCurrentTo().location();

        if (locationAndSearchWindow.isUnbounded()) {
            return IntermediatePlacesCalculator.createIntermediateRequest(router, request,
                    locationAndSearchWindow.requestLocation(), currentTo, request.dateTime,
                    request.searchWindow);
        }

        return IntermediatePlacesCalculator.createIntermediateRequest(router, request,
                locationAndSearchWindow.requestLocation(), currentTo,
                locationAndSearchWindow.requestDepartAtTime(),
                locationAndSearchWindow.requestSearchWindow());
    }

    private static abstract class AbstractDepartAtItinerarySequence implements ItinerarySequence {

        @Override public Optional<ItinerarySequence> extendWith(Itinerary nextItinerary,
                Duration minWaitTime, Duration maxWaitTime) {

            Duration newWaitTime;
            Instant newStartTime;
            Instant newEndTime;

            if (!nextItinerary.firstLeg().from.sameLocation(getFinalPlace())) {
                return Optional.empty();
            }

            if (nextItinerary.streetOnly) {
                newWaitTime = minWaitTime;
                newStartTime = getStartTime();
                newEndTime = getEndTime().plus(newWaitTime)
                        .plusSeconds(nextItinerary.durationSeconds);
            } else {
                if (isStreetOnly()) {
                    Duration existingDuration = Duration.between(getStartTime(), getEndTime());
                    newWaitTime = minWaitTime;
                    newStartTime = nextItinerary.firstLeg().startTime.toInstant().minus(newWaitTime)
                            .minus(existingDuration);
                    newEndTime = nextItinerary.lastLeg().endTime.toInstant();
                } else {
                    Instant minStartTime = getEndTime().plus(minWaitTime);
                    Instant maxStartTime = getEndTime().plus(maxWaitTime);
                    Instant nextItineraryStartTime = nextItinerary.firstLeg().startTime.toInstant();

                    if (nextItineraryStartTime.isBefore(minStartTime) || nextItineraryStartTime
                            .isAfter(maxStartTime)) {
                        return Optional.empty();
                    }

                    newWaitTime = Duration.ofSeconds(0);
                    newStartTime = getStartTime();
                    newEndTime = nextItinerary.lastLeg().endTime.toInstant();
                }
            }

            return Optional
                    .of(new DepartAtExtendedItinerarySequence(this, nextItinerary, newStartTime,
                            newEndTime, newWaitTime));
        }
    }

    private static class DepartAtItinerarySequence extends AbstractDepartAtItinerarySequence {

        private final Itinerary itinerary;

        public DepartAtItinerarySequence(Itinerary itinerary) {
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

    private static class DepartAtExtendedItinerarySequence
            extends AbstractDepartAtItinerarySequence {

        private final ItinerarySequence parent;

        private final Itinerary itinerary;

        private final Instant startTime;

        private final Instant endTime;

        private final Duration waitTime;

        DepartAtExtendedItinerarySequence(ItinerarySequence parent, Itinerary itinerary,
                Instant startTime, Instant endTime, Duration waitTime) {
            this.parent = parent;
            this.itinerary = itinerary;
            this.startTime = startTime;
            this.endTime = endTime;
            this.waitTime = waitTime;
        }

        @Override public Place getInitialPlace() {
            return parent.getInitialPlace();
        }

        @Override public Place getFinalPlace() {
            return itinerary.lastLeg().to;
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
            itineraries.add(itinerary);
            return itineraries;
        }

        @Override public List<Duration> getWaitTimes() {
            List<Duration> waitTimes = parent.getWaitTimes();
            waitTimes.add(waitTime);
            return waitTimes;
        }

        @Override public Itinerary asItinerary() {
            List<Leg> legs = new ArrayList<>();
            List<Duration> waitTimes = getWaitTimes();
            List<Itinerary> itineraries = getItineraries();

            long previousEnd = 0;
            boolean streetOnlyStart = true;
            for (int i = 0; i < itineraries.size(); i++) {
                Itinerary itinerary = itineraries.get(i);

                int currentLegOffset = 0;
                int existingLegOffset = 0;

                if (i > 0) {
                    long offset = previousEnd - itinerary.firstLeg().startTime.getTimeInMillis()
                            + waitTimes.get(i - 1).getSeconds() * 1000;

                    if (streetOnlyStart && !itinerary.streetOnly) {
                        existingLegOffset = -1 * (int) offset;
                    }

                    if (itinerary.streetOnly) {
                        currentLegOffset = (int) offset;
                    }
                }

                if (existingLegOffset != 0) {
                    for (Leg leg : legs) {
                        leg.timeShift(existingLegOffset);
                    }
                }

                List<Leg> legList = itinerary.legs;
                for (Leg leg : legList) {
                    Leg copy = new Leg(leg);
                    copy.timeShift(currentLegOffset);
                    legs.add(copy);

                    previousEnd = copy.endTime.getTimeInMillis();
                }

                streetOnlyStart = streetOnlyStart && itinerary.streetOnly;
            }

            return ItinerarySequence.mergeItineraryWithLegs(itineraries, legs);
        }
    }
}
