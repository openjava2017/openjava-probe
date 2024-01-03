package org.openjava.probe.client.context;

import org.openjava.probe.shared.property.*;

import java.util.LinkedList;
import java.util.Map;

public class UserEnvironment implements Environment {

    private static final String CLIENT_ARGUMENT_SOURCE_NAME = "clientArgumentProperties";
    private static final String CLIENT_CONFIG_SOURCE_NAME = "clientConfigProperties";
    private static final String PROPERTIES_PROPERTY_SOURCE_NAME = "systemProperties";
    private static final String ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment";
    private static final String LIST_PROPERTY_SOURCE_NAME = "listPropertySource";
    private static final String CLIENT_CONFIG_FILE_PATH = "org/openjava/probe/client/config/ProbeClient.properties";

    private final ListPropertySource propertySource = new ListPropertySource(LIST_PROPERTY_SOURCE_NAME, new LinkedList<>());
    private final PropertyResolver propertyResolver = new UserPropertyResolver(propertySource);

    public UserEnvironment(String args) {
        propertySource.addLast(new StringPropertySource(CLIENT_ARGUMENT_SOURCE_NAME, args));
        propertySource.addLast(new FilePathPropertySource(CLIENT_CONFIG_SOURCE_NAME, CLIENT_CONFIG_FILE_PATH));
        propertySource.addLast(new PropertiesPropertySource(PROPERTIES_PROPERTY_SOURCE_NAME, System.getProperties()));
        propertySource.addLast(new MapPropertySource(ENVIRONMENT_PROPERTY_SOURCE_NAME, (Map)System.getenv()));
    }

    @Override
    public String getProperty(String key) {
        return propertyResolver.getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return propertyResolver.getProperty(key, defaultValue);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return propertyResolver.getProperty(key, targetType);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return propertyResolver.getProperty(key, targetType, defaultValue);
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalArgumentException {
        String value = getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Required key '" + key + "' not found");
        }
        return value;
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalArgumentException {
        T value = getProperty(key, targetType);
        if (value == null) {
            throw new IllegalArgumentException("Required key '" + key + "' not found");
        }
        return value;
    }
}
