package org.openjava.probe.agent.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public interface IResourceFactory {
    Resource getResource(String name);

    Enumeration<URL> findResources(String name);

    interface ILoader {
        URL getBaseURL();

        Resource getResource(String name);

        URL[] getClassPath() throws IOException;
    }
}
