
package com.translink.qa;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.translink.qa.decoder.FeedDecoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FeedDecoderTest {

    private final FeedDecoder decoder = new FeedDecoder();

    @Test
    void shouldDecodeValidProtobufFeed() throws Exception {

        FeedMessage original = FeedMessage.newBuilder()
                .setHeader(
                        FeedHeader.newBuilder()
                                .setGtfsRealtimeVersion("2.0")
                                .build()
                )
                .build();

        FeedMessage decoded =
                decoder.decode(original.toByteArray());

        assertNotNull(decoded, "Decoded feed must not be null");

        assertTrue(
                decoded.hasHeader(),
                "GTFS-Realtime header must be present"
        );

        assertEquals(
                "2.0",
                decoded.getHeader().getGtfsRealtimeVersion(),
                "GTFS version should match the original feed"
        );

        System.out.println("PASS - Valid GTFS feed decoded");
    }

    @Test
    void shouldRejectEmptyResponse() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> decoder.decode(new byte[0]),
                        "Empty protobuf response must be rejected"
                );

        assertTrue(
                exception.getMessage().contains("empty"),
                "Error message should explain that the response is empty"
        );

        System.out.println("PASS - Empty response rejected");
    }

    @Test
    void shouldRejectInvalidProtobuf() {

        byte[] invalidData = new byte[] {
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0xFF
        };

        assertThrows(
                InvalidProtocolBufferException.class,
                () -> decoder.decode(invalidData),
                "Malformed protobuf data must fail decoding"
        );

        System.out.println("PASS - Invalid protobuf rejected");
    }
}
