package org.opentripplanner.routing.api.request;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.opentripplanner.model.TransitMode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RequestModes {

  public Set<StreetMode> accessModes;
  public StreetMode transferMode;
  public Set<StreetMode> egressModes;
  public Set<StreetMode> directModes;
  public Set<TransitMode> transitModes;

  public static RequestModes defaultRequestModes = new RequestModes(
      StreetMode.WALK,
      StreetMode.WALK,
      StreetMode.WALK,
      StreetMode.WALK,
      new HashSet<>(Arrays.asList(TransitMode.values()))
  );

  public RequestModes(
          StreetMode accessMode,
          StreetMode transferMode,
          StreetMode egressMode,
          StreetMode directMode,
          Set<TransitMode> transitModes
  ) {
    this(
            accessMode != null ? Set.of(accessMode) : Set.of(),
            transferMode,
            egressMode != null ? Set.of(egressMode) : Set.of(),
            directMode != null ? Set.of(directMode) : Set.of(),
            transitModes
    );
  }

  public RequestModes(
      Set<StreetMode> accessModes,
      StreetMode transferMode,
      Set<StreetMode> egressModes,
      Set<StreetMode> directModes,
      Set<TransitMode> transitModes
  ) {
    this.accessModes = setOf(accessModes, m -> m.access);
    this.transferMode = (transferMode != null && transferMode.transfer) ? transferMode : null;
    this.egressModes = setOf(egressModes, m -> m.egress);
    this.directModes = setOf(directModes, m -> true);
    this.transitModes = transitModes;
  }

  public boolean contains(StreetMode streetMode) {
    return
        accessModes.contains(streetMode)
            || egressModes.contains(streetMode)
            || directModes.contains(streetMode);
  }

  private static Set<StreetMode> setOf(Collection<StreetMode> modes, Predicate<StreetMode> predicate) {
    if (modes == null) {
      return Set.of();
    }

    return modes.stream()
            .filter(predicate)
            .collect(Collectors.toCollection(HashSet::new));
  }
}
