package org.opentripplanner.ext.vehicleparking.hslpark;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opentripplanner.model.calendar.openinghours.OpeningHoursCalendarService;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingGroup;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingSpaces;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingSpaces.VehicleParkingSpacesBuilder;
import org.opentripplanner.transit.model.framework.FeedScopedId;
import org.opentripplanner.updater.DataSource;
import org.opentripplanner.util.xml.JsonDataListDownloader;

/**
 * Vehicle parking updater class for https://github.com/HSLdevcom/parkandrideAPI format APIs. There
 * has been further development in a private repository (the current state is documented in
 * https://p.hsl.fi/docs/index.html) but this updater supports both formats.
 */
public class HslParkUpdater implements DataSource<VehicleParking> {

  private static final String JSON_PARSE_PATH = "results";

  private final HslFacilitiesDownloader facilitiesDownloader;
  private final int facilitiesFrequencySec;
  private final HslHubsDownloader hubsDownloader;
  private final JsonDataListDownloader utilizationsDownloader;
  private final HslParkToVehicleParkingMapper vehicleParkingMapper;
  private final HslHubToVehicleParkingGroupMapper vehicleParkingGroupMapper;
  private final HslParkUtilizationToPatchMapper parkPatchMapper;

  private long lastFacilitiesFetchTime;

  private List<VehicleParking> parks;
  private Map<VehicleParkingGroup, List<FeedScopedId>> parksForhubs;

  public HslParkUpdater(
    HslParkUpdaterParameters parameters,
    OpeningHoursCalendarService openingHoursCalendarService
  ) {
    String feedId = parameters.getFeedId();
    vehicleParkingMapper =
      new HslParkToVehicleParkingMapper(
        feedId,
        openingHoursCalendarService,
        parameters.getTimeZone()
      );
    vehicleParkingGroupMapper = new HslHubToVehicleParkingGroupMapper(feedId);
    parkPatchMapper = new HslParkUtilizationToPatchMapper(feedId);
    facilitiesDownloader =
      new HslFacilitiesDownloader(
        parameters.getFacilitiesUrl(),
        JSON_PARSE_PATH,
        vehicleParkingMapper::parsePark
      );
    hubsDownloader =
      new HslHubsDownloader(
        parameters.getHubsUrl(),
        JSON_PARSE_PATH,
        vehicleParkingGroupMapper::parseHub
      );
    utilizationsDownloader =
      new JsonDataListDownloader<>(
        parameters.getUtilizationsUrl(),
        "",
        parkPatchMapper::parseUtilization,
        null
      );
    this.facilitiesFrequencySec = parameters.getFacilitiesFrequencySec();
  }

  /**
   * Update the data from the sources. It first fetches parks from the facilities URL and park
   * groups from hubs URL and then realtime updates from utilizations URL. If facilitiesFrequencySec
   * is configured to be over 0, it also occasionally retches the parks as new parks might have been
   * added or the state of the old parks might have changed.
   *
   * @return true if there might have been changes
   */
  @Override
  public boolean update() {
    List<VehicleParking> parks = null;
    Map<VehicleParkingGroup, List<FeedScopedId>> parksForhubs;
    if (fetchFacilitiesAndHubsNow()) {
      parksForhubs = hubsDownloader.downloadHubs();
      if (parksForhubs != null) {
        parks = facilitiesDownloader.downloadFacilities(parksForhubs);
        if (parks != null) {
          lastFacilitiesFetchTime = System.currentTimeMillis();
        }
      }
    } else {
      parks = this.parks;
      parksForhubs = this.parksForhubs;
    }
    if (parks != null) {
      List<HslParkPatch> utilizations = utilizationsDownloader.download();
      if (utilizations != null) {
        Map<FeedScopedId, List<HslParkPatch>> patches = utilizations
          .stream()
          .collect(Collectors.groupingBy(utilization -> utilization.getId()));
        parks.forEach(park -> {
          List<HslParkPatch> patchesForPark = patches.get(park.getId());
          if (patchesForPark != null) {
            park.updateAvailability(createVehicleAvailability(patchesForPark));
          }
        });
      } else if (this.parks != null) {
        return false;
      }
      synchronized (this) {
        // Update atomically
        this.parks = parks;
        this.parksForhubs = parksForhubs;
      }
      return true;
    }
    return false;
  }

  @Override
  public synchronized List<VehicleParking> getUpdates() {
    return parks;
  }

  private static VehicleParkingSpaces createVehicleAvailability(List<HslParkPatch> patches) {
    VehicleParkingSpacesBuilder availabilityBuilder = VehicleParkingSpaces.builder();
    boolean hasHandledSpaces = false;

    for (HslParkPatch patch : patches) {
      String type = patch.getCapacityType();

      if (type != null) {
        Integer spaces = patch.getSpacesAvailable();

        switch (type) {
          case "CAR":
            availabilityBuilder.carSpaces(spaces);
            hasHandledSpaces = true;
            break;
          case "BICYCLE":
            availabilityBuilder.bicycleSpaces(spaces);
            hasHandledSpaces = true;
            break;
          case "DISABLED":
            availabilityBuilder.wheelchairAccessibleCarSpaces(spaces);
            hasHandledSpaces = true;
            break;
        }
      }
    }

    return hasHandledSpaces ? availabilityBuilder.build() : null;
  }

  /**
   * @return true if facilities and hubs have not been successfully downloaded before, or
   * facilitiesFrequencySec > 0 and over facilitiesFrequencySec has passed since last successful
   * fetch
   */
  private boolean fetchFacilitiesAndHubsNow() {
    if (parks == null) {
      return true;
    }
    if (facilitiesFrequencySec <= 0) {
      return false;
    }
    return System.currentTimeMillis() > lastFacilitiesFetchTime + facilitiesFrequencySec * 1000;
  }
}
