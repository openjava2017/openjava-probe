package org.openjava.probe.agent.json;

public class DefaultJsonWriter implements JsonWriter {
    private StringBuilder builder = new StringBuilder(64);

    @Override
    public void writeStartObject() {
        builder.append("{");
    }

    @Override
    public void writeEndObject() {
        builder.append("}");
    }

    @Override
    public void writeStartArray() {
        builder.append("[");
    }

    @Override
    public void writeEndArray() {
        builder.append("]");
    }

    @Override
    public void writeFieldName(String name) {
        builder.append("\"").append(name).append("\":");
    }

    @Override
    public void writeFieldValue(String value) {
        builder.append("\"").append(value).append("\"");
    }

    @Override
    public void writeFieldValue(boolean value) {
        builder.append(value);
    }

    @Override
    public void writeFieldValue(char value) {
        builder.append("\"").append(value).append("\"");
    }

    @Override
    public void writeFieldValue(int value) {
        builder.append(value);
    }

    @Override
    public void writeFieldValue(float value) {
        builder.append(value);
    }

    @Override
    public void writeFieldValue(long value) {
        builder.append(value);
    }

    @Override
    public void writeFieldValue(double value) {
        builder.append(value);
    }

    @Override
    public void writeSeparator() {
        builder.append(", ");
    }

    @Override
    public void writeRawValue(String value) {
        builder.append(value);
    }

    @Override
    public void writeNull() {
        builder.append("null");
    }

    @Override
    public String toString() {
        String json = builder.toString();
        builder.setLength(0);
        return json;
    }
}
