package org.opentripplanner.routing.algorithm.raptoradapter.transit;

import java.time.LocalDate;
import org.opentripplanner.framework.model.TimeAndCost;
import org.opentripplanner.framework.tostring.ToStringBuilder;
import org.opentripplanner.raptor.api.model.RaptorConstants;
import org.opentripplanner.raptor.api.model.RaptorTripSchedule;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.cost.RaptorCostConverter;
import org.opentripplanner.street.search.state.State;
import org.opentripplanner.transit.model.network.TripPattern;
import org.opentripplanner.transit.model.timetable.TripTimes;

public class OnBoardAccess implements RoutingAccessEgress {

  private final TripSchedule tripSchedule;

  private final int fromStopPos;

  private final int toStopPos;

  private final int toStopId;

  private final int departureTime;

  private final int arrivalTime;

  private final int duration;

  private final int cost;

  private final TimeAndCost penalty;

  public OnBoardAccess(
    TripSchedule tripSchedule,
    int fromStopPos,
    int toStopPos,
    int departureTime,
    int arrivalTime,
    int duration,
    double cost
  ) {
    this.tripSchedule = tripSchedule;
    this.fromStopPos = fromStopPos;
    this.toStopPos = toStopPos;
    this.departureTime = departureTime;
    this.arrivalTime = arrivalTime;
    this.duration = duration;
    this.cost = RaptorCostConverter.toRaptorCost(cost);
    this.toStopId = tripSchedule.pattern().stopIndex(toStopPos);
    this.penalty = null;
  }

  public OnBoardAccess(OnBoardAccess that, TimeAndCost penalty) {
    this.tripSchedule = that.tripSchedule;
    this.fromStopPos = that.fromStopPos;
    this.toStopPos = that.toStopPos;
    this.departureTime = that.departureTime;
    this.arrivalTime = that.arrivalTime;
    this.duration = that.duration;
    this.cost = that.cost;
    this.toStopId = that.toStopId;
    this.penalty = penalty;
  }

  @Override
  public int stop() {
    return toStopId;
  }

  @Override
  public int c1() {
    return 0;
  }

  @Override
  public int durationInSeconds() {
    return duration;
  }

  @Override
  public int numberOfRides() {
    return 1;
  }

  @Override
  public boolean stopReachedOnBoard() {
    return true;
  }

  @Override
  public RaptorTripSchedule stopReachedOnBoardTripSchedule() {
    return tripSchedule;
  }

  @Override
  public Integer stopReachedOnBoardBoardingStopPos() {
    return fromStopPos;
  }

  @Override
  public Integer stopReachedOnBoardAlightingStopPos() {
    return toStopPos;
  }

  @Override
  public int earliestDepartureTime(int requestedDepartureTime) {
    if (requestedDepartureTime > departureTime) {
      return RaptorConstants.TIME_NOT_SET;
    }
    return departureTime;
  }

  @Override
  public int latestArrivalTime(int requestedArrivalTime) {
    if (requestedArrivalTime < arrivalTime) {
      return RaptorConstants.TIME_NOT_SET;
    }
    return arrivalTime;
  }

  @Override
  public boolean hasOpeningHours() {
    return true;
  }

  public TripPattern getTripPattern() {
    return tripSchedule.getOriginalTripPattern();
  }

  public TripTimes getTripTimes() {
    return tripSchedule.getOriginalTripTimes();
  }

  public LocalDate getServiceDate() {
    return tripSchedule.getServiceDate();
  }

  public int getDepartureTime() {
    return tripSchedule.departure(fromStopPos);
  }

  public int getArrivalTime() {
    return tripSchedule.arrival(toStopPos);
  }

  @Override
  public RoutingAccessEgress withPenalty(TimeAndCost penalty) {
    return new OnBoardAccess(this, penalty);
  }

  @Override
  public State getLastState() {
    return null;
  }

  @Override
  public boolean isWalkOnly() {
    return false;
  }

  @Override
  public boolean hasPenalty() {
    return penalty != null;
  }

  @Override
  public TimeAndCost penalty() {
    return penalty;
  }

  @Override
  public String toString() {
    return ToStringBuilder
      .of(getClass())
      .addNum("fromStopIndex", fromStopPos)
      .addNum("toStopIndex", toStopPos)
      .addDurationSec("arrivalTime", arrivalTime)
      .addCost("cost", cost, null)
      .toString();
  }
}
