package org.opentripplanner.routing.algorithm.raptor.router;

import static org.junit.Assert.assertEquals;
import static org.opentripplanner.model.plan.PlanTestConstants.A;
import static org.opentripplanner.model.plan.PlanTestConstants.D10m;
import static org.opentripplanner.model.plan.PlanTestConstants.E;
import static org.opentripplanner.model.plan.PlanTestConstants.T11_06;
import static org.opentripplanner.model.plan.TestItineraryBuilder.newItinerary;

import java.util.List;
import org.junit.Test;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.api.request.RequestModes;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.api.request.StreetMode;

public class PruningItineraryHandlerTest {

  @Test
  public void directModeEqualsPruningMode() {
    var modes = new RequestModes(null, null, null, StreetMode.BIKE, null);

    var subject = new PruningItineraryHandler(StreetMode.BIKE, modes);
    subject.route(null, null, null);

    assertEquals(List.of(), subject.pruningItineraries());
  }

  @Test
  public void pruningModeIsEmpty() {
    var modes = new RequestModes(null, null, null, StreetMode.BIKE, null);

    var subject = new PruningItineraryHandler(null, modes);
    subject.route(null, null, null);

    assertEquals(List.of(), subject.pruningItineraries());
  }

  @Test
  public void directModeIsEmpty() {
    var modes = new RequestModes(null, null, null, null, null);

    var subject = new PruningItineraryHandler(StreetMode.BIKE, modes);
    subject.route(null, null, null);

    assertEquals(List.of(), subject.pruningItineraries());
  }

  @Test
  public void directModeAndPruningModeDiffers() {
    Itinerary walk = newItinerary(A, T11_06).walk(D10m, E).build();

    var modes = new RequestModes(null, null, null, StreetMode.BIKE, null);

    var subject = new PruningItineraryHandler(StreetMode.WALK, modes);

    assertEquals(List.of(walk), subject.route(null, new RoutingRequest(modes), (a, b) -> List.of(walk)));
    assertEquals(List.of(walk), subject.pruningItineraries());
  }
}