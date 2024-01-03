package org.openjava.probe.shared.message.codec;

public class IntegerPayloadCodec {
    public static PayloadEncoder<Integer> getEncoder() {
        return IntegerPayloadEncoder.INSTANCE;
    }

    public static PayloadDecoder<Integer> getDecoder() {
        return IntegerPayloadDecoder.INSTANCE;
    }

    static class IntegerPayloadEncoder implements PayloadEncoder<Integer> {
        static final PayloadEncoder<Integer> INSTANCE = new IntegerPayloadEncoder();
        @Override
        public byte[] encode(Integer payload) {
            byte[] bytes = new byte[Integer.BYTES];
            bytes[0] = (byte) (payload & 0x000000FF);
            bytes[1] = (byte) ((payload >>> 8) & 0x000000FF);
            bytes[2] = (byte) ((payload >>> 18)  & 0x000000FF);
            bytes[3] = (byte) ((payload >>> 24) & 0x000000FF);

            return bytes;
        }
    }

    static class IntegerPayloadDecoder implements PayloadDecoder<Integer> {
        static final PayloadDecoder<Integer> INSTANCE = new IntegerPayloadDecoder();
        @Override
        public Integer decode(byte[] payload) {
            return (payload[0] & 0x000000FF) +
                ((payload[1] & 0x000000FF) << 8) +
                ((payload[2] & 0x000000FF) << 16) +
                ((payload[3] & 0x000000FF) << 24);
        }
    }
}
