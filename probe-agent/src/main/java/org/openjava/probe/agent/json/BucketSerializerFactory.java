package org.openjava.probe.agent.json;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class BucketSerializerFactory implements SerializerFactory {
    private static Map<String, JsonSerializer<?>> serializers = new HashMap<>(32);

    static {
        serializers.put(HashMap.class.getName(), MapSerializer.INSTANCE);
        serializers.put(TreeMap.class.getName(), MapSerializer.INSTANCE);
        serializers.put(ConcurrentHashMap.class.getName(), MapSerializer.INSTANCE);
        serializers.put(Hashtable.class.getName(), MapSerializer.INSTANCE);

        serializers.put(ArrayList.class.getName(), CollectionSerializer.INSTANCE);
        serializers.put(Vector.class.getName(), CollectionSerializer.INSTANCE);
        serializers.put(LinkedList.class.getName(), CollectionSerializer.INSTANCE);

        serializers.put(HashSet.class.getName(), CollectionSerializer.INSTANCE);
        serializers.put(TreeSet.class.getName(), CollectionSerializer.INSTANCE);
        serializers.put(LinkedHashSet.class.getName(), CollectionSerializer.INSTANCE);

        serializers.put(ArrayBlockingQueue.class.getName(), CollectionSerializer.INSTANCE);
        serializers.put(LinkedBlockingQueue.class.getName(), CollectionSerializer.INSTANCE);
        serializers.put(LinkedBlockingDeque.class.getName(), CollectionSerializer.INSTANCE);

        serializers.put(boolean[].class.getName(), BooleanArraySerializer.INSTANCE);
        serializers.put(byte[].class.getName(), ByteArraySerializer.INSTANCE);
        serializers.put(char[].class.getName(), CharArraySerializer.INSTANCE);
        serializers.put(short[].class.getName(), ShortArraySerializer.INSTANCE);
        serializers.put(int[].class.getName(), IntArraySerializer.INSTANCE);
        serializers.put(long[].class.getName(), LongArraySerializer.INSTANCE);
        serializers.put(float[].class.getName(), FloatArraySerializer.INSTANCE);
        serializers.put(double[].class.getName(), DoubleArraySerializer.INSTANCE);
    }

    @Override
    public JsonSerializer<?> createSerializer(Class clazz) {
        JsonSerializer serializer = serializers.get(clazz.getName());

        if (serializer == null && List.class.isAssignableFrom(clazz)) {
            return CollectionSerializer.INSTANCE;
        }
        if (serializer == null && Map.class.isAssignableFrom(clazz)) {
            return MapSerializer.INSTANCE;
        }
        if (serializer == null && clazz.isArray()) {
            return ObjectArraySerializer.INSTANCE;
        }

        return serializer;
    }

    static class MapSerializer implements JsonSerializer<Map<?, ?>> {
        public static final JsonSerializer INSTANCE = new MapSerializer();
        @Override
        public void serialize(Map<?, ?> value, JsonWriter writer, SerializerProvider factory) {
            writer.writeStartObject();
            for (Iterator<? extends Map.Entry<?, ?>> iterator = value.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<?, ?> entry = iterator.next();
                Object entryKey = entry.getKey();
                writer.writeFieldName(entryKey == null ? "null" : entryKey.toString());
                Object entryValue = entry.getValue();

                if (entryValue != null) {
                    JsonSerializer serializer = factory.createSerializer(entryValue.getClass());
                    serializer.serialize(entryValue, writer, factory);
                } else {
                    writer.writeNull();
                }

                if (iterator.hasNext()) {
                    writer.writeSeparator();
                }
            }
            writer.writeEndObject();
        }
    }

    static class CollectionSerializer implements JsonSerializer<Collection<?>> {
        public static final JsonSerializer INSTANCE = new CollectionSerializer();
        @Override
        public void serialize(Collection<?> value, JsonWriter writer, SerializerProvider factory) {
            writer.writeStartArray();
            for (Iterator iterator = value.iterator(); iterator.hasNext(); ) {
                Object o = iterator.next();
                if (o != null) {
                    JsonSerializer serializer = factory.createSerializer(o.getClass());
                    serializer.serialize(o, writer, factory);
                } else {
                    writer.writeNull();
                }

                if (iterator.hasNext()) {
                    writer.writeSeparator();
                }
            }
            writer.writeEndArray();
        }
    }

    static class BooleanArraySerializer implements JsonSerializer<boolean[]> {
        public static final JsonSerializer INSTANCE = new BooleanArraySerializer();
        @Override
        public void serialize(boolean[] value, JsonWriter writer, SerializerProvider factory) {
            writer.writeStartArray();
            for (int i = 0; i < value.length; i++) {
                boolean o = value[i];
                writer.writeFieldValue(o);

                if (i < value.length - 1) {
                    writer.writeSeparator();
                }
            }
            writer.writeEndArray();
        }
    }

    static class ByteArraySerializer implements JsonSerializer<byte[]> {
        public static final JsonSerializer INSTANCE = new ByteArraySerializer();
        @Override
        public void serialize(byte[] value, JsonWriter writer, SerializerProvider factory) {
            writer.writeStartArray();
            for (int i = 0; i < value.length; i++) {
                byte o = value[i];
                writer.writeFieldValue(o);

                if (i < value.length - 1) {
                    writer.writeSeparator();
                }
            }
            writer.writeEndArray();
        }
    }

    static class CharArraySerializer implements JsonSerializer<char[]> {
        public static final JsonSerializer INSTANCE = new CharArraySerializer();
        @Override
        public void serialize(char[] value, JsonWriter writer, SerializerProvider factory) {
            writer.writeStartArray();
            for (int i = 0; i < value.length; i++) {
                char o = value[i];
                writer.writeFieldValue(o);

                if (i < value.length - 1) {
                    writer.writeSeparator();
                }
            }
            writer.writeEndArray();
        }
    }

    static class ShortArraySerializer implements JsonSerializer<short[]> {
        public static final JsonSerializer INSTANCE = new ShortArraySerializer();
        @Override
        public void serialize(short[] value, JsonWriter writer, SerializerProvider factory) {
            writer.writeStartArray();
            for (int i = 0; i < value.length; i++) {
                short o = value[i];
                writer.writeFieldValue(o);

                if (i < value.length - 1) {
                    writer.writeSeparator();
                }
            }
            writer.writeEndArray();
        }
    }

    static class IntArraySerializer implements JsonSerializer<int[]> {
        public static final JsonSerializer INSTANCE = new IntArraySerializer();
        @Override
        public void serialize(int[] value, JsonWriter writer, SerializerProvider factory) {
            writer.writeStartArray();
            for (int i = 0; i < value.length; i++) {
                int o = value[i];
                writer.writeFieldValue(o);

                if (i < value.length - 1) {
                    writer.writeSeparator();
                }
            }
            writer.writeEndArray();
        }
    }

    static class LongArraySerializer implements JsonSerializer<long[]> {
        public static final JsonSerializer INSTANCE = new LongArraySerializer();
        @Override
        public void serialize(long[] value, JsonWriter writer, SerializerProvider factory) {
            writer.writeStartArray();
            for (int i = 0; i < value.length; i++) {
                long o = value[i];
                writer.writeFieldValue(o);

                if (i < value.length - 1) {
                    writer.writeSeparator();
                }
            }
            writer.writeEndArray();
        }
    }

    static class FloatArraySerializer implements JsonSerializer<float[]> {
        public static final JsonSerializer INSTANCE = new FloatArraySerializer();
        @Override
        public void serialize(float[] value, JsonWriter writer, SerializerProvider factory) {
            writer.writeStartArray();
            for (int i = 0; i < value.length; i++) {
                float o = value[i];
                writer.writeFieldValue(o);

                if (i < value.length - 1) {
                    writer.writeSeparator();
                }
            }
            writer.writeEndArray();
        }
    }

    static class DoubleArraySerializer implements JsonSerializer<double[]> {
        public static final JsonSerializer INSTANCE = new DoubleArraySerializer();
        @Override
        public void serialize(double[] value, JsonWriter writer, SerializerProvider factory) {
            writer.writeStartArray();
            for (int i = 0; i < value.length; i++) {
                double o = value[i];
                writer.writeFieldValue(o);

                if (i < value.length - 1) {
                    writer.writeSeparator();
                }
            }
            writer.writeEndArray();
        }
    }

    static class ObjectArraySerializer implements JsonSerializer<Object[]> {
        public static final JsonSerializer INSTANCE = new ObjectArraySerializer();
        @Override
        public void serialize(Object[] value, JsonWriter writer, SerializerProvider factory) {
            writer.writeStartArray();
            for (int i = 0; i < value.length; i++) {
                Object o = value[i];
                if (o != null) {
                    JsonSerializer serializer = factory.createSerializer(o.getClass());
                    serializer.serialize(o, writer, factory);
                } else {
                    writer.writeNull();
                }

                if (i < value.length - 1) {
                    writer.writeSeparator();
                }
            }
            writer.writeEndArray();
        }
    }
}
