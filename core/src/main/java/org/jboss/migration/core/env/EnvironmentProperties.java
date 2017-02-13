/*
 * Copyright 2017 Red Hat, Inc.
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author emmartins
 */
public interface EnvironmentProperties {

    static EnvironmentProperty<Boolean> newBooleanProperty(String name) {
        return newBooleanProperty(name, null);
    }

    static EnvironmentProperty<Boolean> newBooleanProperty(String name, Boolean defaultValue) {
        return newProperty(name, defaultValue, stringValue -> stringValue != null ? Boolean.valueOf(stringValue) : null, tValue -> tValue != null ? tValue.toString() : null);
    }

    static EnvironmentProperty<Integer> newIntegerProperty(String name) {
        return newIntegerProperty(name, null);
    }

    static EnvironmentProperty<Integer> newIntegerProperty(String name, Integer defaultValue) {
        return newProperty(name, defaultValue, stringValue -> stringValue != null ? Integer.valueOf(stringValue) : null, tValue -> tValue != null ? tValue.toString() : null);
    }

    static EnvironmentProperty<String> newStringProperty(String name) {
        return newStringProperty(name, null);
    }

    static EnvironmentProperty<String> newStringProperty(String name, String defaultValue) {
        return newProperty(name, defaultValue, stringValue -> stringValue, stringValue -> stringValue);
    }

    static EnvironmentProperty<Path> newPathProperty(String name) {
        return newPathProperty(name, null);
    }

    static EnvironmentProperty<Path> newPathProperty(String name, Path defaultValue) {
        return newProperty(name, defaultValue, stringValue -> stringValue != null ? Paths.get(stringValue) : null, pathValue -> pathValue != null ? pathValue.toString() : null);
    }

    static EnvironmentProperty<List<String>> newStringListProperty(String name) {
        return newStringListProperty(name, null);
    }

    static EnvironmentProperty<List<String>> newStringListProperty(String name, List<String> defaultValue) {
        return newListProperty(name, defaultValue, stringValue -> stringValue, stringValue -> stringValue);
    }

    static <T> EnvironmentProperty<T> newProperty(String name, T defaultValue, Function<String, T> stringToValueMapper, Function<T, String> valueToStringMapper) {
        return new EnvironmentProperty<T>() {
            @Override
            public String getName() {
                return name;
            }
            @Override
            public T getValue(Environment environment) {
                final String stringValue = environment.getPropertyAsString(getName());
                final T tValue = stringValue != null ? stringToValueMapper.apply(stringValue.trim()) : null;
                return tValue != null ? tValue : defaultValue;
            }
            @Override
            public void setValue(T value, Environment environment) {
                final String string = valueToStringMapper.apply(value);
                environment.setProperty(getName(), string);
            }
        };
    }

    static <T> EnvironmentProperty<List<T>> newListProperty(String name, List<T> defaultValue, Function<String, T> stringToValueMapper, Function<T, String> valueToStringMapper) {
        return newProperty(name, defaultValue,
                stringValue -> stringValue != null ? Stream.of(stringValue.trim().split(",")).filter(s -> !s.trim().isEmpty()).map(stringToValueMapper).collect(toList()) : null,
                list -> list != null ? list.stream().map(value -> valueToStringMapper.apply(value)).collect(Collectors.joining(", ")) : null);
    }
}
