
package com.translink.qa.decoder;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

public class FeedDecoder {

    public FeedMessage decode(byte[] data)
            throws InvalidProtocolBufferException {

        if (data == null || data.length == 0) {
            throw new IllegalArgumentException(
                    "Cannot decode an empty protobuf response"
            );
        }

        try {
            FeedMessage feed = FeedMessage.parseFrom(data);

            if (!feed.hasHeader()) {
                throw new IllegalArgumentException(
                        "GTFS feed header is missing"
                );
            }

            return feed;

        } catch (InvalidProtocolBufferException exception) {
            throw new InvalidProtocolBufferException(
                    "Invalid GTFS-Realtime protobuf: "
                            + exception.getMessage()
            );
        }
    }
}
