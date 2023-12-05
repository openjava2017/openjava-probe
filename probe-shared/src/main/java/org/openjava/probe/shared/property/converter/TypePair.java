package org.openjava.probe.shared.property.converter;

public class TypePair<S, T> {

    private final Class<S> sourceType;

    private final Class<T> targetType;

    public TypePair(Class<S> sourceType, Class<T> targetType) {
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    public Class<S> sourceType() {
        return this.sourceType;
    }

    public Class<T> targetType() {
        return this.targetType;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !TypePair.class.isAssignableFrom(other.getClass())) {
            return false;
        }
        TypePair typePair = (TypePair) other;
        return this.sourceType().equals(typePair.sourceType()) && this.targetType().equals(typePair.targetType());
    }

    @Override
    public int hashCode() {
        return this.sourceType().hashCode() * 31 + this.targetType().hashCode();
    }

    @Override
    public String toString() {
        return this.sourceType().getName() + " -> " + this.targetType().getName();
    }
}
