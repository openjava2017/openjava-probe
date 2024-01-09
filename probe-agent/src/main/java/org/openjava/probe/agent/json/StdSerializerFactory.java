package org.openjava.probe.agent.json;

import org.openjava.probe.shared.util.DateUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StdSerializerFactory implements SerializerFactory {
    private static Map<String, JsonSerializer<?>> serializers = new HashMap<>(32);

    static {
        serializers.put(Byte.class.getName(), NumberSerializer.INSTANCE);
        serializers.put(Short.class.getName(), NumberSerializer.INSTANCE);
        serializers.put(Integer.class.getName(), NumberSerializer.INSTANCE);
        serializers.put(Long.class.getName(), NumberSerializer.INSTANCE);
        serializers.put(Double.class.getName(), NumberSerializer.INSTANCE);
        serializers.put(Float.class.getName(), NumberSerializer.INSTANCE);
        serializers.put(BigDecimal.class.getName(), NumberSerializer.INSTANCE);
        serializers.put(BigInteger.class.getName(), NumberSerializer.INSTANCE);
        serializers.put(AtomicInteger.class.getName(), NumberSerializer.INSTANCE);
        serializers.put(AtomicLong.class.getName(), NumberSerializer.INSTANCE);

        serializers.put(Boolean.class.getName(), BooleanSerializer.INSTANCE);
        serializers.put(Character.class.getName(), CharacterSerializer.INSTANCE);

        serializers.put(String.class.getName(), StringSerializer.INSTANCE);
        serializers.put(Date.class.getName(), DateSerializer.INSTANCE);
        serializers.put(LocalDate.class.getName(), LocalDateSerializer.INSTANCE);
        serializers.put(LocalDateTime.class.getName(), LocalDateTimeSerializer.INSTANCE);

        serializers.put(AtomicBoolean.class.getName(), ToStringSerializer.INSTANCE);
        serializers.put(StringBuffer.class.getName(), ToStringSerializer.INSTANCE);
        serializers.put(StringBuilder.class.getName(), ToStringSerializer.INSTANCE);

        serializers.put(Void.class.getName(), NullSerializer.INSTANCE);
        serializers.put(Class.class.getName(), ClassSerializer.INSTANCE);
        serializers.put(URL.class.getName(), ToStringSerializer.INSTANCE);
        serializers.put(URI.class.getName(), ToStringSerializer.INSTANCE);
        serializers.put(UUID.class.getName(), ToStringSerializer.INSTANCE);
        serializers.put(File.class.getName(), ToStringSerializer.INSTANCE);

        serializers.put(Locale.class.getName(), ToStringSerializer.INSTANCE);
        serializers.put(Currency.class.getName(), ToStringSerializer.INSTANCE);
    }

    @Override
    public JsonSerializer<?> createSerializer(Class clazz) {
        JsonSerializer serializer = serializers.get(clazz.getName());
        if (serializer == null && clazz.isEnum()) {
            return ToStringSerializer.INSTANCE;
        }

        if (serializer == null) {
            Class superClass = clazz.getSuperclass();
            serializer = serializers.get(superClass);
        }
        return serializer;
    }

    static class NumberSerializer implements JsonSerializer<Number> {
        public static final JsonSerializer INSTANCE = new NumberSerializer();
        @Override
        public void serialize(Number value, JsonWriter writer, SerializerProvider factory) {
            if (value instanceof Integer || value instanceof Byte || value instanceof Short) {
                writer.writeFieldValue(value.intValue());
            } else if (value instanceof Long) {
                writer.writeFieldValue(value.longValue());
            } else if (value instanceof Double) {
                writer.writeFieldValue(value.doubleValue());
            } else if (value instanceof Float) {
                writer.writeFieldValue(value.floatValue());
            } else {
                writer.writeFieldValue(value.toString());
            }
        }
    }

    static class BooleanSerializer implements JsonSerializer<Boolean> {
        public static final JsonSerializer INSTANCE = new BooleanSerializer();
        @Override
        public void serialize(Boolean value, JsonWriter writer, SerializerProvider factory) {
            writer.writeFieldValue(value);
        }
    }

    static class CharacterSerializer implements JsonSerializer<Character> {
        public static final JsonSerializer INSTANCE = new CharacterSerializer();
        @Override
        public void serialize(Character value, JsonWriter writer, SerializerProvider factory) {
            writer.writeFieldValue(value);
        }
    }

    static class StringSerializer implements JsonSerializer<String> {
        public static final JsonSerializer INSTANCE = new StringSerializer();
        @Override
        public void serialize(String value, JsonWriter writer, SerializerProvider factory) {
            writer.writeFieldValue(value);
        }
    }

    static class ToStringSerializer implements JsonSerializer<Object> {
        public static final JsonSerializer INSTANCE = new ToStringSerializer();
        @Override
        public void serialize(Object value, JsonWriter writer, SerializerProvider factory) {
            writer.writeFieldValue(String.valueOf(value));
        }
    }

    static class DateSerializer implements JsonSerializer<Date> {
        public static final JsonSerializer INSTANCE = new DateSerializer();
        @Override
        public void serialize(Date value, JsonWriter writer, SerializerProvider factory) {
            writer.writeFieldValue(DateUtils.format(value));
        }
    }

    static class LocalDateSerializer implements JsonSerializer<LocalDate> {
        public static final JsonSerializer INSTANCE = new LocalDateSerializer();
        @Override
        public void serialize(LocalDate value, JsonWriter writer, SerializerProvider factory) {
            writer.writeFieldValue(DateUtils.formatDate(value));
        }
    }

    static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        public static final JsonSerializer INSTANCE = new LocalDateTimeSerializer();
        @Override
        public void serialize(LocalDateTime value, JsonWriter writer, SerializerProvider factory) {
            writer.writeFieldValue(DateUtils.formatDateTime(value));
        }
    }

    static class ClassSerializer implements JsonSerializer<Class> {
        public static final JsonSerializer INSTANCE = new ClassSerializer();
        @Override
        public void serialize(Class value, JsonWriter writer, SerializerProvider factory) {
            writer.writeFieldValue(value.getName());
        }
    }

    static class NullSerializer implements JsonSerializer<Object> {
        public static final JsonSerializer INSTANCE = new NullSerializer();
        @Override
        public void serialize(Object value, JsonWriter writer, SerializerProvider factory) {
            writer.writeNull();
        }
    }
}
