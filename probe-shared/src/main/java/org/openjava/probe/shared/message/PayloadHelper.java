package org.openjava.probe.shared.message;

import java.nio.charset.StandardCharsets;

public class PayloadHelper {
    public static final PayloadDecoder<String> STRING_DECODER = new StringPayloadDecoder();
    public static final PayloadDecoder<String> NONE_DECODER = new StringPayloadDecoder();
    public static final PayloadEncoder<String> STRING_ENCODER = new StringPayloadEncoder();

    static class StringPayloadDecoder implements PayloadDecoder<String> {
        @Override
        public String decode(byte[] payload) {
            if (payload != null && payload.length > 0) {
                return new String(payload, StandardCharsets.UTF_8);
            } else {
                return null;
            }
        }
    }

    static class NonePayloadDecoder implements PayloadDecoder<byte[]> {
        @Override
        public byte[] decode(byte[] payload) {
            if (payload == null) {
                return new byte[0];
            }
            return payload;
        }
    }

    static class StringPayloadEncoder implements PayloadEncoder<String> {
        @Override
        public byte[] encode(String payload) {
            if (payload != null) {
                return payload.getBytes(StandardCharsets.UTF_8);
            }
            return new byte[0];
        }
    }
}
