package org.opentripplanner.routing.algorithm.raptoradapter.router;

import static org.mockito.Mockito.when;
import static org.opentripplanner.raptor._data.transit.TestTripSchedule.schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentripplanner.framework.application.OTPFeature;
import org.opentripplanner.framework.geometry.WgsCoordinate;
import org.opentripplanner.model.GenericLocation;
import org.opentripplanner.model.PickDrop;
import org.opentripplanner.model.StopTime;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.api.model.RaptorTripPattern;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TripSchedule;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.request.RaptorRoutingRequestTransitData;
import org.opentripplanner.routing.api.request.RouteRequest;
import org.opentripplanner.transit.model._data.TransitModelForTest;
import org.opentripplanner.transit.model.basic.Accessibility;
import org.opentripplanner.transit.model.framework.FeedScopedId;
import org.opentripplanner.transit.model.network.StopPattern;
import org.opentripplanner.transit.model.network.TripPattern;
import org.opentripplanner.transit.model.site.RegularStop;
import org.opentripplanner.transit.model.timetable.Trip;
import org.opentripplanner.transit.model.timetable.TripTimes;
import org.opentripplanner.transit.service.TransitService;

class OnboardAccessCalculatorTest {

  private static final TransitModelForTest TEST_MODEL = TransitModelForTest.of();

  private final LocalDate serviceDate = LocalDate.of(2022, 3, 23);
  private final FeedScopedId tripId = new FeedScopedId("TEST", "Trip-1");

  private final Trip trip;
  private final TransitService transitService;
  private final ZonedDateTime transitSearchTimeZero;

  public OnboardAccessCalculatorTest() {
    trip = TransitModelForTest.trip("trip").build();

    transitService = mockTransitService();

    transitSearchTimeZero = ZonedDateTime.of(serviceDate, LocalTime.MIDNIGHT, ZoneOffset.UTC);
  }

  @BeforeAll
  public static void enableFeature() {
    OTPFeature.enableFeatures(Map.of(OTPFeature.OnBoardAccessEgress, true));
  }

  @AfterAll
  public static void disableFeature() {
    OTPFeature.enableFeatures(Map.of(OTPFeature.OnBoardAccessEgress, false));
  }

  @Test
  public void onBoardAccessGeneratorTest() {
    var testTripSchedule = schedule("00:05 00:10 00:15 00:20 00:25").build();

    var requestTransitDataProvider = mockRaptorRoutingRequestTransitData(testTripSchedule);

    var routingRequest = createTestRequest(LocalTime.of(0, 12));

    var onBoardAccessList = OnboardAccessCalculator.getOnboardAccessEgress(
      transitService,
      requestTransitDataProvider,
      transitSearchTimeZero,
      routingRequest
    );

    Assertions.assertEquals(
      "[" +
      "OnBoardAccess{fromStopIndex: 1, toStopIndex: 2, arrivalTime: 15m, cost: $0}, " +
      "OnBoardAccess{fromStopIndex: 1, toStopIndex: 3, arrivalTime: 20m, cost: $30000}, " +
      "OnBoardAccess{fromStopIndex: 1, toStopIndex: 4, arrivalTime: 25m, cost: $60000}" +
      "]",
      onBoardAccessList.toString()
    );
  }

  @Test
  public void onBoardAccessBeforeStartOfTripTest() {
    var testTripSchedule = schedule("00:05 00:10").build();

    var requestTransitDataProvider = mockRaptorRoutingRequestTransitData(testTripSchedule);

    var routingRequest = createTestRequest(LocalTime.of(0, 2));

    var onBoardAccessList = OnboardAccessCalculator.getOnboardAccessEgress(
      transitService,
      requestTransitDataProvider,
      transitSearchTimeZero,
      routingRequest
    );

    Assertions.assertEquals(
      "[" + "OnBoardAccess{fromStopIndex: 0, toStopIndex: 1, arrivalTime: 10m, cost: $0}" + "]",
      onBoardAccessList.toString()
    );
  }

  @Test
  public void onBoardAccessAfterEndOfTripTest() {
    var testTripSchedule = schedule("00:05 00:10").build();

    var requestTransitDataProvider = mockRaptorRoutingRequestTransitData(testTripSchedule);

    var routingRequest = createTestRequest(LocalTime.of(0, 12));

    var onBoardAccessList = OnboardAccessCalculator.getOnboardAccessEgress(
      transitService,
      requestTransitDataProvider,
      transitSearchTimeZero,
      routingRequest
    );

    Assertions.assertEquals(
      "[" + "OnBoardAccess{fromStopIndex: 0, toStopIndex: 1, arrivalTime: 12m, cost: $0}" + "]",
      onBoardAccessList.toString()
    );
  }

  @Test
  public void planningFromStopTest() {
    var testTripSchedule = schedule("00:05 00:10 00:15 00:20 00:25").build();

    var requestTransitDataProvider = mockRaptorRoutingRequestTransitData(testTripSchedule);

    var routingRequest = createTestRequest(LocalTime.of(0, 15));

    var onBoardAccessList = OnboardAccessCalculator.getOnboardAccessEgress(
      transitService,
      requestTransitDataProvider,
      transitSearchTimeZero,
      routingRequest
    );

    Assertions.assertEquals(
      "[" +
      "OnBoardAccess{fromStopIndex: 1, toStopIndex: 2, arrivalTime: 15m, cost: $0}, " +
      "OnBoardAccess{fromStopIndex: 1, toStopIndex: 3, arrivalTime: 20m, cost: $30000}, " +
      "OnBoardAccess{fromStopIndex: 1, toStopIndex: 4, arrivalTime: 25m, cost: $60000}" +
      "]",
      onBoardAccessList.toString()
    );
  }

  @Test
  public void planningFromStopWhenArrivalAndDepartureTimesAreDifferent() {
    var testTripSchedule = new TestTripSchedule.Builder()
      .arrivals("00:09 00:12 00:15")
      .departures("00:09 00:13 00:15")
      .build();

    var requestTransitDataProvider = mockRaptorRoutingRequestTransitData(testTripSchedule);

    var routingRequest = createTestRequest(LocalTime.of(0, 12));

    var onBoardAccessList = OnboardAccessCalculator.getOnboardAccessEgress(
      transitService,
      requestTransitDataProvider,
      transitSearchTimeZero,
      routingRequest
    );

    Assertions.assertEquals(
      "[" +
      "OnBoardAccess{fromStopIndex: 0, toStopIndex: 1, arrivalTime: 12m, cost: $0}, " +
      "OnBoardAccess{fromStopIndex: 0, toStopIndex: 2, arrivalTime: 15m, cost: $18000}" +
      "]",
      onBoardAccessList.toString()
    );
  }

  private TransitService mockTransitService() {
    var transitService = Mockito.mock(TransitService.class);
    when(transitService.getTripForId(tripId)).thenReturn(trip);
    return transitService;
  }

  private RaptorRoutingRequestTransitData mockRaptorRoutingRequestTransitData(
    TestTripSchedule testTripSchedule
  ) {
    var data = Mockito.mock(RaptorRoutingRequestTransitData.class);

    var raptorTripSchedule = new TestRaptorTripSchedule(trip, testTripSchedule, serviceDate);

    when(data.getTripScheduleForTrip(trip, serviceDate))
      .thenReturn(Optional.of(raptorTripSchedule));

    return data;
  }

  private static TripPattern mockTripPattern(Trip trip, TestTripSchedule testTripSchedule) {
    var stopTimes = IntStream
      .range(0, testTripSchedule.size())
      .mapToObj(i -> {
        var stopTime = new StopTime();
        stopTime.setPickupType(PickDrop.SCHEDULED);
        stopTime.setDropOffType(PickDrop.SCHEDULED);
        stopTime.setStop(stopForTest(Integer.toString(i), Accessibility.NO_INFORMATION, i, i));
        return stopTime;
      })
      .collect(Collectors.toList());

    var stopPattern = new StopPattern(stopTimes);

    return TripPattern
      .of(new FeedScopedId("TEST", "trip-pattern"))
      .withStopPattern(stopPattern)
      .withRoute(trip.getRoute())
      .build();
  }

  private RouteRequest createTestRequest(LocalTime requestTime) {
    var routingRequest = new RouteRequest();
    routingRequest.setFrom(GenericLocation.forTrip("", tripId, serviceDate));
    routingRequest.setDateTime(
      ZonedDateTime.of(serviceDate, requestTime, ZoneOffset.UTC).toInstant()
    );

    return routingRequest;
  }

  private static class TestRaptorTripSchedule implements TripSchedule {

    private final LocalDate serviceDate;
    private final TestTripSchedule tripSchedule;
    private final TripPattern tripPattern;

    private TestRaptorTripSchedule(
      Trip trip,
      TestTripSchedule tripSchedule,
      LocalDate serviceDate
    ) {
      this.serviceDate = serviceDate;
      this.tripSchedule = tripSchedule;
      tripPattern = mockTripPattern(trip, tripSchedule);
    }

    @Override
    public TripTimes getOriginalTripTimes() {
      return null;
    }

    @Override
    public TripPattern getOriginalTripPattern() {
      return tripPattern;
    }

    @Override
    public LocalDate getServiceDate() {
      return serviceDate;
    }

    @Override
    public int tripSortIndex() {
      return 0;
    }

    @Override
    public int arrival(int stopPosInPattern) {
      return tripSchedule.arrival(stopPosInPattern);
    }

    @Override
    public int departure(int stopPosInPattern) {
      return tripSchedule.departure(stopPosInPattern);
    }

    @Override
    public RaptorTripPattern pattern() {
      return tripSchedule.pattern();
    }

    @Override
    public int transitReluctanceFactorIndex() {
      return 0;
    }

    @Override
    public Accessibility wheelchairBoarding() {
      return null;
    }
  }

  public static RegularStop stopForTest(
    String idAndName,
    Accessibility wheelchair,
    double lat,
    double lon
  ) {
    return TEST_MODEL
      .stop(idAndName)
      .withCoordinate(new WgsCoordinate(lat, lon))
      .withWheelchairAccessibility(wheelchair)
      .build();
  }
}
