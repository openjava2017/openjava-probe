package org.openjava.probe.shared.property;

import org.openjava.probe.shared.util.ObjectUtils;

public abstract class PropertySource<T> {
    protected final String name;

    protected final T source;

    public PropertySource(String name, T source) {
        this.name = name;
        this.source = source;
    }

    public boolean containsProperty(String name) {
        return (getProperty(name) != null);
    }

    public abstract Object getProperty(String name);

    public String name() {
        return this.name;
    }

    public T source() {
        return this.source;
    }

    public static PropertySource<?> named(String name) {
        return new ComparisonPropertySource(name);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof PropertySource && ObjectUtils.equals(this.name, ((PropertySource<?>) other).name));
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {name='" + this.name + "'}";
    }

    static class ComparisonPropertySource extends PropertySource {
        public ComparisonPropertySource(String name) {
            super(name, name);
        }

        @Override
        public Object getProperty(String name) {
            return null;
        }
    }
}
