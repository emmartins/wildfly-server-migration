/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.core.env;

import org.jboss.migration.core.ServerMigrationFailureException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author emmartins
 */
public class MigrationEnvironment implements Environment {

    private final SortedMap<String, PropertyValue> properties;
    private final SortedSet<String> readed = new TreeSet<>();

    public MigrationEnvironment() {
        this.properties = new TreeMap<>();
    }

    public Boolean getPropertyAsBoolean(String propertyName) {
        final String propertyValue  = getPropertyAsString(propertyName);
        if (propertyValue == null || propertyValue.isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(propertyValue);
    }

    public Boolean getPropertyAsBoolean(String propertyName, Boolean defaultValue) {
        final Boolean propertyValue = getPropertyAsBoolean(propertyName);
        return propertyValue != null ? propertyValue : defaultValue;
    }

    public String getPropertyAsString(String propertyName) {
        readed.add(propertyName);
        final PropertyValue propertyValue = properties.get(propertyName);
        if (propertyValue == null) {
            return null;
        }
        return propertyValue.getStringValue();
    }

    public String getPropertyAsString(String propertyName, String defaultValue) {
        final String propertyValue = getPropertyAsString(propertyName);
        return propertyValue != null ? propertyValue : defaultValue;
    }

    public List<String> getPropertyAsList(String propertyName) {
        readed.add(propertyName);
        final PropertyValue propertyValue = properties.get(propertyName);
        if (propertyValue == null) {
            return null;
        }
        return propertyValue.getListValue();
    }

    public List<String> getPropertyAsList(String propertyName, List<String> defaultValue) {
        final List<String> propertyValue = getPropertyAsList(propertyName);
        return propertyValue != null ? propertyValue : defaultValue;
    }

    public Boolean requirePropertyAsBoolean(String propertyName) throws ServerMigrationFailureException {
        final Boolean propertyValue = getPropertyAsBoolean(propertyName);
        if (propertyValue == null) {
            throw new ServerMigrationFailureException("Environment property "+propertyName+" is required.");
        } else {
            return propertyValue;
        }
    }

    public String requirePropertyAsString(String propertyName, boolean failIfEmpty) throws ServerMigrationFailureException {
        final String propertyValue = getPropertyAsString(propertyName);
        if (propertyValue == null || (failIfEmpty && propertyValue.isEmpty())) {
            throw new ServerMigrationFailureException("Environment property "+propertyName+" is required.");
        } else {
            return propertyValue;
        }
    }

    public List<String> requirePropertyAsList(String propertyName, boolean failIfEmpty) throws ServerMigrationFailureException {
        final List<String> propertyValue = getPropertyAsList(propertyName);
        if (propertyValue == null || (failIfEmpty && propertyValue.isEmpty())) {
            throw new ServerMigrationFailureException("Environment property "+propertyName+" is required.");
        } else {
            return propertyValue;
        }
    }

    public String setProperty(String propertyName, String propertyValue) {
        final PropertyValue old = properties.put(propertyName, new PropertyValue(propertyValue));
        return old != null ? old.getStringValue() : null;
    }

    public void setProperties(Properties properties) {
        for (String propertyName : properties.stringPropertyNames()) {
            setProperty(propertyName, properties.getProperty(propertyName));
        }
    }

    public void setProperties(MigrationEnvironment migrationEnvironment) {
        properties.putAll(migrationEnvironment.properties);
    }

    public List<String> getPropertyNames() {
        return Collections.unmodifiableList(new ArrayList<>(properties.keySet()));
    }

    public List<String> getPropertyNamesReaded() {
        final List<String> result = new ArrayList<>();
        for (String propertyName : properties.keySet()) {
            final PropertyValue propertyValue = properties.get(propertyName);
            if (propertyValue.isReaded()) {
                result.add(propertyName);
            }
        }
        //return Collections.unmodifiableList(result);
        return Collections.unmodifiableList(new ArrayList<>(readed));
    }

    private static class PropertyValue {
        private final String stringValue;
        private List<String> listValue;
        private boolean readed;
        private PropertyValue(String stringValue) {
            this.stringValue = stringValue;
        }

        public String getStringValue() {
            readed = true;
            return stringValue;
        }

        public List<String> getListValue() {
            readed = true;
            if (listValue == null) {
                final List<String> list = new ArrayList<>();
                for (String s : stringValue.split(",")) {
                    final String st = s.trim();
                    if (!st.isEmpty()) {
                        list.add(st);
                    }
                }
                listValue = list;
            }
            return listValue;
        }

        public boolean isReaded() {
            return readed;
        }
    }
}
