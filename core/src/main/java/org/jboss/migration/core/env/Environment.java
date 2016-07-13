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

import java.util.List;
import java.util.Properties;

/**
 * @author emmartins
 */
public interface Environment {

    Boolean getPropertyAsBoolean(String propertyName);

    Boolean getPropertyAsBoolean(String propertyName, Boolean defaultValue);

    String getPropertyAsString(String propertyName);

    String getPropertyAsString(String propertyName, String defaultValue);

    List<String> getPropertyAsList(String propertyName);

    List<String> getPropertyAsList(String propertyName, List<String> defaultValue);

    Boolean requirePropertyAsBoolean(String propertyName) throws ServerMigrationFailedException;

    String requirePropertyAsString(String propertyName, boolean failIfEmpty) throws ServerMigrationFailedException;

    List<String> requirePropertyAsList(String propertyName, boolean failIfEmpty) throws ServerMigrationFailedException;

    String setProperty(String propertyName, String propertyValue);

    void setProperties(Properties properties);

    List<String> getPropertyNames();

    List<String> getPropertyNamesReaded();
}
