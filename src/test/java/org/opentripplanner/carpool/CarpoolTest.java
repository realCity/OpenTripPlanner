package org.opentripplanner.carpool;

import org.opentripplanner.GtfsTest;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.TransitMode;

public class CarpoolTest extends GtfsTest {

    @Override
    public String getFeedName() {
        return "mfdz_gtfs.zip";
    }

    public void testImport() {
        var routeId = FeedScopedId.parseId("FEED:3");
        var route = router.graph.index.getRouteForId(routeId);

        assertNotNull(route);

        assertEquals(TransitMode.CARPOOL, route.getMode());
    }
}
