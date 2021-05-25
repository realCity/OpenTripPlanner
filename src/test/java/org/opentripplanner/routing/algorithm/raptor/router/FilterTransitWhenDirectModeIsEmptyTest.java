package org.opentripplanner.routing.algorithm.raptor.router;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import org.junit.Test;
import org.opentripplanner.routing.api.request.RequestModes;
import org.opentripplanner.routing.api.request.StreetMode;

public class FilterTransitWhenDirectModeIsEmptyTest {

  @Test
  public void directModeIsExistAndIsNotWalking() {
    var modes = new RequestModes(null, null, null, StreetMode.BIKE, null);

    var subject = new FilterTransitWhenDirectModeIsEmpty(modes);

    assertEquals(Set.of(StreetMode.BIKE), subject.resolveDirectMode());
    assertFalse(subject.removeWalkAllTheWayResults());
    assertEquals(Set.of(StreetMode.BIKE), subject.originalDirectMode());
  }

  @Test
  public void directModeIsExistAndIsWalking() {
    var modes = new RequestModes(null, null, null, StreetMode.WALK, null);

    var subject = new FilterTransitWhenDirectModeIsEmpty(modes);

    assertEquals(Set.of(StreetMode.WALK), subject.resolveDirectMode());
    assertFalse(subject.removeWalkAllTheWayResults());
    assertEquals(Set.of(StreetMode.WALK), subject.originalDirectMode());
  }

  @Test
  public void directModeIsEmpty() {
    var modes = new RequestModes((StreetMode) null, null, null, null, null);

    var subject = new FilterTransitWhenDirectModeIsEmpty(modes);

    assertEquals(Set.of(StreetMode.WALK), subject.resolveDirectMode());
    assertTrue(subject.removeWalkAllTheWayResults());
    assertEquals(Set.of(), subject.originalDirectMode());
  }
}