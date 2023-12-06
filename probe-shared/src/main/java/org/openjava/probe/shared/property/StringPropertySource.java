package org.openjava.probe.shared.property;

import org.openjava.probe.shared.util.ObjectUtils;

import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * supported format: key1=value1, key2=value2, key3=value3
 */
public class StringPropertySource extends MapPropertySource {
    public StringPropertySource(String name, String properties) {
        super(name, new HashMap<>());
        loadProperties(properties);
    }

    @Override
    public Object getProperty(String name) {
        return source().get(name);
    }

    private void loadProperties(String properties) {
        if (properties != null) {
            StringTokenizer tokenizer = new StringTokenizer(properties, ",");
            while(tokenizer.hasMoreTokens()) {
                String pair = tokenizer.nextToken();
                int index = pair.indexOf('=');
                if (index > 0 && index < pair.length() - 1) {
                    String key = pair.substring(0, index).trim();
                    String value = pair.substring(index + 1, pair.length()).trim();
                    if (ObjectUtils.isNotEmpty(key) && ObjectUtils.isNotEmpty(value)) {
                        source().put(key, value);
                    }
                }
            }
        }
    }
}
