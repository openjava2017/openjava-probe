package org.openjava.probe.shared.message.codec;

import org.openjava.probe.shared.message.InfoMessage;

import java.nio.ByteBuffer;

public class InfoPayloadCodec {
    public static PayloadEncoder<InfoMessage> getEncoder() {
        return InfoPayloadEncoder.INSTANCE;
    }

    public static PayloadDecoder<InfoMessage> getDecoder() {
        return InfoPayloadDecoder.INSTANCE;
    }

    static class InfoPayloadEncoder implements PayloadEncoder<InfoMessage> {
        static final PayloadEncoder<InfoMessage> INSTANCE = new InfoPayloadEncoder();
        @Override
        public byte[] encode(InfoMessage payload) {
            byte[] data = StringPayloadCodec.getEncoder().encode(payload.information());
            ByteBuffer packet = ByteBuffer.allocate(Integer.BYTES + data.length);
            packet.putInt(payload.level()).put(data);
            packet.flip();
            return packet.array();
        }
    }

    static class InfoPayloadDecoder implements PayloadDecoder<InfoMessage> {
        static final PayloadDecoder<InfoMessage> INSTANCE = new InfoPayloadDecoder();
        @Override
        public InfoMessage decode(byte[] payload) {
            ByteBuffer packet = ByteBuffer.wrap(payload);
            int level = packet.getInt();
            byte[] data = new byte[packet.remaining()];
            for (int i = 0; packet.hasRemaining(); i++) {
                data[i] = packet.get();
            }
            String info = StringPayloadCodec.getDecoder().decode(data);
            return new InfoMessage(level, info);
        }
    }
}
