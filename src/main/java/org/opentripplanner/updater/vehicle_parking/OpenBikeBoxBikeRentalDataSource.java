package org.opentripplanner.updater.vehicle_parking;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.updater.GenericJsonDataSource;
import org.opentripplanner.util.NonLocalizedString;

public class OpenBikeBoxBikeRentalDataSource extends GenericJsonDataSource<VehicleParking> {

  private static final String JSON_PARSE_PATH = "data";

  private final String feedId;

  public OpenBikeBoxBikeRentalDataSource(String url, String feedId) {
    super(url, JSON_PARSE_PATH);
    this.feedId = feedId;
  }

  @Override
  protected VehicleParking parseElement(JsonNode jsonNode) {

    var vehicleParkId = new FeedScopedId(feedId, jsonNode.path("id").asText());
    double x = jsonNode.path("lon").asDouble();
    double y = jsonNode.path("lat").asDouble();

    VehicleParking.VehicleParkingEntranceCreator entrance = builder -> builder
        .entranceId(new FeedScopedId(feedId, vehicleParkId.getId() + "/entrance"))
        .name(new NonLocalizedString(jsonNode.path("name").asText()))
        .x(x)
        .y(y)
        .walkAccessible(true);

    return VehicleParking.builder()
        .id(vehicleParkId)
        .name(new NonLocalizedString(jsonNode.path("name").asText()))
        .state(VehicleParking.VehicleParkingState.OPERATIONAL)
        .x(x)
        .y(y)
        .detailsUrl(jsonNode.has("booking_url") ? jsonNode.get("booking_url").asText() : null)
        .imageUrl(getPhotoUrl(jsonNode))
        .bicyclePlaces(true)
        .entrance(entrance)
        .build();
  }

  private static String getPhotoUrl(JsonNode jsonNode) {
    if(!jsonNode.has("photo")) {
      return null;
    }
    var photoPath = jsonNode.path("photo");
    if (photoPath.has("url")) {
      return photoPath.get("url").asText();
    }
    return null;
  }
}
