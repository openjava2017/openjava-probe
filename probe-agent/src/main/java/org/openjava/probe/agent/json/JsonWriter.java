package org.openjava.probe.agent.json;

import org.objectweb.asm.Type;

public interface JsonWriter {
    void writeStartObject();

    void writeEndObject();

    void writeStartArray();

    void writeEndArray();

    void writeFieldName(String name);

    void writeFieldValue(String value);

    void writeFieldValue(boolean value);

    void writeFieldValue(char value);

    void writeFieldValue(int value);

    void writeFieldValue(float value);

    void writeFieldValue(long value);

    void writeFieldValue(double value);

    void writeSeparator();

    void writeRawValue(String value);

    void writeNull();
}
