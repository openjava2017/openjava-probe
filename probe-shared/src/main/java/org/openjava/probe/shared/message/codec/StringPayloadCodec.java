package org.openjava.probe.shared.message.codec;

import java.nio.charset.StandardCharsets;

public class StringPayloadCodec {
    public static PayloadEncoder<String> getEncoder() {
        return StringPayloadEncoder.INSTANCE;
    }

    public static PayloadDecoder<String> getDecoder() {
        return StringPayloadDecoder.INSTANCE;
    }

    static class StringPayloadEncoder implements PayloadEncoder<String> {
        static final StringPayloadEncoder INSTANCE = new StringPayloadEncoder();
        @Override
        public byte[] encode(String payload) {
            if (payload != null) {
                return payload.getBytes(StandardCharsets.UTF_8);
            }
            return new byte[0];
        }
    }

    static class StringPayloadDecoder implements PayloadDecoder<String> {
        static final StringPayloadDecoder INSTANCE = new StringPayloadDecoder();
        @Override
        public String decode(byte[] payload) {
            if (payload != null && payload.length > 0) {
                return new String(payload, StandardCharsets.UTF_8);
            } else {
                return null;
            }
        }
    }
}
