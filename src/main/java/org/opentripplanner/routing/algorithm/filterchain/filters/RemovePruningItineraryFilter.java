package org.opentripplanner.routing.algorithm.filterchain.filters;

import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryFilter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter itineraries and removes ones created for pruning results.
 */
public class RemovePruningItineraryFilter implements ItineraryFilter {

    private final List<Itinerary> pruningItineraries;

    public RemovePruningItineraryFilter(List<Itinerary> pruningItineraries) {
        this.pruningItineraries = pruningItineraries;
    }

    @Override
    public String name() {
        return "remove-pruning-itinerary-filter";
    }

    @Override
    public List<Itinerary> filter(List<Itinerary> itineraries) {
        return itineraries
            .stream()
            .filter(it -> !pruningItineraries.contains(it))
            .collect(Collectors.toList());
    }

    @Override
    public boolean removeItineraries() {
        return true;
    }
}
