
package com.translink.qa.client;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class TransLinkClient {

    private static final String BASE_URL =
            "https://gtfsapi.translink.ca/v3/gtfsposition?apikey=";

    private final HttpClient client;

    public TransLinkClient() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public HttpResponse<byte[]> fetchVehiclePositions()
            throws IOException, InterruptedException {

        String apiKey = System.getenv("TRANSLINK_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "TRANSLINK_API_KEY is missing. " +
                            "Configure it in IntelliJ environment variables."
            );
        }

        String url = BASE_URL + URLEncoder.encode(
                apiKey, StandardCharsets.UTF_8
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/x-protobuf")
                .GET()
                .build();

        HttpResponse<byte[]> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "TransLink API returned HTTP " +
                            response.statusCode()
            );
        }

        if (response.body() == null ||
                response.body().length == 0) {
            throw new IOException(
                    "TransLink API returned an empty response"
            );
        }

        return response;
    }
}
