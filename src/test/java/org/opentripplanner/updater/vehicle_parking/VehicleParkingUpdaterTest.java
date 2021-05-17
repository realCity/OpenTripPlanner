package org.opentripplanner.updater.vehicle_parking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentripplanner.routing.edgetype.StreetVehicleParkingLink;
import org.opentripplanner.routing.edgetype.VehicleParkingEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingService;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingTestBase;
import org.opentripplanner.routing.vertextype.VehicleParkingEntranceVertex;
import org.opentripplanner.updater.GraphUpdater;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.GraphWriterRunnable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class VehicleParkingUpdaterTest extends VehicleParkingTestBase {

  private VehicleParkingDataSource dataSource;
  private VehicleParkingUpdater vehicleParkingUpdater;

  @BeforeEach
  public void setup() {
    initGraph();

    dataSource = Mockito.mock(VehicleParkingDataSource.class);
    when(dataSource.update()).thenReturn(true);

    var parameters = new VehicleParkingUpdaterParameters(null, null, null, null, -1, false, null);
    vehicleParkingUpdater = new VehicleParkingUpdater(parameters, dataSource);
    vehicleParkingUpdater.setup(graph);
  }

  @Test
  public void addVehicleParkingTest() {
    var vehicleParkings = List.of(
        createParingWithEntrances("1", 0.0001, 0)
    );

    when(dataSource.getVehicleParkings()).thenReturn(vehicleParkings);
    runUpdaterOnce();

    assertVehicleParkingsInGraph(1);
  }

  private void assertVehicleParkingsInGraph(int vehicleParkingNumber) {
    var parkingVertices = graph.getVerticesOfType(VehicleParkingEntranceVertex.class);

    assertEquals(vehicleParkingNumber, parkingVertices.size());

    for (var parkingVertex : parkingVertices) {
      assertEquals(2, parkingVertex.getIncoming().size());
      assertEquals(2, parkingVertex.getOutgoing().size());

      assertEquals(
          1,
          parkingVertex.getIncoming().stream().filter(StreetVehicleParkingLink.class::isInstance).count()
      );

      assertEquals(
          1,
          parkingVertex.getIncoming().stream().filter(VehicleParkingEdge.class::isInstance).count()
      );

      assertEquals(
          1,
          parkingVertex.getOutgoing().stream().filter(StreetVehicleParkingLink.class::isInstance).count()
      );

      assertEquals(
          1,
          parkingVertex.getOutgoing().stream().filter(VehicleParkingEdge.class::isInstance).count()
      );
    }

    assertEquals(vehicleParkingNumber, graph.getService(VehicleParkingService.class).getVehicleParkings().count());
  }

  private void runUpdaterOnce() {
    class GraphUpdaterMock extends GraphUpdaterManager {

      public GraphUpdaterMock(
          Graph graph, List<GraphUpdater> updaters
      ) {
        super(graph, updaters);
      }

      @Override
      public void execute(GraphWriterRunnable runnable) {
        runnable.run(graph);
      }
    }

    var graphUpdaterManager = new GraphUpdaterMock(graph, List.of(vehicleParkingUpdater));
    graphUpdaterManager.startUpdaters();
    graphUpdaterManager.stop();
  }

  @Test
  public void updateVehicleParkingTest() {
    var vehiclePlaces = VehicleParking.VehiclePlaces.builder()
        .bicycleSpaces(1)
        .build();

    var vehicleParkings = List.of(
        createParingWithEntrances("1", 0.0001, 0, vehiclePlaces)
    );

    when(dataSource.getVehicleParkings()).thenReturn(vehicleParkings);
    runUpdaterOnce();

    assertVehicleParkingsInGraph(1);

    var vehicleParkingInGraph = graph.getService(VehicleParkingService.class).getVehicleParkings().findFirst().orElseThrow();
    assertEquals(vehiclePlaces, vehicleParkingInGraph.getAvailability());
    assertEquals(vehiclePlaces, vehicleParkingInGraph.getCapacity());

    vehiclePlaces = VehicleParking.VehiclePlaces.builder()
        .bicycleSpaces(2)
        .build();
    vehicleParkings = List.of(
        createParingWithEntrances("1", 0.0001, 0, vehiclePlaces)
    );

    when(dataSource.getVehicleParkings()).thenReturn(vehicleParkings);
    runUpdaterOnce();

    assertVehicleParkingsInGraph(1);

    vehicleParkingInGraph = graph.getService(VehicleParkingService.class).getVehicleParkings().findFirst().orElseThrow();
    assertEquals(vehiclePlaces, vehicleParkingInGraph.getAvailability());
    assertEquals(vehiclePlaces, vehicleParkingInGraph.getCapacity());
  }

  @Test
  public void deleteVehicleParkingTest() {
    var vehicleParkings = List.of(
        createParingWithEntrances("1", 0.0001, 0),
        createParingWithEntrances("2", -0.0001, 0)
    );

    when(dataSource.getVehicleParkings()).thenReturn(vehicleParkings);
    runUpdaterOnce();

    assertVehicleParkingsInGraph(2);

    vehicleParkings = List.of(createParingWithEntrances("1", 0.0001, 0));

    when(dataSource.getVehicleParkings()).thenReturn(vehicleParkings);
    runUpdaterOnce();

    assertVehicleParkingsInGraph(1);
  }

}
