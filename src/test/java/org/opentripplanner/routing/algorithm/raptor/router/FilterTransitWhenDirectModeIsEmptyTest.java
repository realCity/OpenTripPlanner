package org.opentripplanner.routing.algorithm.raptor.router;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.opentripplanner.routing.api.request.RequestModes;
import org.opentripplanner.routing.api.request.StreetMode;

public class FilterTransitWhenDirectModeIsEmptyTest {

  @Test
  public void directModeIsExistAndIsNotWalking() {
    var modes = new RequestModes(null, null, null, StreetMode.BIKE, null);

    var subject = new FilterTransitWhenDirectModeIsEmpty(modes);

    assertEquals(StreetMode.BIKE, subject.resolveDirectMode());
    assertFalse(subject.removeWalkAllTheWayResults());
    assertEquals(StreetMode.BIKE, subject.originalDirectMode());
  }

  @Test
  public void directModeIsExistAndIsWalking() {
    var modes = new RequestModes(null, null, null, StreetMode.WALK, null);

    var subject = new FilterTransitWhenDirectModeIsEmpty(modes);

    assertEquals(StreetMode.WALK, subject.resolveDirectMode());
    assertFalse(subject.removeWalkAllTheWayResults());
    assertEquals(StreetMode.WALK, subject.originalDirectMode());
  }

  @Test
  public void directModeIsEmpty() {
    var modes = new RequestModes(null, null, null, null, null);

    var subject = new FilterTransitWhenDirectModeIsEmpty(modes);

    assertEquals(StreetMode.WALK, subject.resolveDirectMode());
    assertTrue(subject.removeWalkAllTheWayResults());
    assertNull(subject.originalDirectMode());
  }

  @Test
  public void directModeIsFlex() {
    var modes = new RequestModes(null, null, null, StreetMode.FLEXIBLE, null);

    var subject = new FilterTransitWhenDirectModeIsEmpty(modes);

    assertEquals(StreetMode.FLEXIBLE, subject.resolveDirectMode());
    assertTrue(subject.removeWalkAllTheWayResults());
    assertEquals(StreetMode.FLEXIBLE, subject.originalDirectMode());
  }
}