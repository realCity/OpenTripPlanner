package org.opentripplanner.ext.flex.flexpathcalculator;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.opentripplanner.routing.graph.Vertex;

/**
 * FlexPathCalculator is used to calculate the driving times and distances during flex routing
 */
public interface FlexPathCalculator {

  @Nullable
  FlexPath calculateFlexPath(Vertex fromv, Vertex tov, int fromStopIndex, int toStopIndex);

  void initWith(Map<Vertex, Set<Vertex>> map);
}
