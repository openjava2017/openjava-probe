package org.openjava.probe.shared.message;

import org.openjava.probe.shared.message.codec.InfoPayloadCodec;
import org.openjava.probe.shared.message.codec.PayloadDecoder;
import org.openjava.probe.shared.message.codec.PayloadEncoder;

import java.nio.ByteBuffer;

public class Message {
    private final int header;
    private final byte[] payload;

    private Message(int header, byte[] payload) {
        this.header = header;
        this.payload = payload;
    }

    public static Message of(MessageHeader header, byte[] payload) {
        return new Message(header.getCode(), payload);
    }

    public static Message info(String payload) {
        return of(MessageHeader.INFO_MESSAGE, InfoMessage.of(InfoMessage.INFO_LEVEL, payload), InfoPayloadCodec.getEncoder());
    }

    public static Message error(String payload) {
        return of(MessageHeader.INFO_MESSAGE, InfoMessage.of(InfoMessage.ERROR_LEVEL, payload), InfoPayloadCodec.getEncoder());
    }

    public static Message of(MessageHeader header, String payload, PayloadEncoder<String> encoder) {
        return of(header, encoder.encode(payload));
    }

    public static <T> Message of(MessageHeader header, T payload, PayloadEncoder<T> encoder) {
        return of(header, encoder.encode(payload));
    }

    public static Message from(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int header = byteBuffer.getInt();
        byte[] data = new byte[0];
        if (byteBuffer.remaining() > 0) {
            data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);
//            for (int i = 0; byteBuffer.hasRemaining(); i++) {
//                data[i] = byteBuffer.get();
//            }
        }
        byteBuffer.clear();
        return new Message(header, data);
    }

    public byte[] toBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES + payload.length);
        byteBuffer.putInt(header);
        byteBuffer.put(payload);
        byteBuffer.flip();
        return byteBuffer.array();
    }

    public int header() {
        return this.header;
    }

    public byte[] payload() {
        return this.payload;
    }

    public <T> T payload(PayloadDecoder<T> decoder) {
        return decoder.decode(this.payload);
    }
}
