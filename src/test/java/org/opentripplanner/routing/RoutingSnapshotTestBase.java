package org.opentripplanner.routing;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.opentripplanner.ConstantsForTests;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.GenericLocation;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.model.plan.Leg;
import org.opentripplanner.model.plan.WalkStep;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.api.response.RoutingResponse;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.standalone.config.RouterConfig;
import org.opentripplanner.standalone.server.Router;
import org.opentripplanner.transit.raptor.util.TimeUtils;
import org.opentripplanner.util.TestUtils;

import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

import static io.github.jsonSnapshot.SnapshotMatcher.expect;

public abstract class RoutingSnapshotTestBase {

    private static final ObjectMapper objectMapper = buildObjectMapper();
    private static final PrettyPrinter pp = buildDefaultPrettyPrinter();

    static final boolean verbose = Boolean.getBoolean("otp.test.verbose");

    protected Router router;

    public static void loadGraphBeforeClass() {
        ConstantsForTests.getInstance().getPortlandGraph();
    }

    protected Router getRouter() {
        if (router == null) {
            Graph graph = ConstantsForTests.getInstance().getPortlandGraph();

            router = new Router(graph, RouterConfig.DEFAULT);
            router.startup();
        }

        return router;
    }

    protected RoutingRequest createTestRequest(int year, int month, int day, int hour, int minute, int second) {
        Router router = getRouter();

        RoutingRequest request = router.defaultRoutingRequest.clone();
        request.dateTime = TestUtils.dateInSeconds(router.graph.getTimeZone().getID(), year, month, day, hour, minute, second);
        request.maxTransfers = 6;
        request.numItineraries = 6;
        request.searchWindow = Duration.ofHours(5);

        return request;
    }

    protected void printItineraries(List<Itinerary> itineraries, long startMillis, long endMillis,
            TimeZone timeZone) {
        ZoneId zoneId = timeZone.toZoneId();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

        System.out.println("\n");

        for (int i = 0; i < itineraries.size(); i++) {
            Itinerary itinerary = itineraries.get(i);
            System.out
                    .printf("Itinerary %2d - duration: %s [%5d] (effective: %s [%5d]) - wait time: %d seconds, transit time: %d seconds\n",
                            i, TimeUtils.timeToStrShort(itinerary.durationSeconds),
                            itinerary.durationSeconds,
                            TimeUtils.timeToStrShort(itinerary.effectiveDurationSeconds()),
                            itinerary.effectiveDurationSeconds(), itinerary.waitingTimeSeconds,
                            itinerary.transitTimeSeconds);

            for (int j = 0; j < itinerary.legs.size(); j++) {
                Leg leg = itinerary.legs.get(j);
                String mode = leg.mode.name().substring(0, 1);
                System.out.printf(" - leg %2d - %52.52s %9s --%s-> %-9s %-52.52s\n", j, leg.from.toStringShort(),
                        dtf.format(leg.startTime.toInstant().atZone(zoneId)), mode,
                        dtf.format(leg.endTime.toInstant().atZone(zoneId)), leg.to.toStringShort());
            }

            System.out.println();
        }

        long printMillis = System.currentTimeMillis();

        System.out.println(
                "  Request duration: " + Duration.ofMillis(endMillis - startMillis).toMillis()
                        + " ms");
        System.out.println(
                "Request print time: " + Duration.ofMillis(printMillis - endMillis).toMillis()
                        + " ms");
    }

    protected void expectRequestResponseToMatchSnapshot(RoutingRequest request) {
        Router router = getRouter();

        request.setRoutingContext(router.graph);

        long startMillis = System.currentTimeMillis();
        RoutingService routingService = new RoutingService(router.graph);
        RoutingResponse response = routingService.route(request, router);
        request.cleanup();

        List<Itinerary> itineraries = response.getTripPlan().itineraries;

        if (verbose) {
            printItineraries(itineraries, startMillis, System.currentTimeMillis(),
                    router.graph.getTimeZone());
        }

        expectItinerariesToMatchSnapshot(itineraries);
    }

    protected void expectItinerariesToMatchSnapshot(List<Itinerary> itineraries) {
        sanitizeItinerariesForSnapshot(itineraries);

        expect(itineraries).toMatchSnapshot();
    }

    private void sanitizeItinerariesForSnapshot(List<Itinerary> itineraries) {
        itineraries.forEach(itinerary -> itinerary.legs.forEach(leg -> {
            sanitizeWalkStepsForSnapshot(leg.walkSteps);
        }));
    }

    private void sanitizeWalkStepsForSnapshot(List<WalkStep> walkSteps) {
        walkSteps.forEach(walkStep -> {
            walkStep.edges.clear();
        });
    }

    /**
     * Workaround for an incompatibility between latest Jackson and json-snapshot libs.
     * <p>
     * Intended to replace {@code io.github.jsonSnapshot.SnapshotMatcher#defaultJsonFunction}
     *
     * @see <a href="https://github.com/json-snapshot/json-snapshot.github.io/issues/27">Issue in json-snapshot project</a>
     */
    public static String asJsonString(Object object) {
        try {
            return objectMapper.writer(pp).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Unmodified copy of {@code io.github.jsonSnapshot.SnapshotMatcher#buildObjectMapper}
     */
    private static ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        objectMapper.setVisibility(
                objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        return objectMapper;
    }

    /**
     * Modified copy of {@code io.github.jsonSnapshot.SnapshotMatcher#buildDefaultPrettyPrinter}
     */
    private static PrettyPrinter buildDefaultPrettyPrinter() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter("") {
            @Override public DefaultPrettyPrinter createInstance() {
                return this;
            }

            @Override public DefaultPrettyPrinter withSeparators(Separators separators) {
                this._separators = separators;
                this._objectFieldValueSeparatorWithSpaces =
                        separators.getObjectFieldValueSeparator() + " ";
                return this;
            }
        };

        DefaultPrettyPrinter.Indenter lfOnlyIndenter = new DefaultIndenter("  ", "\n");
        pp.indentArraysWith(lfOnlyIndenter);
        pp.indentObjectsWith(lfOnlyIndenter);
        return pp;
    }
}
