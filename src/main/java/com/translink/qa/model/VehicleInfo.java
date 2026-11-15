
package com.translink.qa.model;

public record VehicleInfo(
        String vehicleId,
        String routeId,
        String direction,
        Long timestamp,
        Double latitude,
        Double longitude
) {
}
