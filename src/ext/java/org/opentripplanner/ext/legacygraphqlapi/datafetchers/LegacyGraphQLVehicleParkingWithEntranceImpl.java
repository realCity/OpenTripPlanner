package org.opentripplanner.ext.legacygraphqlapi.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.opentripplanner.ext.legacygraphqlapi.generated.LegacyGraphQLDataFetchers.LegacyGraphQLVehicleParkingWithEntrance;
import org.opentripplanner.model.plan.VehicleParkingWithEntrance;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;

public class LegacyGraphQLVehicleParkingWithEntranceImpl
        implements LegacyGraphQLVehicleParkingWithEntrance {

    @Override
    public DataFetcher<VehicleParking> vehicleParking() {
        return (environment) -> getSource(environment).getVehicleParking();
    }

    @Override
    public DataFetcher<Boolean> closesSoon() {
        return (environment) -> getSource(environment).isClosesSoon();
    }

    @Override
    public DataFetcher<Boolean> realtime() {
        return (environment) -> getSource(environment).isRealtime();
    }

    private VehicleParkingWithEntrance getSource(DataFetchingEnvironment environment) {
        return environment.getSource();
    }
}
