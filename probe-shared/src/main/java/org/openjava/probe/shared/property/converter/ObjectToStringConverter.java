package org.openjava.probe.shared.property.converter;

public class ObjectToStringConverter extends AbstractPropertyConverter<Object, String> {
    @Override
    public String convert(Object source, Class<String> targetType) {
        return source.toString();
    }

    @Override
    public TypePair<Object, String> typePair() {
        return new TypePair<>(Object.class, String.class);
    }
}
