package org.opentripplanner.routing.algorithm.filterchain.filters;

import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryFilter;
import org.opentripplanner.routing.core.TraverseMode;

import java.util.List;
import java.util.stream.Collectors;

public class RemoveBikeParkWithShortBikingFilter implements ItineraryFilter {

  private final double minBikeParkingDistance;

  public RemoveBikeParkWithShortBikingFilter(double minBikeParkingDistance) {
    this.minBikeParkingDistance = minBikeParkingDistance;
  }

  @Override
  public String name() {
    return "remove-bike-park-with-short-biking-filter";
  }

  @Override
  public List<Itinerary> filter(List<Itinerary> itineraries) {
    return itineraries.stream()
        .filter(this::filterItinerariesWithShortBikeParkingLeg)
        .collect(Collectors.toList());
  }

  private boolean filterItinerariesWithShortBikeParkingLeg(Itinerary itinerary) {
    double bikeParkingDistance = 0;
    for (var leg : itinerary.legs) {
      if (leg.isTransitLeg()) {
        break;
      }

      if (leg.mode == TraverseMode.BICYCLE) {
        bikeParkingDistance += leg.distanceMeters;
      }
    }

    return bikeParkingDistance > minBikeParkingDistance;
  }

  @Override
  public boolean removeItineraries() {
    return true;
  }
}
