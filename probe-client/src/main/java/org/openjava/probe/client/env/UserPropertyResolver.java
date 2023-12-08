package org.openjava.probe.client.env;

import org.openjava.probe.shared.property.AbstractPropertyResolver;
import org.openjava.probe.shared.property.PropertySource;

public class UserPropertyResolver extends AbstractPropertyResolver {
    private final PropertySource propertySource;

    public UserPropertyResolver(PropertySource propertySource) {
        this.propertySource = propertySource;
        setIgnoreUnresolvablePlaceholders(false);
    }

    @Override
    protected Object doGetProperty(String key) {
        return this.propertySource.getProperty(key);
    }
}
