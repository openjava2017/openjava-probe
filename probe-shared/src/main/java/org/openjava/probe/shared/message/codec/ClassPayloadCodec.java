package org.openjava.probe.shared.message.codec;

import org.openjava.probe.shared.message.DumpClass;

import java.nio.ByteBuffer;

public class ClassPayloadCodec {
    public static PayloadEncoder<DumpClass> getEncoder() {
        return ClassPayloadEncoder.INSTANCE;
    }

    public static PayloadDecoder<DumpClass> getDecoder() {
        return ClassPayloadDecoder.INSTANCE;
    }

    static class ClassPayloadEncoder implements PayloadEncoder<DumpClass> {
        static final PayloadEncoder<DumpClass> INSTANCE = new ClassPayloadEncoder();
        @Override
        public byte[] encode(DumpClass payload) {
            byte[] data = StringPayloadCodec.getEncoder().encode(payload.name());
            byte[] classBytes = payload.classBytes();
            ByteBuffer packet = ByteBuffer.allocate(Integer.BYTES + data.length + classBytes.length);
            packet.putInt(data.length).put(data).put(classBytes);
            packet.flip();
            return packet.array();
        }
    }

    static class ClassPayloadDecoder implements PayloadDecoder<DumpClass> {
        static final PayloadDecoder<DumpClass> INSTANCE = new ClassPayloadDecoder();
        @Override
        public DumpClass decode(byte[] payload) {
            ByteBuffer packet = ByteBuffer.wrap(payload);
            int length = packet.getInt();
            byte[] nameBytes = new byte[length];
            packet.get(nameBytes);
            byte[] classBytes = new byte[packet.remaining()];
            packet.get(classBytes);

            String name = StringPayloadCodec.getDecoder().decode(nameBytes);
            return DumpClass.of(name, classBytes);
        }
    }
}
