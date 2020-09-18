package org.opentripplanner.routing.algorithm.intermediateplaces;

import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.model.plan.Leg;
import org.opentripplanner.model.plan.Place;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ItinerarySequence {

    Instant getStartTime();

    Instant getEndTime();

    Place getInitialPlace();

    Place getFinalPlace();

    boolean isStreetOnly();

    List<Itinerary> getItineraries();

    List<Duration> getWaitTimes();

    Optional<ItinerarySequence> extendWith(Itinerary itinerary, Duration minWaitTime,
            Duration maxWaitTime);

    Itinerary asItinerary();

    static Itinerary mergeItineraryWithLegs(List<Itinerary> itineraries, List<Leg> legs) {
        Itinerary mergedItinerary = new Itinerary(legs);

        for (Itinerary itinerary : itineraries) {
            mergedItinerary.nonTransitLimitExceeded =
                    mergedItinerary.nonTransitLimitExceeded || itinerary.nonTransitLimitExceeded;
            mergedItinerary.elevationLost = mergedItinerary.elevationLost + itinerary.elevationLost;
            mergedItinerary.elevationGained =
                    mergedItinerary.elevationGained + itinerary.elevationGained;
            mergedItinerary.generalizedCost =
                    mergedItinerary.generalizedCost + itinerary.generalizedCost;
            mergedItinerary.tooSloped = mergedItinerary.tooSloped || itinerary.tooSloped;
            // mergedItinerary.fare = null; TODO: handle fares
        }

        return mergedItinerary;
    }
}
