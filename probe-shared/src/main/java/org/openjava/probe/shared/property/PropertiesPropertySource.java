package org.openjava.probe.shared.property;

import java.util.Properties;

public class PropertiesPropertySource extends PropertySource<Properties> {
    public PropertiesPropertySource(String name, Properties source) {
        super(name, source);
    }

    public boolean containsProperty(String name) {
        return source().contains(name);
    }

    @Override
    public Object getProperty(String name) {
        return source().get(name);
    }
}
