package org.opentripplanner.routing.api.request.request.filter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.opentripplanner.framework.tostring.ToStringBuilder;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;

/**
 * A request object that checks if parking faclities match certain conditions for
 * inclusion/exclusion or preference/unpreference.
 */
public class VehicleParkingFilterRequest implements Serializable {

  private final VehicleParkingSelect[] not;
  private final VehicleParkingSelect[] select;

  public VehicleParkingFilterRequest(
    Collection<VehicleParkingSelect> not,
    Collection<VehicleParkingSelect> select
  ) {
    this.not = makeFilter(not);
    this.select = makeFilter(select);
  }

  public VehicleParkingFilterRequest(VehicleParkingSelect not, VehicleParkingSelect select) {
    this(List.of(not), List.of(select));
  }

  public List<VehicleParkingSelect> not() {
    return Arrays.asList(not);
  }

  public List<VehicleParkingSelect> select() {
    return Arrays.asList(select);
  }

  /**
   * Create a request with no conditions.
   */
  public static VehicleParkingFilterRequest empty() {
    return new VehicleParkingFilterRequest(List.of(), List.of());
  }

  /**
   * Checks if a parking facility matches the conditions defined in this filter.
   */
  public boolean matches(VehicleParking p) {
    for (var n : not) {
      if (n.matches(p)) {
        return false;
      }
    }
    // not doesn't match and no selects means it matches
    if (select.length == 0) {
      return true;
    }
    for (var s : select) {
      if (s.matches(p)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return ToStringBuilder
      .of(this.getClass())
      .addCol("not", Arrays.asList(not))
      .addCol("select", Arrays.asList(select))
      .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VehicleParkingFilterRequest that = (VehicleParkingFilterRequest) o;
    return (Arrays.equals(not, that.not) && Arrays.equals(select, that.select));
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(not) + Arrays.hashCode(select);
  }

  @Nonnull
  private static VehicleParkingSelect[] makeFilter(Collection<VehicleParkingSelect> select) {
    return select.stream().filter(f -> !f.isEmpty()).toArray(VehicleParkingSelect[]::new);
  }
}
