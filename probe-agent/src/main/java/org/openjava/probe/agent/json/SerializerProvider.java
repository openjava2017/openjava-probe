package org.openjava.probe.agent.json;

public interface SerializerProvider extends SerializerFactory, Iterable<SerializerFactory> {
    void addFirst(SerializerFactory serializerFactory);

    void addLast(SerializerFactory serializerFactory);

    void serialize(Object object, JsonWriter writer);
}
