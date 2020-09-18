package org.opentripplanner.routing.algorithm.filterchain.filters;

import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryFilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Remove all street-only itineraries after the provided limit. This filter remove the itineraries at the
 * end of the list, so the list should be sorted on the desired key before this filter is applied.
 */
public class MaxOnStreetOnlyLimitFilter implements ItineraryFilter {

    private final String name;
    private final int maxLimit;

    public MaxOnStreetOnlyLimitFilter(String name, int maxLimit) {
        this.name = name;
        this.maxLimit = maxLimit;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<Itinerary> filter(final List<Itinerary> itineraries) {
        if(itineraries.size() <= maxLimit) { return itineraries; }

        int found = 0;

        List<Itinerary> copy = new ArrayList<>(itineraries);
        Iterator<Itinerary> iterator = copy.iterator();
        while (iterator.hasNext()) {
            Itinerary itinerary = iterator.next();
            if (itinerary.streetOnly) {
                if (found < maxLimit) {
                    found++;
                } else {
                    iterator.remove();
                }
            }
        }
        return copy;
    }

    @Override
    public boolean removeItineraries() {
        return true;
    }
}
