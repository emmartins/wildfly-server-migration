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
package org.jboss.migration.wfly10.subsystem;

/**
 * Environment properties related to extensions and their subsystems.
 * @author emmartins
 */
public class EnvironmentProperties {

    /**
     * the prefix for the name of extensions related properties
     */
    public static final String EXTENSIONS_PROPERTIES_PREFIX = "extensions.";

    /**
     * a list with extensions to remove
     */
    public static final String EXTENSIONS_REMOVE = EXTENSIONS_PROPERTIES_PREFIX + "remove";

    /**
     * the prefix for the name of subsystems related properties
     */
    public static final String SUBSYSTEMS_PROPERTIES_PREFIX = "subsystems.";

    /**
     * a list with subsystems to remove
     */
    public static final String SUBSYSTEMS_REMOVE = SUBSYSTEMS_PROPERTIES_PREFIX + "remove";

    /**
     * Retrieves the prefix for all env properties in the context of a subsystem task.
     * @param subsystem the subsystem in context
     * @return the prefix for all env properties in the context of a subsystem task
     */
    public static String getSubsystemTaskPropertiesPrefix(String subsystem) {
        return new StringBuilder("subsystem.").append(subsystem).append('.').toString();
    }

    /**
     * Retrieves the prefix for all env properties in the context of a subsystem subtask.
     * @param subsystem the subsystem in context
     * @param subtask the subtask in context
     * @return the prefix for all env properties in the context of a subsystem subtask
     */
    public static String getSubsystemSubtaskPropertiesPrefix(String subsystem, String subtask) {
        return new StringBuilder(getSubsystemTaskPropertiesPrefix(subsystem)).append(subtask).append('.').toString();
    }
}
