package org.openjava.probe.shared.property;

public interface PropertySourceRegister extends Iterable<PropertySource<?>> {
    boolean contains(String name);

    void addFirst(PropertySource<?> propertySource);

    void addLast(PropertySource<?> propertySource);

    PropertySource<?> get(String name);

    PropertySource<?> remove(String name);

    void replace(String name, PropertySource<?> propertySource);
}
