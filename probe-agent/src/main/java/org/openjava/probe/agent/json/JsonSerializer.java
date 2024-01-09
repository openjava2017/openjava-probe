package org.openjava.probe.agent.json;

public interface JsonSerializer<T> {
    void serialize(T value, JsonWriter writer, SerializerProvider factory);
}
