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
public interface EnvironmentProperties {

    /**
     * the prefix for the name of extensions related properties
     */
    String EXTENSIONS_PROPERTIES_PREFIX = "extensions.";

    /**
     * a list with extensions to remove
     */
    String EXTENSIONS_REMOVE = EXTENSIONS_PROPERTIES_PREFIX + "remove";

    /**
     * a list with extensions to skip
     */
    String EXTENSIONS_SKIP = EXTENSIONS_PROPERTIES_PREFIX + "skip";

    /**
     * the prefix for the name of subsystems related properties
     */
    String SUBSYSTEMS_PROPERTIES_PREFIX = "subsystems.";

    /**
     * a list with subsystems to remove
     */
    String SUBSYSTEMS_REMOVE = SUBSYSTEMS_PROPERTIES_PREFIX + "remove";

    /**
     * a list with subsystems to skip
     */
    String SUBSYSTEMS_SKIP = SUBSYSTEMS_PROPERTIES_PREFIX + "skip";
}
