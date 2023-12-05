package org.openjava.probe.shared.property.converter;

public class StringToLongConverter extends AbstractPropertyConverter<String, Long> {

    @Override
    public Long convert(String source, Class<Long> targetType) {
        return Long.parseLong(source);
    }

    @Override
    public TypePair<String, Long> typePair() {
        return new TypePair<>(String.class, Long.class);
    }
}
