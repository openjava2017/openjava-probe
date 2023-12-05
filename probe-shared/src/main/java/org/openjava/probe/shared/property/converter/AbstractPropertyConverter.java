package org.openjava.probe.shared.property.converter;

public abstract class AbstractPropertyConverter<S, T> implements PropertyConverter<S, T> {

    public abstract T convert(S source, Class<T> targetType);

    public abstract TypePair<S, T> typePair();
}
