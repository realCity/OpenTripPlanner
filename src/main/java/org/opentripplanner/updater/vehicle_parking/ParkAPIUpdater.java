package org.opentripplanner.updater.vehicle_parking;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import lombok.SneakyThrows;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.routing.core.OsmOpeningHours;
import org.opentripplanner.routing.core.TimeRestriction;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.updater.GenericJsonDataSource;
import org.opentripplanner.util.I18NString;
import org.opentripplanner.util.NonLocalizedString;
import org.opentripplanner.util.TranslatedString;

class ParkAPIUpdater extends GenericJsonDataSource<VehicleParking> {

  private static final String JSON_PARSE_PATH = "lots";

  private final String feedId;

  public ParkAPIUpdater(String url, String feedId) {
    super(url, JSON_PARSE_PATH);
    this.feedId = feedId;
  }

  @Override
  protected VehicleParking parseElement(JsonNode jsonNode) {

    var totalPlaces = parseCapacity(jsonNode, "total");
    var totalWheelchairAccessiblePlaces = parseCapacity(jsonNode, "total:disabled");
    VehicleParking.VehiclePlaces capacity = null;

    if (totalPlaces.isPresent() || totalWheelchairAccessiblePlaces.isPresent()) {
      capacity = VehicleParking.VehiclePlaces.builder()
          .carSpaces(totalPlaces.orElse(-1))
          .wheelchairAccessibleCarSpaces(totalWheelchairAccessiblePlaces.orElse(-1))
          .build();
    }

    VehicleParking.VehiclePlaces availability = null;
    var freePlaces = parseCapacity(jsonNode, "free");
    var freeWheelchairAccessiblePlaces = parseCapacity(jsonNode, "free:disabled");

    if(freePlaces.isPresent() || freeWheelchairAccessiblePlaces.isPresent()) {
      availability = VehicleParking.VehiclePlaces.builder()
          .carSpaces(freePlaces.orElse(-1))
          .wheelchairAccessibleCarSpaces(freeWheelchairAccessiblePlaces.orElse(-1))
          .build();
    }

    I18NString note = null;
    if (jsonNode.has("notes") && !jsonNode.get("notes").isEmpty()) {
      var noteFieldIterator = jsonNode.path("notes").fields();
      Map<String, String> noteLocalizations = new HashMap<>();
      while(noteFieldIterator.hasNext()) {
        var noteFiled = noteFieldIterator.next();
        noteLocalizations.put(noteFiled.getKey(), noteFiled.getValue().asText());
      }
      note = TranslatedString.getI18NString(noteLocalizations);
    }

    var vehicleParkId = createIdForNode(jsonNode);
    double x = jsonNode.path("coords").path("lng").asDouble();
    double y = jsonNode.path("coords").path("lat").asDouble();

    VehicleParking.VehicleParkingEntranceCreator entrance = builder -> builder
        .entranceId(new FeedScopedId(feedId, vehicleParkId.getId() + "/entrance"))
        .name(new NonLocalizedString(jsonNode.path("name").asText()))
        .x(x)
        .y(y)
        .walkAccessible(true)
        .carAccessible(true);

    var stateText = jsonNode.get("state").asText();
    var state = stateText.equals("open") || stateText.equals("many") ?
        VehicleParking.VehicleParkingState.OPERATIONAL : VehicleParking.VehicleParkingState.CLOSED;

    return VehicleParking.builder()
        .id(vehicleParkId)
        .name(new NonLocalizedString(jsonNode.path("name").asText()))
        .state(state)
        .x(x)
        .y(y)
        .openingHours(parseOpeningHours(jsonNode.path("opening_hours")))
        .feeHours(parseOpeningHours(jsonNode.path("fee_hours")))
        .detailsUrl(jsonNode.has("url") ? jsonNode.get("url").asText() : null)
        .note(note)
        .capacity(capacity)
        .availability(availability)
        .carPlaces(true)
        .entrance(entrance)
        .tags(parseTags(jsonNode, "lot_type", "address", "forecast"))
        .build();
  }

  @SneakyThrows
  private TimeRestriction parseOpeningHours(JsonNode jsonNode) {
    if (jsonNode == null) {
      return null;
    }

    return OsmOpeningHours.parseFromOsm(jsonNode.asText());
  }

  private OptionalInt parseCapacity(JsonNode jsonNode, String filedName) {
    if (!jsonNode.has(filedName)) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(jsonNode.get(filedName).asInt());
  }

  private FeedScopedId createIdForNode(JsonNode jsonNode) {
    String id;
    if (jsonNode.has("id")) {
      id = jsonNode.path("id").asText();
    } else {
      id = String.format("%s/%f/%f", jsonNode.get("name"), jsonNode.path("coords").path("lng").asDouble(), jsonNode.path("coords").path("lat").asDouble());
    }
    return new FeedScopedId(feedId, id);
  }

  private List<String> parseTags(JsonNode node, String... tagNames) {
    var tagList = new ArrayList<String>();
    for(var tagName : tagNames) {
      if (node.has(tagName)) {
        tagList.add(tagName + ":" + node.get(tagName).asText());
      }
    }
    return tagList;
  }
}
