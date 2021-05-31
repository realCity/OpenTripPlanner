package org.opentripplanner.api.mapping;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.opentripplanner.api.model.ApiPlace;
import org.opentripplanner.api.model.ApiVehicleParkingWithEntrance;
import org.opentripplanner.api.model.ApiVehicleParkingWithEntrance.ApiVehicleParkingPlaces;
import org.opentripplanner.model.plan.Place;
import org.opentripplanner.model.plan.StopArrival;
import org.opentripplanner.model.plan.VehicleParkingWithEntrance;
import org.opentripplanner.routing.vehicle_parking.VehicleParking.VehiclePlaces;

public class PlaceMapper {

    public static List<ApiPlace> mapStopArrivals(Collection<StopArrival> domain) {
        if(domain == null) { return null; }

        return domain.stream().map(PlaceMapper::mapStopArrival).collect(Collectors.toList());
    }

    public static ApiPlace mapStopArrival(StopArrival domain) {
        return mapPlace(domain.place, domain.arrival, domain.departure);
    }

    public static ApiPlace mapPlace(Place domain, Calendar arrival, Calendar departure) {
        if(domain == null) { return null; }

        ApiPlace api = new ApiPlace();

        api.name = domain.getName();
        api.orig = domain.getOrig();
        if(domain.getCoordinate() != null) {
            api.lon = domain.getCoordinate().longitude();
            api.lat = domain.getCoordinate().latitude();
        }

        api.vertexType = VertexTypeMapper.mapVertexType(domain.getVertexType());

        api.arrival = arrival;
        api.departure = departure;

        switch (domain.getVertexType()) {
            case NORMAL:
                break;
            case BIKESHARE:
                api.bikeShareId = domain.getBikeRentalStation().id;
                break;
            case VEHICLEPARKING:
                api.vehicleParking = mapVehicleParking(domain.getVehicleParkingWithEntrance());
                break;
            case TRANSIT:
                api.stopId = FeedScopedIdMapper.mapToApi(domain.getStop().getId());
                api.stopCode = domain.getStop().getCode();
                api.platformCode = domain.getStop().getPlatformCode();
                api.zoneId = domain.getStop().getFirstZoneAsString();
                api.stopIndex = domain.getStopIndex();
                api.stopSequence = domain.getStopSequence();
                break;
        }

        return api;
    }

    private static ApiVehicleParkingWithEntrance mapVehicleParking(VehicleParkingWithEntrance vehicleParkingWithEntrance) {
        var vp = vehicleParkingWithEntrance.getVehicleParking();
        var e = vehicleParkingWithEntrance.getEntrance();

        return ApiVehicleParkingWithEntrance.builder()
                .id(FeedScopedIdMapper.mapToApi(vp.getId()))
                .name(vp.getName().toString())
                .entranceId(FeedScopedIdMapper.mapToApi(e.getEntranceId()))
                .entranceName(vp.getName().toString())
                .detailsUrl(vp.getDetailsUrl())
                .imageUrl(vp.getImageUrl())
                .note(vp.getNote() != null ? vp.getNote().toString() : null)
                .tags(new ArrayList<>(vp.getTags()))
                .hasBicyclePlaces(vp.hasBicyclePlaces())
                .hasAnyCarPlaces(vp.hasAnyCarPlaces())
                .hasCarPlaces(vp.hasCarPlaces())
                .hasWheelchairAccessibleCarPlaces(vp.hasWheelchairAccessibleCarPlaces())
                .availability(mapVehicleParkingPlaces(vp.getAvailability()))
                .capacity(mapVehicleParkingPlaces(vp.getCapacity()))
                .build();
    }

    private static ApiVehicleParkingPlaces mapVehicleParkingPlaces(VehiclePlaces availability) {
        if (availability == null) {
            return null;
        }

        return ApiVehicleParkingPlaces.builder()
                .bicycleSpaces(availability.getBicycleSpaces())
                .carSpaces(availability.getCarSpaces())
                .wheelchairAccessibleCarSpaces(availability.getWheelchairAccessibleCarSpaces())
                .build();
    }
}
