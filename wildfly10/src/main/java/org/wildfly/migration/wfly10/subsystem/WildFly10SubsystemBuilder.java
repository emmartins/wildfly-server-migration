/*
 * Copyright 2015 Red Hat, Inc.
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
package org.wildfly.migration.wfly10.subsystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link WildFly10Subsystem}.
 * @author emmartins
 */
public class WildFly10SubsystemBuilder {

    private final List<WildFly10SubsystemMigrationTask> tasks = new ArrayList<>();
    private WildFly10Extension extension;
    private String name;

    /**
     * Sets the subsystem extension.
     * @param extension
     * @return
     */
    public WildFly10SubsystemBuilder setExtension(WildFly10Extension extension) {
        this.extension = extension;
        return this;
    }

    /**
     * Sets the subsystem name.
     * @param name
     * @return
     */
    public WildFly10SubsystemBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Adds a config migration task to the subsystem.
     * @param configMigrationTask
     * @return
     */
    public WildFly10SubsystemBuilder addTask(WildFly10SubsystemMigrationTask configMigrationTask) {
        tasks.add(configMigrationTask);
        return this;
    }

    /**
     * Builds the subsystem.
     * @return
     */
    WildFly10Subsystem build() {
        return new WildFly10Subsystem(name, tasks, extension);
    }
}
