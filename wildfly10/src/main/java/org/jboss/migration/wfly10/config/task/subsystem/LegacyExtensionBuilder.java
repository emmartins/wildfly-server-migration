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
package org.jboss.migration.wfly10.config.task.subsystem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class LegacyExtensionBuilder {

    private final List<Subsystem> subsystems = new ArrayList<>();
    private final String name;

    public LegacyExtensionBuilder(String name) {
        this.name = name;
    }

    public LegacyExtensionBuilder subsystem(String subsystemName) {
        return subsystem(subsystemName, null);
    }

    public LegacyExtensionBuilder subsystem(String subsystemName, String namespaceWithoutVersion) {
        subsystems.add(new Subsystem(subsystemName, namespaceWithoutVersion));
        return this;
    }

    public LegacyExtension build() {
        final LegacyExtension extension = new LegacyExtension(name);
        for (Subsystem subsystem : subsystems) {
            extension.subsystems.add(new LegacySubsystem(subsystem.name, subsystem.namespaceWithoutVersion, extension));
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
