
package com.translink.qa;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.translink.qa.client.TransLinkClient;
import com.translink.qa.decoder.FeedDecoder;
import com.translink.qa.model.VehicleInfo;
import com.translink.qa.service.VehicleService;

import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ApiIntegrationTest {

    @Test
    void shouldValidateLiveTransLinkFeed() throws Exception {

        String apiKey = System.getenv("TRANSLINK_API_KEY");

        assumeTrue(
                apiKey != null && !apiKey.isBlank(),
                "TRANSLINK_API_KEY must be configured"
        );

        TransLinkClient client = new TransLinkClient();
        FeedDecoder decoder = new FeedDecoder();
        VehicleService service = new VehicleService();

        // Validation 1: HTTP response

        HttpResponse<byte[]> response =
                client.fetchVehiclePositions();

        assertEquals(
                200,
                response.statusCode(),
                "Expected HTTP 200 from TransLink API"
        );

        assertNotNull(
                response.body(),
                "API response body must not be null"
        );

        assertTrue(
                response.body().length > 0,
                "API response body must not be empty"
        );

        System.out.println("PASS - HTTP response successful");

        // Validation 2: Protobuf decoding

        FeedMessage feed =
                decoder.decode(response.body());

        assertNotNull(
                feed,
                "Decoded FeedMessage must not be null"
        );

        assertTrue(
                feed.hasHeader(),
                "GTFS-Realtime feed must contain a header"
        );

        System.out.println("PASS - Protobuf decoded");

        // Validation 3: Unique Route 99 vehicles

        List<VehicleInfo> vehicles =
                service.getVehiclesByRoute(feed, "99");

        Set<String> vehicleIds = new HashSet<>();

        for (VehicleInfo vehicle : vehicles) {

            assertEquals(
                    "99",
                    vehicle.routeId(),
                    "Incorrect route returned"
            );

            assertNotNull(
                    vehicle.vehicleId(),
                    "Vehicle ID must not be null"
            );

            assertTrue(
                    !vehicle.vehicleId().isBlank(),
                    "Vehicle ID must not be blank"
            );

            assertTrue(
                    vehicleIds.add(vehicle.vehicleId()),
                    "Duplicate vehicle ID: " + vehicle.vehicleId()
            );
        }

        assertEquals(
                vehicleIds.size(),
                vehicles.size(),
                "Vehicle count must equal number of unique IDs"
        );

        System.out.println(
                "PASS - No duplicate Route 99 vehicle IDs"
        );

        System.out.println(
                "Total unique Route 99 buses: " + vehicles.size()
        );
    }
}
