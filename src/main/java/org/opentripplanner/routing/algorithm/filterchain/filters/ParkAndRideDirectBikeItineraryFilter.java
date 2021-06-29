package org.opentripplanner.routing.algorithm.filterchain.filters;

import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryFilter;

import java.util.List;
import java.util.stream.Collectors;

import static org.opentripplanner.routing.core.TraverseMode.BICYCLE;

public class ParkAndRideDirectBikeItineraryFilter implements ItineraryFilter {

  @Override
  public String name() {
    return "park-and-ride-direct-bike-itinerary-filter";
  }

  @Override
  public List<Itinerary> filter(List<Itinerary> itineraries) {
    return itineraries.stream()
        .filter(this::filterBikeOnlyParkAndRideItineraries)
        .collect(Collectors.toList());
  }

  private boolean filterBikeOnlyParkAndRideItineraries(Itinerary itinerary) {
    return !itinerary.legs.stream().allMatch(leg -> leg.mode == BICYCLE || leg.walkingBike);
  }

  @Override
  public boolean removeItineraries() {
    return true;
  }
}
