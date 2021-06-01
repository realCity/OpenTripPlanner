package org.opentripplanner.routing.graphfinder;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.model.StopLocation;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.vertextype.TransitStopVertex;

/**
 * A specific stop at a distance. Also includes a geometry and potentially a list of edges and a
 * state of how to reach the stop from the search origin
 */
public class NearbyStop implements Comparable<NearbyStop> {

  public final StopLocation stop;
  public final double distance;

  public final List<Edge> edges;
  public final LineString geometry;
  public final State state;

  public NearbyStop(
      StopLocation stop, double distance, List<Edge> edges, LineString geometry, State state
  ) {
    this.stop = stop;
    this.distance = distance;
    this.edges = edges;
    this.geometry = geometry;
    this.state = state;
  }

  public NearbyStop(
      TransitStopVertex stopVertex, double distance, List<Edge> edges, State state
  ) {
    this(stopVertex.getStop(), distance, edges, null, state);
  }

  @Override
  public int compareTo(NearbyStop that) {
    return (int) (this.distance) - (int) (that.distance);
  }

  public String toString() {
    return String.format("stop %s at %.1f meters", stop, distance);
  }

  /**
   * Given a State at a StopVertex, bundle the StopVertex together with information about how far
   * away it is and the geometry of the path leading up to the given State.
   */
  public static NearbyStop nearbyStopForState(State state, StopLocation stop) {
    double effectiveWalkDistance = 0.0;
    var graphPath = new GraphPath(state);
    var edges = new ArrayList<Edge>();
    for (Edge edge : graphPath.edges) {
      effectiveWalkDistance += edge.getEffectiveWalkDistance();
      edges.add(edge);
    }
    return new NearbyStop(
        stop,
        effectiveWalkDistance,
        edges,
        null,
        state
    );
  }
}
