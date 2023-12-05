package org.openjava.probe.shared.property;

import org.openjava.probe.shared.property.converter.ConversionService;
import org.openjava.probe.shared.property.converter.PropertyConversionService;

public abstract class AbstractPropertyResolver implements ConfigurablePropertyResolver {

    protected ConversionService conversionService = new PropertyConversionService();

    private PropertyPlaceholderHelper propertyPlaceholderHelper;

    private boolean ignoreUnresolvablePlaceholders = false;

    private String placeholderPrefix = "${";

    private String placeholderSuffix = "}";

    private String valueSeparator = ":";

    @Override
    public ConversionService getConversionService() {
        return this.conversionService;
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    @Override
    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    @Override
    public void setValueSeparator(String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    @Override
    public boolean containsProperty(String key) {
        return (doGetProperty(key) != null);
    }

    @Override
    public String getProperty(String key) {
        return getProperty(key, String.class);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return (value != null ? value : defaultValue);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        Object value = doGetProperty(key);
        if (value != null) {
            Class<?> valueType = value.getClass();
            if (valueType == String.class) {
                value = resolvePlaceholders((String) value);
            }

            if (this.conversionService.canConvert(valueType, targetType)) {
                return this.conversionService.convert(value, targetType);
            } else {
                String format = "Cannot convert value [%s] from source type [%s] to target type [%s]";
                throw new IllegalArgumentException(String.format(format, value, valueType.getSimpleName(), targetType.getSimpleName()));
            }
        }
        return null;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        T value = getProperty(key, targetType);
        return (value != null ? value : defaultValue);
    }

    @Override
    public String resolvePlaceholders(String text) {
        if (this.propertyPlaceholderHelper == null) {
            this.propertyPlaceholderHelper = createPlaceholderHelper();
        }

        return propertyPlaceholderHelper.replacePlaceholders(text, new PropertyPlaceholderHelper.PlaceholderResolver() {
            public String resolvePlaceholder(String placeholderName) {
                Object value = doGetProperty(placeholderName);
                if (value != null) {
                    return value.toString();
                }
                return null;
            }
        });
    }

    protected abstract Object doGetProperty(String key);

    protected PropertyPlaceholderHelper createPlaceholderHelper() {
        return new PropertyPlaceholderHelper(this.placeholderPrefix, this.placeholderSuffix, this.valueSeparator, ignoreUnresolvablePlaceholders);
    }
}
