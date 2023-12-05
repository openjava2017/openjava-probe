package org.openjava.probe.shared.property;

import java.util.Iterator;
import java.util.LinkedList;

public class ChainPropertySource extends PropertySource<LinkedList<PropertySource<?>>> implements PropertySourceRegister {

    public ChainPropertySource(String name, LinkedList<PropertySource<?>> source) {
        super(name, source);
    }

    @Override
    public boolean contains(String name) {
        return this.source.contains(PropertySource.named(name));
    }

    @Override
    public void addFirst(PropertySource<?> propertySource) {
        this.source.remove(propertySource);
        this.source.addFirst(propertySource);
    }

    @Override
    public void addLast(PropertySource<?> propertySource) {
        this.source.remove(propertySource);
        this.source.addLast(propertySource);
    }

    @Override
    public PropertySource<?> get(String name) {
        int index = this.source.indexOf(PropertySource.named(name));
        return index == -1 ? null : this.source.get(index);
    }

    @Override
    public PropertySource<?> remove(String name) {
        int index = this.source.indexOf(PropertySource.named(name));
        return index == -1 ? null : this.source.remove(index);
    }

    @Override
    public void replace(String name, PropertySource<?> propertySource) {
        int index = this.source.indexOf(PropertySource.named(name));
        if (index > 0) {
            this.source.set(index, propertySource);
        } else {
            throw new IllegalArgumentException(String.format("property source [%s] not found", name));
        }
    }

    @Override
    public Iterator<PropertySource<?>> iterator() {
        return this.source.iterator();
    }

    public boolean containsProperty(String name) {
        for (Iterator<PropertySource<?>> iterator = iterator(); iterator.hasNext(); ) {
            PropertySource source = iterator.next();
            if (source.containsProperty(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getProperty(String name) {
        for (Iterator<PropertySource<?>> iterator = iterator(); iterator.hasNext(); ) {
            PropertySource source = iterator.next();
            Object value = source.getProperty(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
