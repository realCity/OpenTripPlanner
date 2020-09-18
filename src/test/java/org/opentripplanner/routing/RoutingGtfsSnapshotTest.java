package org.opentripplanner.routing;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentripplanner.model.TransitMode;
import org.opentripplanner.routing.api.request.RequestModes;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.api.request.StreetMode;

import java.util.Set;

import static java.util.Collections.emptySet;

public class RoutingGtfsSnapshotTest
        extends RoutingSnapshotTestBase {

    @BeforeClass public static void beforeClass() {
        SnapshotMatcher.start(RoutingSnapshotTestBase::asJsonString);
        loadGraphBeforeClass();
    }

    @Test public void test_trip_planning_with_walk_only() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                emptySet());
        request.from = p0;
        request.to = p2;

        expectArriveByToMatchDepartAtAndSnapshot(request);
    }

    @Test public void test_trip_planning_with_walk_only_arrive_by() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 11, 3, 24);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                emptySet());
        request.arriveBy = true;
        request.from = p0;
        request.to = p2;

        expectRequestResponseToMatchSnapshot(request);
    }

    @Test public void test_trip_planning_with_walk_only_stop() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                emptySet());
        request.from = ps;
        request.to = p2;

        expectArriveByToMatchDepartAtAndSnapshot(request);
    }

    @Test public void test_trip_planning_with_walk_only_stop_collection() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                emptySet());
        request.from = ptc;
        request.to = p3;

        expectRequestResponseToMatchSnapshot(request);
        // not equal - expectArriveByToMatchDepartAtAndSnapshot(request);
    }

    @Test public void test_trip_planning_with_transit() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                Set.of(TransitMode.values()));
        request.from = p1;
        request.to = p2;

        expectRequestResponseToMatchSnapshot(request);
    }

    @Test public void test_trip_planning_with_transit_max_walk() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                Set.of(TransitMode.values()));
        request.maxWalkDistance = 1000;
        request.from = p1;
        request.to = p2;

        expectArriveByToMatchDepartAtAndSnapshot(request);
    }

    @Test public void test_trip_planning_with_transit_stop() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                Set.of(TransitMode.values()));
        request.maxWalkDistance = 1000;
        request.from = ps;
        request.to = p3;

        expectArriveByToMatchDepartAtAndSnapshot(request);
    }

    @Test public void test_trip_planning_with_transit_stop_collection() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                Set.of(TransitMode.values()));
        request.maxWalkDistance = 1000;
        request.from = ptc;
        request.to = p3;

        expectArriveByToMatchDepartAtAndSnapshot(request);
    }
}
