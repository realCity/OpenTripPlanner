package org.opentripplanner.updater.vehicle_parking;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.routing.vehicle_parking.VehicleParking.VehiclePlaces;

public class CarParkAPIUpdater extends ParkAPIUpdater {

    public CarParkAPIUpdater(String url, String feedId) {
        super(url, feedId);
    }

    @Override
    protected VehiclePlaces parseCapacity(JsonNode jsonNode) {
        return parseVehiclePlaces(jsonNode, null, "total", "total:disabled");
    }

    @Override
    protected VehiclePlaces parseAvailability(JsonNode jsonNode) {
        return parseVehiclePlaces(jsonNode, null, "free", "free:disabled");
    }
}
