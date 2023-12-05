package org.openjava.probe.shared.property.converter;

public interface PropertyConverter<S, T> {
    T convert(S source, Class<T> targetType);

    TypePair<S, T> typePair();
}
