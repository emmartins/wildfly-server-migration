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
package org.wildfly.migration.wildfly.standalone.config;

/**
 * @author emmartins
 */
public class Subsystem {

    private final String name;
    private final boolean migrationRequired;

    public Subsystem(String name) {
        this(name, false);
    }

    public Subsystem(String name, boolean migrationRequired) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.migrationRequired = migrationRequired;
    }

    public String getName() {
        return name;
    }

    public boolean isMigrationRequired() {
        return migrationRequired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Subsystem subsystem = (Subsystem) o;
        return name.equals(subsystem.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
