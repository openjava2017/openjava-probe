package org.openjava.probe.agent.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class AgentClassloader extends ClassLoader {

    static {
        if (!ClassLoader.registerAsParallelCapable()) {
            throw new InternalError();
        }
    }

    private final IResourceFactory resourceFactory;

    public AgentClassloader(URL[] urls) {
        super();
        resourceFactory = initResourceFactory(urls);
    }

    public AgentClassloader(URL[] urls, ClassLoader parent) {
        super(parent);
        resourceFactory = initResourceFactory(urls);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> c = findClassOrNull(name);
        if (c == null) throw new ClassNotFoundException(name);
        return c;
    }

    @Override
    protected URL findResource(String name) {
        Resource resource = resourceFactory.getResource(name);
        if (resource != null) {
            return resource.getURL();
        }
        return null;
    }

    @Override
    protected Enumeration<URL> findResources(String name) {
        return resourceFactory.findResources(name);
    }

    private Class<?> findClassOrNull(String name) {
        if (name == null) return null;
        String path = name.replace('.', '/').concat(".class");
        Resource resource = resourceFactory.getResource(path);
        if (resource == null) return null;
        try {
            byte[] bytes = resource.getBytes();
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException iex) {
            return null;
        }
    }

    protected IResourceFactory initResourceFactory(URL[] urls) {
        if (urls == null || urls.length == 0) {
            throw new IllegalArgumentException("Url cannot be empty");
        }
        URLClassPath urlClassPath = new URLClassPath(urls);
        return new URLResourceFactory(urlClassPath);
    }
}
