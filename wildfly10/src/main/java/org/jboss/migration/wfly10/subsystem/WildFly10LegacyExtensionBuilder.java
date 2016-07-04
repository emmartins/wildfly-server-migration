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
package org.jboss.migration.wfly10.subsystem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class WildFly10LegacyExtensionBuilder {

    private final List<Subsystem> subsystems = new ArrayList<>();
    private String name;

    public WildFly10LegacyExtensionBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public WildFly10LegacyExtensionBuilder addMigratedSubsystem(String subsystemName) {
        return addMigratedSubsystem(subsystemName, null);
    }

    public WildFly10LegacyExtensionBuilder addMigratedSubsystem(String subsystemName, String namespaceWithoutVersion) {
        subsystems.add(new Subsystem(subsystemName, namespaceWithoutVersion));
        return this;
    }

    public LegacyWildFly10Extension build() {
        final LegacyWildFly10Extension extension = new LegacyWildFly10Extension(name);
        for (Subsystem subsystem : subsystems) {
            extension.subsystems.add(new WildFly10LegacySubsystem(subsystem.name, subsystem.namespaceWithoutVersion, extension));
        }
        return extension;
    }

    private static class Subsystem {
        final String name;
        final String namespaceWithoutVersion;
        public Subsystem(String name, String namespaceWithoutVersion) {
            this.name = name;
            this.namespaceWithoutVersion = namespaceWithoutVersion == null ? ("urn:jboss:domain:"+name) : namespaceWithoutVersion;
        }
    }
}
