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

import java.util.Properties;

/**
 * @author emmartins
 */
public class SystemEnvironment extends MigrationEnvironment {

    private static final String SYS_ENV_PROPERTY_NAME_PREFIX = "jboss.server.migration.";

    public static final SystemEnvironment INSTANCE = new SystemEnvironment();

    // package-private for testing
    SystemEnvironment() {
        super();
        final Properties systemProperties = System.getProperties();
        for (String systemPropertyName: systemProperties.stringPropertyNames()) {
            final String propertyValue = systemProperties.getProperty(systemPropertyName);
            if (systemPropertyName.length() > SYS_ENV_PROPERTY_NAME_PREFIX.length() && systemPropertyName.startsWith(SYS_ENV_PROPERTY_NAME_PREFIX)) {
                setProperty(systemPropertyName.substring(SYS_ENV_PROPERTY_NAME_PREFIX.length()), propertyValue);
            }
        }
    }
}
