package org.openjava.probe.shared.log;

public interface Layout<T> {
    String layout(T t);
}
