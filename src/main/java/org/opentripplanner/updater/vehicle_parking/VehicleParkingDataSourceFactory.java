package org.opentripplanner.updater.vehicle_parking;

public class VehicleParkingDataSourceFactory {

  private VehicleParkingDataSourceFactory() {}

  public static VehicleParkingDataSource create(VehicleParkingUpdaterParameters source) {
    switch (source.getSourceType()) {
      case KML:        return new KmlBikeParkDataSource(source.getUrl(), source.getFeedId(), source.getNamePrefix(), source.isZip());
    }
    throw new IllegalArgumentException(
        "Unknown vehicle parking source type: " + source.getSourceType()
    );
  }
}
