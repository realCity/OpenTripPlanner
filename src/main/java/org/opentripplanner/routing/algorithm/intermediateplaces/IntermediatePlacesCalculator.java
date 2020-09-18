package org.opentripplanner.routing.algorithm.intermediateplaces;

import com.google.common.collect.Lists;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.GenericLocation;
import org.opentripplanner.model.IntermediateGenericLocation;
import org.opentripplanner.model.WgsCoordinate;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.model.plan.Place;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.standalone.server.Router;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public interface IntermediatePlacesCalculator {

    boolean hasNextLocation();

    void nextLocation();

    GenericLocationWrapper getCurrentFrom();

    GenericLocationWrapper getCurrentTo();

    GenericLocationWrapper getCurrentMergeEnd();

    List<ItinerarySequence> getItinerarySequences();

    Function<Itinerary, ItinerarySequence> itinerarySequenceProvider();

    Collection<LocationAndSearchWindow> collectLocationAndSearchWindowsForNextRequest();

    RoutingRequest createRequestFromLocationAndSearchWindow(Router router,
            LocationAndSearchWindow locationAndSearchWindow);

    default boolean hasItinerarySequences() {
        return !getItinerarySequences().isEmpty();
    }

    default List<Itinerary> getItinerarySequencesAsItineraries() {
        return getItinerarySequences().stream().map(ItinerarySequence::asItinerary).collect(toList());
    }

    default List<RoutingRequest> prepareNextRequests(Router router) {
        nextLocation();

        return collectLocationAndSearchWindowsForNextRequest().stream()
                .map(locationAndSearchWindow -> createRequestFromLocationAndSearchWindow(router,
                        locationAndSearchWindow)).collect(toList());
    }

    default void mergeInItineraries(List<Itinerary> intermediateItineraries) {
        List<ItinerarySequence> itinerarySequences = getItinerarySequences();

        if (itinerarySequences.isEmpty()) {
            itinerarySequences
                    .addAll(intermediateItineraries.stream().map(itinerarySequenceProvider())
                            .collect(toList()));
        } else {
            GenericLocationWrapper from = getCurrentMergeEnd();
            Duration minWaitTime = from.getMinWaitTime();
            Duration maxWaitTime = from.getMaxWaitTime();

            List<ItinerarySequence> newSequences = new ArrayList<>();
            for (ItinerarySequence itinerarySequence : itinerarySequences) {
                for (Itinerary intermediateItinerary : intermediateItineraries) {
                    itinerarySequence.extendWith(intermediateItinerary, minWaitTime, maxWaitTime)
                            .ifPresent(newSequences::add);
                }
            }

            itinerarySequences.clear();
            itinerarySequences.addAll(newSequences);
        }
    }

    static List<GenericLocationWrapper> collectLocationsFromRequest(RoutingRequest request) {
        List<GenericLocationWrapper> places = Lists
                .newArrayList(GenericLocationWrapper.of(request.from));
        places.addAll(request.intermediatePlaces.stream()
                .map(place -> createIntermediateGenericLocation(place, request))
                .map(GenericLocationWrapper::of).collect(toList()));
        places.add(GenericLocationWrapper.of(request.to));
        return places;
    }

    static IntermediateGenericLocation createIntermediateGenericLocation(
            GenericLocationWrapper location, Place place) {
        return new IntermediateGenericLocation(place.name, place.stopId,
                place.coordinate.latitude(), place.coordinate.longitude(),
                location.getMinWaitTime(), location.getMaxWaitTime());
    }

    static IntermediateGenericLocation createIntermediateGenericLocation(GenericLocation location,
            RoutingRequest request) {

        if (location instanceof IntermediateGenericLocation) {
            return (IntermediateGenericLocation) location;
        }

        return new IntermediateGenericLocation(location.label, location.stopId, location.lat,
                location.lng, Duration.ofSeconds(request.transferSlack));
    }

    static RoutingRequest createIntermediateRequest(Router router, RoutingRequest request,
            GenericLocation from, GenericLocation to, long time, Duration searchWindow) {
        RoutingRequest intermediateRequest = request.clone();
        intermediateRequest.clearIntermediatePlaces();
        intermediateRequest.dateTime = time;
        intermediateRequest.searchWindow = searchWindow;
        intermediateRequest.from = from;
        intermediateRequest.to = to;
        intermediateRequest.rctx = null;
        intermediateRequest.numItineraries = Integer.MAX_VALUE;
        intermediateRequest.setRoutingContext(router.graph);
        return intermediateRequest;
    }

    static IntermediatePlacesCalculator formRequest(RoutingRequest request) {
        return request.arriveBy ?
                new IntermediatePlacesArriveByCalculator(request) :
                new IntermediatePlacesDepartAtCalculator(request);
    }

    Comparator<Place> PLACE_COMPARATOR = Comparator.<Place, FeedScopedId>comparing(
            place -> place.stopId, Comparator.nullsLast(
                    Comparator.comparing(FeedScopedId::getFeedId)
                            .thenComparing(FeedScopedId::getId)))
            .thenComparing(place -> place.coordinate, Comparator.nullsLast(
                    Comparator.comparingDouble(WgsCoordinate::longitude)
                            .thenComparing(WgsCoordinate::latitude)));
}
