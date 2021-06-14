package org.opentripplanner.updater.vehicle_parking;

import java.util.Collection;
import java.util.List;
import org.opentripplanner.updater.DataSourceType;
import org.opentripplanner.updater.PollingGraphUpdaterParameters;

public class VehicleParkingUpdaterParameters implements PollingGraphUpdaterParameters {

  private final String configRef;
  private final String url;
  private final String feedId;
  private final int frequencySec;
  private final String namePrefix;
  private final boolean zip;
  private final DataSourceType sourceType;
  private final List<String> tags;


  public VehicleParkingUpdaterParameters(
      String configRef,
      String url,
      String feedId,
      String namePrefix,
      int frequencySec,
      boolean zip,
      DataSourceType sourceType,
      List<String> tags
  ) {
    this.configRef = configRef;
    this.url = url;
    this.feedId = feedId;
    this.frequencySec = frequencySec;
    this.namePrefix = namePrefix;
    this.zip = zip;
    this.sourceType = sourceType;
    this.tags = tags;
  }

  @Override
  public int getFrequencySec() { return frequencySec; }

  /**
   * The config name/type for the updater. Used to reference the configuration element.
   */
  @Override
  public String getConfigRef() {
    return configRef;
  }

  public DataSourceType getSourceType() {
    return sourceType;
  }

  public String getFeedId() {
    return feedId;
  }

  public String getUrl() {
    return url;
  }

  public String getNamePrefix() {
    return namePrefix;
  }

  public boolean isZip() {
    return zip;
  }

  public Collection<String> getTags() {
    return tags;
  }
}
