package org.opentripplanner.model.plan;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Stop;

public class PlaceTest {

    @Test
    public void sameLocationBasedOnInstance() {
        Place aPlace = new Place(60.0, 10.0, "A Place");
        assertTrue("same instance", aPlace.sameLocation(aPlace));
    }

    @Test
    public void sameLocationBasedOnCoordinates() {
        Place aPlace = new Place(60.0, 10.0, "A Place");
        Place samePlace = new Place(60.000000000001, 10.0000000000001, "Same Place");
        Place otherPlace = new Place(65.0, 14.0, "Other Place");

        assertTrue("same place", aPlace.sameLocation(samePlace));
        assertTrue("same place(symmetric)", samePlace.sameLocation(aPlace));
        assertFalse("other place", aPlace.sameLocation(otherPlace));
        assertFalse("other place(symmetric)", otherPlace.sameLocation(aPlace));
    }

    @Test
    public void sameLocationBasedOnStopId() {
        Place aPlace = place("A Place", "1");
        Place samePlace = place("Same Place", "1");
        Place otherPlace = place("Other Place", "2");

        assertTrue("same place", aPlace.sameLocation(samePlace));
        assertTrue("same place(symmetric)", samePlace.sameLocation(aPlace));
        assertFalse("other place", aPlace.sameLocation(otherPlace));
        assertFalse("other place(symmetric)", otherPlace.sameLocation(aPlace));
    }

    private static Place place(String name, String stopId) {
        return Place.builder()
                .name(name)
                .stop(new Stop(new FeedScopedId("S", stopId), null, null, null, null, null, null, null, null, null, null, null))
                .build();
    }
}