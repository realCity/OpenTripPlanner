package org.opentripplanner.routing.algorithm.filterchain.filters;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryFilter;
import org.opentripplanner.routing.core.TraverseMode;

/**
 * Remove all itineraries where all the final leg is more than x minutes of walking.
 */
public class FlexOnlyToDestinationFilter implements ItineraryFilter {

    private final long maxWalkDuration = Duration.ofMinutes(2).toSeconds();

    @Override
    public String name() {
        return "flex-only-to-destination";
    }

    @Override
    public List<Itinerary> filter(List<Itinerary> itineraries) {
        return itineraries
                .stream().filter(it -> {
                    var lastLeg = it.lastLeg();
                    boolean lastLegIsLongWalk = lastLeg.mode == TraverseMode.WALK
                            && lastLeg.getDuration() > maxWalkDuration;
                    return !lastLegIsLongWalk;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean removeItineraries() {
        return true;
    }
}
