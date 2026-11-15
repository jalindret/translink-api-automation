
package com.translink.qa;

import com.google.transit.realtime.GtfsRealtime.*;
import com.translink.qa.model.VehicleInfo;
import com.translink.qa.service.VehicleService;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleServiceTest {

    private final VehicleService service =
            new VehicleService();

    private FeedEntity createVehicle(
            String entityId,
            String vehicleId,
            String routeId,
            long timestamp) {

        return FeedEntity.newBuilder()
                .setId(entityId)
                .setVehicle(
                        VehiclePosition.newBuilder()
                                .setTrip(
                                        TripDescriptor.newBuilder()
                                                .setRouteId(routeId)
                                )
                                .setVehicle(
                                        VehicleDescriptor.newBuilder()
                                                .setId(vehicleId)
                                )
                                .setTimestamp(timestamp)
                                .setPosition(
                                        Position.newBuilder()
                                                .setLatitude(49.25f)
                                                .setLongitude(-123.12f)
                                )
                )
                .build();
    }

    private FeedMessage createFeed(FeedEntity... entities) {

        FeedMessage.Builder builder =
                FeedMessage.newBuilder()
                        .setHeader(
                                FeedHeader.newBuilder()
                                        .setGtfsRealtimeVersion("2.0")
                        );

        for (FeedEntity entity : entities) {
            builder.addEntity(entity);
        }

        return builder.build();
    }

    @Test
    void shouldFilterRoute99Only() {

        FeedMessage feed = createFeed(
                createVehicle("1", "BUS-101", "99", 1000),
                createVehicle("2", "BUS-102", "99", 1001),
                createVehicle("3", "BUS-103", "25", 1002)
        );

        List<VehicleInfo> vehicles =
                service.getVehiclesByRoute(feed, "99");

        assertEquals(
                2,
                vehicles.size(),
                "Only Route 99 vehicles should be returned"
        );

        assertTrue(
                vehicles.stream()
                        .allMatch(vehicle ->
                                "99".equals(vehicle.routeId())),
                "All returned vehicles must belong to Route 99"
        );

        System.out.println("PASS - Route 99 filtering");
    }

    @Test
    void shouldRemoveDuplicateVehicleIds() {

        FeedMessage feed = createFeed(
                createVehicle("1", "BUS-101", "99", 1000),
                createVehicle("2", "BUS-101", "99", 1005),
                createVehicle("3", "BUS-102", "99", 1001)
        );

        List<VehicleInfo> vehicles =
                service.getVehiclesByRoute(feed, "99");

        Set<String> uniqueIds = new HashSet<>();

        for (VehicleInfo vehicle : vehicles) {

            assertTrue(
                    uniqueIds.add(vehicle.vehicleId()),
                    "Duplicate vehicle ID detected: " +
                            vehicle.vehicleId()
            );
        }

        assertEquals(
                2,
                vehicles.size(),
                "Duplicate records must not increase vehicle count"
        );

        VehicleInfo latestRecord = vehicles.stream()
                .filter(vehicle ->
                        "BUS-101".equals(vehicle.vehicleId()))
                .findFirst()
                .orElseThrow();

        assertEquals(
                1005L,
                latestRecord.timestamp(),
                "Latest timestamp should be retained"
        );

        System.out.println("PASS - Duplicate vehicles removed");
    }

    @Test
    void shouldHandleEmptyRouteResults() {

        FeedMessage feed = createFeed(
                createVehicle("1", "BUS-103", "25", 1000)
        );

        assertEquals(
                0,
                service.countUniqueVehicles(feed, "99"),
                "Route with no matching vehicles should return zero"
        );

        System.out.println("PASS - Empty Route 99 result");
    }

    @Test
    void shouldSkipMissingVehicleId() {

        FeedEntity missingId = FeedEntity.newBuilder()
                .setId("entity-1")
                .setVehicle(
                        VehiclePosition.newBuilder()
                                .setTrip(
                                        TripDescriptor.newBuilder()
                                                .setRouteId("99")
                                )
                                .setVehicle(
                                        VehicleDescriptor.newBuilder()
                                )
                )
                .build();

        FeedMessage feed = createFeed(
                missingId,
                createVehicle("2", "BUS-102", "99", 1000)
        );

        assertEquals(
                1,
                service.countUniqueVehicles(feed, "99"),
                "Records without vehicle IDs must not be counted"
        );

        System.out.println("PASS - Missing vehicle ID handled");
    }

    @Test
    void shouldHandleMissingPositionAndTimestamp() {

        FeedEntity entity = FeedEntity.newBuilder()
                .setId("entity-1")
                .setVehicle(
                        VehiclePosition.newBuilder()
                                .setTrip(
                                        TripDescriptor.newBuilder()
                                                .setRouteId("99")
                                )
                                .setVehicle(
                                        VehicleDescriptor.newBuilder()
                                                .setId("BUS-201")
                                )
                )
                .build();

        List<VehicleInfo> vehicles =
                service.getVehiclesByRoute(
                        createFeed(entity),
                        "99"
                );

        assertEquals(
                1,
                vehicles.size(),
                "Vehicle should still be counted without GPS data"
        );

        VehicleInfo vehicle = vehicles.get(0);

        assertNull(
                vehicle.timestamp(),
                "Missing timestamp should remain null"
        );

        assertNull(
                vehicle.latitude(),
                "Missing latitude should remain null"
        );

        assertNull(
                vehicle.longitude(),
                "Missing longitude should remain null"
        );

        System.out.println(
                "PASS - Missing position and timestamp handled"
        );
    }
}
