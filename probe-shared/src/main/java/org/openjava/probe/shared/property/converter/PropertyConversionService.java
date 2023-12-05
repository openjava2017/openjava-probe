package org.openjava.probe.shared.property.converter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NOTE: Array Enum not supported for property convert
 */
public class PropertyConversionService implements ConversionService, ConverterRegister {
    private static Map<TypePair, PropertyConverter> converters = new ConcurrentHashMap<>();

    {
        registerConverter(new ObjectToStringConverter());
        registerConverter(new StringToIntegerConverter());
        registerConverter(new StringToLongConverter());
    }

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == targetType) {
            return true;
        }

        if (targetType.isPrimitive()) {
            targetType = objectiveClass(targetType);
        }

        if (converters.containsKey(new TypePair(sourceType, targetType))) {
            return true;
        }
        return false;
    }

    @Override
    public <T> T convert(Object source, Class<T> targetType) {
        if (targetType.isPrimitive()) {
            targetType = (Class<T>) objectiveClass(targetType);
        }

        PropertyConverter converter = converters.get(new TypePair(source.getClass(), targetType));
        if (converter != null) {
            return (T) converter.convert(source, targetType);
        }

        return (T) source;
    }

    @Override
    public void registerConverter(PropertyConverter converter) {
        converters.put(converter.typePair(), converter);
    }

    private Class<?> objectiveClass(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == char.class)
                return Character.class;
            if (clazz == int.class)
                return Integer.class;
            if (clazz == boolean.class)
                return Boolean.class;
            if (clazz == byte.class)
                return Byte.class;
            if (clazz == double.class)
                return Double.class;
            if (clazz == float.class)
                return Float.class;
            if (clazz == long.class)
                return Long.class;
            if (clazz == short.class)
                return Short.class;
        }

        return clazz;
    }
}
