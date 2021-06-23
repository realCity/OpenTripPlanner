package org.opentripplanner.routing.algorithm.filterchain.filters;

import org.junit.jupiter.api.Test;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.model.plan.PlanTestConstants;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opentripplanner.model.plan.TestItineraryBuilder.newItinerary;

class RemoveBikeParkWithShortBikingFilterTest implements PlanTestConstants {

  private final RemoveBikeParkWithShortBikingFilter
          filter = new RemoveBikeParkWithShortBikingFilter(500.0);

  @Test
  public void removeItinerariesWithShortBikingDistanceTest() {
    Itinerary i1 = newItinerary(A, T11_06)
        .bicycle(T11_06, T11_25, B)
        .bus(31, T11_25, T11_27, C)
        .bicycle(T11_27, T11_30, D)
        .build();
    i1.firstLeg().distanceMeters = 100.0;
    i1.legs.get(1).distanceMeters = 1000.0;
    i1.lastLeg().distanceMeters = 1000.0;

    var itineraries = List.of(i1);
    var filteredItineraries = filter.filter(itineraries);
    assertEquals(List.of(), filteredItineraries);
  }

  @Test
  public void dontRemoveItinerariesWithLongBikingDistanceTest() {
    Itinerary i1 = newItinerary(A, T11_06)
        .bicycle(T11_06, T11_25, B)
        .bus(31, T11_25, T11_27, C)
        .build();
    i1.firstLeg().distanceMeters = 600.0;

    var itineraries = List.of(i1);
    var filteredItineraries = filter.filter(itineraries);
    assertEquals(itineraries, filteredItineraries);
  }

  @Test
  public void summingBicycleLegsTest() {
    Itinerary i1 = newItinerary(A, T11_06)
        .bicycle(T11_06, T11_25, B)
        .walk(2, C)
        .bicycle(T11_10, T11_23, D)
        .bus(31, T11_25, T11_27, E)
        .build();
    i1.firstLeg().distanceMeters = 300.0;
    i1.legs.get(2).distanceMeters = 300.0;

    var itineraries = List.of(i1);
    var filteredItineraries = filter.filter(itineraries);
    assertEquals(itineraries, filteredItineraries);
  }
}
