package org.openjava.probe.agent.json;

public interface SerializerFactory {
    JsonSerializer<?> createSerializer(Class clazz);
}
