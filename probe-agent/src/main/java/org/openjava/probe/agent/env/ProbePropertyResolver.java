package org.openjava.probe.agent.env;

import org.openjava.probe.shared.property.AbstractPropertyResolver;
import org.openjava.probe.shared.property.PropertySource;

public class ProbePropertyResolver extends AbstractPropertyResolver {
    private final PropertySource propertySource;

    public ProbePropertyResolver(PropertySource propertySource) {
        this.propertySource = propertySource;
        setIgnoreUnresolvablePlaceholders(false);
    }

    @Override
    protected Object doGetProperty(String key) {
        return this.propertySource.getProperty(key);
    }
}
