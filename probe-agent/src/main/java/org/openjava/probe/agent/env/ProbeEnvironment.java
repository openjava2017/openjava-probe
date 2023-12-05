package org.openjava.probe.agent.env;

import org.openjava.probe.shared.property.*;

import java.util.LinkedList;
import java.util.Map;

public class ProbeEnvironment implements Environment {

    private static final String PROBE_PROPERTY_SOURCE_NAME = "probeAgentProperties";
    private static final String ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment";
    private static final String PROPERTIES_PROPERTY_SOURCE_NAME = "systemProperties";
    private static final String CHAIN_PROPERTY_SOURCE_NAME = "chainPropertySource";
    private static final String PROBE_AGENT_CONFIG_FILE = "org/openjava/probe/agent/config/ProbeAgent.properties";

    private final ChainPropertySource propertySource = new ChainPropertySource(CHAIN_PROPERTY_SOURCE_NAME, new LinkedList<>());
    private final PropertyResolver propertyResolver = new EnvironmentPropertyResolver(propertySource);

    public ProbeEnvironment(String args) {
        propertySource.addLast(new ClassPathPropertySource(PROBE_PROPERTY_SOURCE_NAME, PROBE_AGENT_CONFIG_FILE));
        propertySource.addLast(new MapPropertySource(ENVIRONMENT_PROPERTY_SOURCE_NAME, (Map)System.getenv()));
        propertySource.addLast(new PropertiesPropertySource(PROPERTIES_PROPERTY_SOURCE_NAME, System.getProperties()));
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

    public static void main(String[] args) {
        Environment environment = new ProbeEnvironment("test");
        String s = environment.getProperty("probe.server.host");
        System.out.println(s);
        Integer port = environment.getProperty("probe.server.port", int.class);
        System.out.println(port);
        String userHome = environment.getProperty("probe.work.home");
        System.out.println(userHome);
    }
}
