package org.openjava.probe.shared.property;

import org.openjava.probe.shared.property.converter.ConversionService;

public interface ConfigurablePropertyResolver extends PropertyResolver {
    ConversionService getConversionService();

    void setConversionService(ConversionService conversionService);

    void setPlaceholderPrefix(String placeholderPrefix);

    void setPlaceholderSuffix(String placeholderSuffix);

    void setValueSeparator(String valueSeparator);

    void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders);
}
