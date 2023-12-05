package org.openjava.probe.shared.property;

import java.util.Map;

public class MapPropertySource extends PropertySource<Map<String, Object>> {
    public MapPropertySource(String name, Map<String, Object> properties) {
        super(name, properties);
    }

    public boolean containsProperty(String name) {
        return source().containsKey(name);
    }

    @Override
    public Object getProperty(String name) {
        return source().get(name);
    }
}
