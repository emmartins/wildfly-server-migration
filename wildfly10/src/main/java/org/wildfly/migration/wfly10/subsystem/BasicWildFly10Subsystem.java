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

import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;

/**
 * @author emmartins
 */
public class BasicWildFly10Subsystem implements WildFly10Subsystem {

    private final String name;
    private final WildFly10Extension extension;

    public BasicWildFly10Subsystem(String name, WildFly10Extension extension) {
        this.name = name;
        this.extension = extension;
    }

    @Override
    public WildFly10Extension getExtension() {
        return extension;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void migrate(WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
        // nothing to do
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BasicWildFly10Subsystem subsystem = (BasicWildFly10Subsystem) o;
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
