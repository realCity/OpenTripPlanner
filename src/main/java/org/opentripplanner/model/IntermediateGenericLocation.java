package org.opentripplanner.model;

import org.opentripplanner.model.base.ValueObjectToStringBuilder;

import java.time.Duration;

/**
 * Represents a location that is to be used as an intermediate point in a routing request. The difference
 * compared to a {@link GenericLocation} is that minSlackTime and maxSlackTime may be specified to
 * bound the time at then intermediate places.
 * This has to be resolved to a vertex or a collection of vertices before routing can start.
 */
public class IntermediateGenericLocation extends GenericLocation {

    private static final Duration MAX_WAIT_TIME = Duration.ofHours(3);

    /**
     * The minimum time (wait) required at this intermediate point. If not set, then
     * {@link org.opentripplanner.routing.api.request.RoutingRequest#transferSlack} will be used.
     */
    public final Duration minWaitTime;

    /**
     * The maximum time (wait) allowed at this intermediate point. If not set, then
     * MAX_WAIT_TIME will be used.
     */
    public final Duration maxWaitTime;

    public IntermediateGenericLocation(String label, FeedScopedId stopId, Double lat, Double lng,
            Duration minWaitTime, Duration maxWaitTime) {
        super(label, stopId, lat, lng);
        this.minWaitTime = minWaitTime;
        this.maxWaitTime = maxWaitTime;
    }

    public IntermediateGenericLocation(String label, FeedScopedId stopId, Double lat, Double lng,
            Duration minWaitTime) {
        this(label, stopId, lat, lng, minWaitTime, MAX_WAIT_TIME);
    }

    @Override
    public String toString() {
        return ValueObjectToStringBuilder.of()
                .addStr(label)
                .addObj(stopId)
                .addStr(minWaitTime.toString())
                .addStr(maxWaitTime.toString())
                .addCoordinate(lat, lng)
                .toString();
    }
}
