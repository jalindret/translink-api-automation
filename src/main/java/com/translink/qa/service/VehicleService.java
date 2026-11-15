
package com.translink.qa.service;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.translink.qa.model.VehicleInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VehicleService {

    public List<VehicleInfo> getVehiclesByRoute(
            FeedMessage feed, String routeId) {

        if (feed == null) {
            throw new IllegalArgumentException(
                    "GTFS feed cannot be null"
            );
        }

        if (routeId == null || routeId.isBlank()) {
            throw new IllegalArgumentException(
                    "Route ID cannot be empty"
            );
        }

        Map<String, VehicleInfo> uniqueVehicles =
                new LinkedHashMap<>();

        for (FeedEntity entity : feed.getEntityList()) {

            if (!entity.hasVehicle()) {
                continue;
            }

            VehiclePosition vehicle = entity.getVehicle();

            if (!vehicle.hasTrip()
                    || !vehicle.getTrip().hasRouteId()) {
                continue;
            }

            String currentRoute =
                    vehicle.getTrip().getRouteId();

            if (!routeId.equals(currentRoute)) {
                continue;
            }

            if (!vehicle.hasVehicle()
                    || !vehicle.getVehicle().hasId()) {
                continue;
            }

            String vehicleId =
                    vehicle.getVehicle().getId().trim();

            if (vehicleId.isEmpty()) {
                continue;
            }

            String direction = "Unavailable";

            if (vehicle.getTrip().hasDirectionId()) {
                direction = String.valueOf(
                        vehicle.getTrip().getDirectionId()
                );
            }

            Long timestamp = vehicle.hasTimestamp()
                    ? vehicle.getTimestamp()
                    : null;

            Double latitude = null;
            Double longitude = null;

            if (vehicle.hasPosition()) {

                if (vehicle.getPosition().hasLatitude()) {
                    latitude = (double)
                            vehicle.getPosition().getLatitude();
                }

                if (vehicle.getPosition().hasLongitude()) {
                    longitude = (double)
                            vehicle.getPosition().getLongitude();
                }
            }

            VehicleInfo info = new VehicleInfo(
                    vehicleId,
                    currentRoute,
                    direction,
                    timestamp,
                    latitude,
                    longitude
            );

            VehicleInfo existing =
                    uniqueVehicles.get(vehicleId);

            if (existing == null ||
                    isNewer(info, existing)) {
                uniqueVehicles.put(vehicleId, info);
            }
        }

        return new ArrayList<>(uniqueVehicles.values());
    }

    private boolean isNewer(
            VehicleInfo candidate, VehicleInfo existing) {

        if (candidate.timestamp() == null) {
            return false;
        }

        if (existing.timestamp() == null) {
            return true;
        }

        return candidate.timestamp() >
                existing.timestamp();
    }

    public int countUniqueVehicles(
            FeedMessage feed, String routeId) {

        return getVehiclesByRoute(feed, routeId).size();
    }
}
