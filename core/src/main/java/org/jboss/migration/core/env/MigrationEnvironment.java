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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author emmartins
 */
public class MigrationEnvironment {

    private final SortedMap<String, PropertyValue> properties;

    public MigrationEnvironment() {
        this.properties = new TreeMap<>();
    }

    public Boolean getPropertyAsBoolean(String propertyName) {
        final String propertyValue  = getPropertyAsString(propertyName);
        if (propertyValue == null) {
            return null;
        }
        return Boolean.parseBoolean(propertyValue);
    }

    public Boolean getPropertyAsBoolean(String propertyName, Boolean defaultValue) {
        final Boolean propertyValue = getPropertyAsBoolean(propertyName);
        return propertyValue != null ? propertyValue : defaultValue;
    }

    public String getPropertyAsString(String propertyName) {
        final PropertyValue propertyValue = properties.get(propertyName);
        if (propertyValue == null) {
            return null;
        }
        return  propertyValue.getStringValue();
    }

    public String getPropertyAsString(String propertyName, String defaultValue) {
        final String propertyValue = getPropertyAsString(propertyName);
        return propertyValue != null ? propertyValue : defaultValue;
    }

    public List<String> getPropertyAsList(String propertyName) {
        final PropertyValue propertyValue = properties.get(propertyName);
        if (propertyValue == null) {
            return null;
        }
        return propertyValue.getListValue();
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
        for (String propertyName : migrationEnvironment.getPropertyNames()) {
            setProperty(propertyName, migrationEnvironment.getPropertyAsString(propertyName));
        }
    }

    public Set<String> getPropertyNames() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    private static class PropertyValue {
        private final String stringValue;
        private List<String> listValue;
        private PropertyValue(String stringValue) {
            this.stringValue = stringValue;
        }

        public String getStringValue() {
            return stringValue;
        }

        public List<String> getListValue() {
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
    }
}
