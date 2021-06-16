package org.opentripplanner.ext.flex.edgetype;

import static org.opentripplanner.routing.core.TraverseMode.CAR;

import java.util.Locale;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.routing.vertextype.TransitStopVertex;

/**
 * This represents the connection between a street vertex and a transit vertex
 * which is used only for Flex routing between stops.
 *
 * @see org.opentripplanner.ext.flex.flexpathcalculator.StreetFlexPathCalculator
 */
public class FlexStreetTransitStopLink extends Edge {

    public FlexStreetTransitStopLink(StreetVertex fromv, TransitStopVertex tov) {
    	super(fromv, tov);
    }

    public FlexStreetTransitStopLink(TransitStopVertex fromv, StreetVertex tov) {
        super(fromv, tov);
    }

    @Override
    public State traverse(State s0) {
        if (s0.getNonTransitMode() != CAR) {
            return null;
        }

        // Forbid taking shortcuts composed of two street-transit links associated with the same stop in a row.
        if (s0.getBackEdge() instanceof FlexStreetTransitStopLink) {
            return null;
        }

        var s1 = s0.edit(this);
        s1.incrementWeight(1);
        return s1.makeState();
    }

    @Override
    public LineString getGeometry() {
        Coordinate[] coordinates = new Coordinate[]{fromv.getCoordinate(), tov.getCoordinate()};
        return GeometryUtils.getGeometryFactory().createLineString(coordinates);
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public String getName(Locale locale) {
        return getName();
    }

    @Override
    public String toString() {
        return "FlexStreetTransitStopLink(" + fromv + " -> " + tov + ")";
    }
}
