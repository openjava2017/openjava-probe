package org.openjava.probe.client.env;

public interface Environment {
    String getProperty(String key);

    String getProperty(String key, String defaultValue);

    <T> T getProperty(String key, Class<T> targetType);

    <T> T getProperty(String key, Class<T> targetType, T defaultValue);

    String getRequiredProperty(String key) throws IllegalArgumentException;

   <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalArgumentException;
}
