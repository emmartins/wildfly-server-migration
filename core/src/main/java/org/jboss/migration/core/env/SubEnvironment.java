/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.core.env;

import org.jboss.migration.core.ServerMigrationFailedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author emmartins
 */
public class SubEnvironment implements Environment {

    private final Environment environment;
    private final String propertyNamePrefix;

    public SubEnvironment(Environment environment, String propertyNamePrefix) {
        this.environment = environment;
        this.propertyNamePrefix = propertyNamePrefix;
    }

    private String getAbsolutePropertyName(String propertyName) {
        return new StringBuilder(propertyNamePrefix).append(propertyName).toString();
    }

    public Boolean getPropertyAsBoolean(String propertyName) {
        return environment.getPropertyAsBoolean(getAbsolutePropertyName(propertyName));
    }

    public Boolean getPropertyAsBoolean(String propertyName, Boolean defaultValue) {
        return environment.getPropertyAsBoolean(getAbsolutePropertyName(propertyName), defaultValue);
    }

    public String getPropertyAsString(String propertyName) {
        return environment.getPropertyAsString(getAbsolutePropertyName(propertyName));
    }

    public String getPropertyAsString(String propertyName, String defaultValue) {
        return environment.getPropertyAsString(getAbsolutePropertyName(propertyName), defaultValue);
    }

    public List<String> getPropertyAsList(String propertyName) {
        return environment.getPropertyAsList(getAbsolutePropertyName(propertyName));
    }

    public List<String> getPropertyAsList(String propertyName, List<String> defaultValue) {
        return environment.getPropertyAsList(getAbsolutePropertyName(propertyName), defaultValue);
    }

    public Boolean requirePropertyAsBoolean(String propertyName) throws ServerMigrationFailedException {
        return environment.requirePropertyAsBoolean(getAbsolutePropertyName(propertyName));
    }

    public String requirePropertyAsString(String propertyName, boolean failIfEmpty) throws ServerMigrationFailedException {
        return environment.requirePropertyAsString(getAbsolutePropertyName(propertyName), failIfEmpty);
    }

    public List<String> requirePropertyAsList(String propertyName, boolean failIfEmpty) throws ServerMigrationFailedException {
        return environment.requirePropertyAsList(getAbsolutePropertyName(propertyName), failIfEmpty);
    }

    public String setProperty(String propertyName, String propertyValue) {
        return environment.setProperty(getAbsolutePropertyName(propertyName), propertyValue);
    }

    public void setProperties(Properties properties) {
        for (String propertyName : properties.stringPropertyNames()) {
            setProperty(propertyName, properties.getProperty(propertyName));
        }
    }

    public List<String> getPropertyNames() {
        final List<String> result = new ArrayList<>();
        for (String envPropertyName : environment.getPropertyNames()) {
            if (envPropertyName.startsWith(propertyNamePrefix)) {
                result.add(envPropertyName.substring(propertyNamePrefix.length()));
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<String> getPropertyNamesReaded() {
        final List<String> result = new ArrayList<>();
        for (String envPropertyName : environment.getPropertyNamesReaded()) {
            if (envPropertyName.startsWith(propertyNamePrefix)) {
                result.add(envPropertyName.substring(propertyNamePrefix.length()));
            }
        }
        return Collections.unmodifiableList(result);
    }
}
