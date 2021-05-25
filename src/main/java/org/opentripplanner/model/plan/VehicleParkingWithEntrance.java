package org.opentripplanner.model.plan;

import lombok.Builder;
import lombok.Getter;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.routing.vehicle_parking.VehicleParking.VehicleParkingEntrance;

@Getter
@Builder
public class VehicleParkingWithEntrance {
    private final VehicleParking vehicleParking;
    private final VehicleParkingEntrance entrance;

    /**
     * True if the difference of visiting time for a  {@link org.opentripplanner.routing.vehicle_parking.VehicleParking VehicleParking}
     * and the closing time is inside the request's
     * {@link org.opentripplanner.routing.api.request.RoutingRequest#vehicleParkingClosesSoonSeconds RoutingRequest#vehicleParkingClosesSoonSeconds}
     * interval.
     */
    public final boolean closesSoon;
}
