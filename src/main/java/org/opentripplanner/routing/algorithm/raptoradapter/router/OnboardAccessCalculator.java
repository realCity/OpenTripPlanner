package org.opentripplanner.routing.algorithm.raptoradapter.router;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.OnBoardAccess;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TripSchedule;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.request.RaptorRoutingRequestTransitData;
import org.opentripplanner.routing.api.request.RouteRequest;
import org.opentripplanner.transit.service.TransitService;

public class OnboardAccessCalculator {

  private OnboardAccessCalculator() {}

  public static Collection<OnBoardAccess> getOnboardAccessEgress(
    TransitService index,
    RaptorRoutingRequestTransitData requestTransitDataProvider,
    ZonedDateTime transitSearchTimeZero,
    RouteRequest request
  ) {
    var trip = index.getTripForId(request.from().tripId);
    var serviceDate = request.from().serviceDate;
    if (trip == null) {
      return List.of();
    }

    var tripScheduleOptional = requestTransitDataProvider.getTripScheduleForTrip(trip, serviceDate);
    if (tripScheduleOptional.isEmpty()) {
      return List.of();
    }

    var tripSchedule = tripScheduleOptional.get();
    var mode = tripScheduleOptional.get().getOriginalTripPattern().getMode();
    var transitReluctance = request
      .preferences()
      .transit()
      .reluctanceForMode()
      .getOrDefault(mode, 1.);
    var alightSlack = request.preferences().transit().alightSlack().valueOf(mode);

    return getOnboardAccess(
      tripSchedule,
      transitSearchTimeZero.toInstant(),
      request.dateTime(),
      alightSlack,
      transitReluctance
    );
  }

  // TODO: where should alightSlack be handled
  private static Collection<OnBoardAccess> getOnboardAccess(
    TripSchedule tripSchedule,
    Instant transitSearchTimeZero,
    Instant requestDateTime,
    Duration alightSlack,
    double transitReluctance
  ) {
    var requestOffset = (int) Duration.between(transitSearchTimeZero, requestDateTime).toSeconds();

    var tripPattern = tripSchedule.getOriginalTripPattern();

    if (tripSchedule.arrival(tripPattern.numberOfStops() - 1) < requestOffset) {
      return List.of(
        new OnBoardAccess(
          tripSchedule,
          tripPattern.numberOfStops() - 2,
          tripPattern.numberOfStops() - 1,
          requestOffset,
          requestOffset,
          0,
          0
        )
      );
    }

    // Find the first stop where alighting is possible
    int firstStopIndex = tripPattern.numberOfStops() - 1;
    while (tripSchedule.arrival(firstStopIndex - 1) >= requestOffset) {
      if (firstStopIndex > 1) {
        firstStopIndex--;
      } else {
        break;
      }
    }

    var fromStopIndex = Math.max(0, firstStopIndex - 1);
    var firstStopArrivalTime = tripSchedule.arrival(firstStopIndex);

    List<OnBoardAccess> onBoardTransfers = new ArrayList<>();
    for (
      int toStopIndex = firstStopIndex;
      toStopIndex < tripPattern.numberOfStops();
      toStopIndex++
    ) {
      if (!tripPattern.canAlight(toStopIndex)) {
        continue;
      }

      var arriveTime = tripSchedule.arrival(toStopIndex); // + alightSlack;
      int duration = arriveTime - firstStopArrivalTime;
      var onBoardAccess = new OnBoardAccess(
        tripSchedule,
        fromStopIndex,
        toStopIndex,
        firstStopArrivalTime,
        arriveTime,
        duration,
        duration * transitReluctance
      );
      onBoardTransfers.add(onBoardAccess);
    }

    return onBoardTransfers;
  }

  public static boolean isRequestOnBoardAccess(RouteRequest request) {
    return (
      request.from().tripId != null && request.from().serviceDate != null && !request.arriveBy()
    );
  }
}
