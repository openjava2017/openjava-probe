package org.openjava.probe.agent.json;

public final class JsonMapper {
    private static final SerializerProvider provider = new JsonSerializerProvider();

    public static <T> String toJson(T object) {
        JsonWriter writer = new DefaultJsonWriter();
        provider.serialize(object, writer);
        return writer.toString();
    }
}
