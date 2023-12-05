package org.openjava.probe.shared.property.converter;

public class StringToIntegerConverter extends AbstractPropertyConverter<String, Integer> {

    @Override
    public Integer convert(String source, Class<Integer> targetType) {
        return Integer.parseInt(source);
    }

    @Override
    public TypePair<String, Integer> typePair() {
        return new TypePair<>(String.class, Integer.class);
    }
}
