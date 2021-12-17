package org.opentripplanner.updater.vehicle_parking;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import java.util.Map;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingSpaces;

public class CarParkAPIUpdater extends ParkAPIUpdater {

    public CarParkAPIUpdater(
            String url,
            String feedId,
            Map<String, String> httpHeaders,
            Collection<String> staticTags
    ) {
        super(url, feedId, httpHeaders, staticTags);
    }

    @Override
    protected VehicleParkingSpaces parseCapacity(JsonNode jsonNode) {
        return parseVehicleSpaces(jsonNode, null, "total", "total:disabled");
    }

    @Override
    protected VehicleParkingSpaces parseAvailability(JsonNode jsonNode) {
        return parseVehicleSpaces(jsonNode, null, "free", "free:disabled");
    }
}
