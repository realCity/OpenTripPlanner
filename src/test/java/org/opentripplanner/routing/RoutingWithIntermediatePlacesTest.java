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

public class RoutingWithIntermediatePlacesTest extends RoutingSnapshotTestBase {

    @BeforeClass public static void beforeClass() {
        SnapshotMatcher.start(RoutingSnapshotTestBase::asJsonString);
        loadGraphBeforeClass();
    }

    @Test public void test_trip_planning_with_walk_only_intermediate_places() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                emptySet());
        request.from = p0;
        request.addIntermediatePlace(p1);
        request.addIntermediatePlace(p2);
        request.addIntermediatePlace(p3);
        request.to = p4;

        expectRequestResponseToMatchSnapshot(request);
    }

    @Test public void test_trip_planning_with_walk_only_intermediate_places_arrive_by() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.setArriveBy(true);
        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                emptySet());
        request.from = p0;
        request.addIntermediatePlace(p1);
        request.addIntermediatePlace(p2);
        request.addIntermediatePlace(p3);
        request.to = p4;

        expectRequestResponseToMatchSnapshot(request);
    }

    @Test public void test_trip_planning_with_intermediate_places() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                Set.of(TransitMode.values()));
        request.from = p0;
        request.addIntermediatePlace(p1);
        request.addIntermediatePlace(p2);
        request.addIntermediatePlace(p3);
        request.to = p4;

        expectRequestResponseToMatchSnapshot(request);
    }

    @Test public void test_trip_planning_with_intermediate_places_arrive_by() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.setArriveBy(true);
        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                Set.of(TransitMode.values()));
        request.from = p0;
        request.addIntermediatePlace(p1);
        request.addIntermediatePlace(p2);
        request.addIntermediatePlace(p3);
        request.to = p4;

        expectRequestResponseToMatchSnapshot(request);
    }

    @Test public void test_trip_planning_with_initial_stop_collection() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                Set.of(TransitMode.values()));
        request.from = ptc;
        request.addIntermediatePlace(p2);
        request.to = p4;
        request.setMaxWalkDistance(1000);

        expectArriveByToMatchDepartAtAndSnapshot(request);
    }

    @Test public void test_trip_planning_with_initial_stop_collection_arrive_by() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 11, 11, 23);

        request.setArriveBy(true);
        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                Set.of(TransitMode.values()));
        request.from = ptc;
        request.addIntermediatePlace(p2);
        request.to = p4;
        request.setMaxWalkDistance(1000);

        expectRequestResponseToMatchSnapshot(request);
    }

    @Test public void test_trip_planning_with_intermediate_stop_collection() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                Set.of(TransitMode.values()));
        request.from = p0;
        request.addIntermediatePlace(ptc);
        request.to = p4;

        expectRequestResponseToMatchSnapshot(request);
    }

    @Test public void test_trip_planning_with_intermediate_stop_collection_arrive_by() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 41, 01);

        request.setArriveBy(true);
        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                Set.of(TransitMode.values()));
        request.from = p0;
        request.addIntermediatePlace(ptc);
        request.to = p4;

        expectRequestResponseToMatchSnapshot(request);
    }

    @Test public void test_trip_planning_with_last_stop_collection() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                Set.of(TransitMode.values()));
        request.from = p2;
        request.addIntermediatePlace(p4);
        request.to = ptc;

        expectRequestResponseToMatchSnapshot(request);
    }

    @Test public void test_trip_planning_with_last_stop_collection_arrive_be() {
        RoutingRequest request = createTestRequest(2009, 10, 17, 10, 0, 0);

        request.setArriveBy(true);
        request.modes = new RequestModes(StreetMode.WALK, StreetMode.WALK, StreetMode.WALK,
                Set.of(TransitMode.values()));
        request.from = p2;
        request.addIntermediatePlace(p4);
        request.to = ptc;

        expectRequestResponseToMatchSnapshot(request);
    }
}
