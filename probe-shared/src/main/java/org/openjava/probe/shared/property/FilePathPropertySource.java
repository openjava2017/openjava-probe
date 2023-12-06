package org.openjava.probe.shared.property;

import java.io.InputStream;
import java.util.Properties;

public class FilePathPropertySource extends PropertiesPropertySource {
    public FilePathPropertySource(String name, String fileClassPath) {
        super(name, new Properties());
        loadProperties(fileClassPath);
    }

    private void loadProperties(String fileClassPath) {
        try {
            InputStream is = FilePathPropertySource.class.getClassLoader().getResourceAsStream(fileClassPath);
            source().load(is);
            is.close();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Load property file source failed: " + fileClassPath, ex);
        }
    }
}
