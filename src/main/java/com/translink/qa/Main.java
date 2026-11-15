
package com.translink.qa;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.translink.qa.client.TransLinkClient;
import com.translink.qa.decoder.FeedDecoder;
import com.translink.qa.model.VehicleInfo;
import com.translink.qa.service.VehicleService;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String routeId = "99";

        try {
            TransLinkClient client = new TransLinkClient();
            FeedDecoder decoder = new FeedDecoder();
            VehicleService service = new VehicleService();

            System.out.println("Connecting to TransLink API...");

            HttpResponse<byte[]> response =
                    client.fetchVehiclePositions();

            System.out.println(
                    "HTTP Status: " + response.statusCode()
            );

            FeedMessage feed = decoder.decode(response.body());

            System.out.println("GTFS protobuf decoded successfully");

            List<VehicleInfo> vehicles =
                    service.getVehiclesByRoute(feed, routeId);

            System.out.println();
            System.out.println("==============================");
            System.out.println("TRANSLINK ROUTE 99 REPORT");
            System.out.println("==============================");
            System.out.println("Route: " + routeId);
            System.out.println("Unique buses: " + vehicles.size());

            if (vehicles.isEmpty()) {
                System.out.println(
                        "No identifiable buses currently reported."
                );
            }

            for (VehicleInfo vehicle : vehicles) {

                System.out.println();
                System.out.println("------------------------------");
                System.out.println(
                        "Vehicle ID: " + vehicle.vehicleId()
                );
                System.out.println(
                        "Route: " + vehicle.routeId()
                );
                System.out.println(
                        "Direction ID: " + vehicle.direction()
                );

                String timestamp = vehicle.timestamp() == null
                        ? "Unavailable"
                        : Instant.ofEpochSecond(
                        vehicle.timestamp()
                ).toString();

                System.out.println(
                        "Last reported: " + timestamp
                );
                System.out.println(
                        "Latitude: " +
                                (vehicle.latitude() == null
                                        ? "Unavailable"
                                        : vehicle.latitude())
                );
                System.out.println(
                        "Longitude: " +
                                (vehicle.longitude() == null
                                        ? "Unavailable"
                                        : vehicle.longitude())
                );
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println(
                    "Request interrupted: " + exception.getMessage()
            );
            System.exit(1);

        } catch (Exception exception) {
            System.err.println(
                    "Application failed: " + exception.getMessage()
            );
            System.exit(1);
        }
    }
}
