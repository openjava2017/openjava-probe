package org.openjava.probe.agent.env;

import org.openjava.probe.shared.property.AbstractPropertyResolver;
import org.openjava.probe.shared.property.ChainPropertySource;

public class EnvironmentPropertyResolver extends AbstractPropertyResolver {
    private final ChainPropertySource propertySource;

    public EnvironmentPropertyResolver(ChainPropertySource propertySource) {
        this.propertySource = propertySource;
        setIgnoreUnresolvablePlaceholders(false);
    }

    @Override
    protected Object doGetProperty(String key) {
        return this.propertySource.getProperty(key);
    }
}
