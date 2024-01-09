package org.openjava.probe.agent.json;

import java.lang.reflect.Field;
import java.util.*;

public class JsonSerializerProvider implements SerializerProvider {
    private LinkedList<SerializerFactory> factories = new LinkedList<>();

    public JsonSerializerProvider() {
        addLast(new StdSerializerFactory());
        addLast(new BucketSerializerFactory());
    }

    @Override
    public void addFirst(SerializerFactory serializerFactory) {
        factories.addFirst(serializerFactory);
    }

    @Override
    public void addLast(SerializerFactory serializerFactory) {
        factories.addLast(serializerFactory);
    }

    @Override
    public Iterator<SerializerFactory> iterator() {
        return factories.iterator();
    }

    @Override
    public JsonSerializer<?> createSerializer(Class clazz) {
        JsonSerializer jsonSerializer = null;
        for (Iterator<SerializerFactory> iterator = iterator(); iterator.hasNext(); ) {
            SerializerFactory factory = iterator.next();
            jsonSerializer = factory.createSerializer(clazz);
            if (jsonSerializer != null) {
                return jsonSerializer;
            }
        }

        if (jsonSerializer == null) {
            return BeanSerializer.INSTANCE;
        }
        return jsonSerializer;
    }

    @Override
    public void serialize(Object object, JsonWriter writer) {
        if (object != null) {
            JsonSerializer jsonSerializer = createSerializer(object.getClass());
            jsonSerializer.serialize(object, writer, this);
        } else {
            writer.writeNull();
        }
    }

    static class BeanSerializer implements JsonSerializer<Object> {
        public static final JsonSerializer INSTANCE = new BeanSerializer();
        @Override
        public void serialize(Object value, JsonWriter writer, SerializerProvider factory) {
            if (value != null) {
                writer.writeStartObject();

                Class clazz = value.getClass();
                List<Field> fields = new ArrayList<>();
                while (clazz != Object.class) {
                    fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
                    clazz = clazz.getSuperclass();
                }

                for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext();) {
                    Field field = iterator.next();
                    field.setAccessible(true);
                    try {
                        writer.writeFieldName(field.getName());
                        Object fieldValue = field.get(value);
                        if (fieldValue != null) {
                            JsonSerializer jsonSerializer = factory.createSerializer(fieldValue.getClass());
                            jsonSerializer.serialize(fieldValue, writer, factory);
                        } else {
                            writer.writeNull();
                        }

                        if (iterator.hasNext()) {
                            writer.writeSeparator();
                        }
                    } catch (Exception ex) {
                        // Ignore it
                    }
                }

                writer.writeEndObject();
            } else {
                writer.writeNull();
            }
        }
    }
}
