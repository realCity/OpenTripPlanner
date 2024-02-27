package org.opentripplanner.ext.vehicleparking.bikely;

import static org.opentripplanner.routing.vehicle_parking.VehicleParkingState.OPERATIONAL;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Currency;
import org.opentripplanner.framework.geometry.WgsCoordinate;
import org.opentripplanner.framework.i18n.LocalizedString;
import org.opentripplanner.framework.i18n.NonLocalizedString;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingSpaces;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingState;
import org.opentripplanner.transit.model.basic.LocalizedMoney;
import org.opentripplanner.transit.model.basic.Money;
import org.opentripplanner.transit.model.framework.FeedScopedId;
import org.opentripplanner.updater.spi.GenericJsonDataSource;

/**
 * Vehicle parking updater class for the Norwegian bike box provider Bikely:
 * https://www.safebikely.com/
 */
public class BikelyUpdater extends GenericJsonDataSource<VehicleParking> {

  private static final String JSON_PARSE_PATH = "result";
  private static final Currency NOK = Currency.getInstance("NOK");
  private final String feedId;

  public BikelyUpdater(BikelyUpdaterParameters parameters) {
    super(parameters.url().toString(), JSON_PARSE_PATH, parameters.httpHeaders());
    this.feedId = parameters.feedId();
  }

  @Override
  protected VehicleParking parseElement(JsonNode jsonNode) {
    var vehicleParkId = new FeedScopedId(feedId, jsonNode.get("id").asText());

    var workingHours = jsonNode.get("workingHours");

    var lat = jsonNode.get("latitude").asDouble();
    var lng = jsonNode.get("longitude").asDouble();
    var coord = new WgsCoordinate(lat, lng);

    var name = new NonLocalizedString(jsonNode.path("name").asText());

    var totalSpots = jsonNode.get("totalStandardSpots").asInt();
    var freeSpots = jsonNode.get("availableStandardSpots").asInt();
    var isUnderMaintenance = jsonNode.get("isInMaintenance").asBoolean();

    LocalizedString note = toNote(jsonNode);

    VehicleParking.VehicleParkingEntranceCreator entrance = builder ->
      builder
        .entranceId(new FeedScopedId(feedId, vehicleParkId.getId() + "/entrance"))
        .name(name)
        .coordinate(coord)
        .walkAccessible(true)
        .carAccessible(false);

    return VehicleParking
      .builder()
      .id(vehicleParkId)
      .name(name)
      .bicyclePlaces(true)
      .capacity(VehicleParkingSpaces.builder().bicycleSpaces(totalSpots).build())
      .availability(VehicleParkingSpaces.builder().bicycleSpaces(freeSpots).build())
      .state(toState(isUnderMaintenance))
      .coordinate(coord)
      .entrance(entrance)
      .note(note)
      .build();
  }

  private static LocalizedString toNote(JsonNode price) {
    var startPriceAmount = price.get("startPriceAmount").floatValue();
    var mainPriceAmount = price.get("mainPriceAmount").floatValue();

    var startPriceDurationHours = price.get("startPriceDuration").asInt();
    var mainPriceDurationHours = price.get("mainPriceDuration").asInt();

    if (startPriceAmount == 0 && mainPriceAmount == 0) {
      return new LocalizedString("price.free");
    } else {
      return new LocalizedString(
        "price.startMain",
        NonLocalizedString.ofNumber(startPriceDurationHours),
        new LocalizedMoney(Money.ofFractionalAmount(NOK, startPriceAmount)),
        new LocalizedMoney(Money.ofFractionalAmount(NOK, mainPriceAmount)),
        NonLocalizedString.ofNumber(mainPriceDurationHours)
      );
    }
  }

  private static VehicleParkingState toState(boolean isUnderMaintenance) {
    if (isUnderMaintenance) return VehicleParkingState.TEMPORARILY_CLOSED; else return OPERATIONAL;
  }
}
