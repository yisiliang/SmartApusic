package com.poratu.idea.plugins.apusic.setting;

import com.intellij.openapi.options.ConfigurationException;

@FunctionalInterface
public interface ApusicNameValidator<T> {
    void validate(T t) throws ConfigurationException;
}
