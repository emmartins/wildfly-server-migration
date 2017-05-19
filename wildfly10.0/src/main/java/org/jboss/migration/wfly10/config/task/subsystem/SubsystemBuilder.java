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

/**
 * Builder for {@link Subsystem}.
 * @author emmartins
 */
public class SubsystemBuilder {

    private Extension extension;
    private String name;
    private String namespaceWithoutVersion;

    /**
     * Sets the subsystem extension.
     * @param extension
     * @return
     */
    public SubsystemBuilder setExtension(Extension extension) {
        this.extension = extension;
        return this;
    }

    /**
     * Sets the subsystem name.
     * @param name
     * @return
     */
    public SubsystemBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the subsystem namespace, without the version suffix, e.g. urn:jboss:domain:ee
     * Please note that a namespace will be generated if not set, by concatenating "urn:jboss:domain:" with the subsystem name.
     * @param namespaceWithoutVersion
     * @return
     */
    public SubsystemBuilder setNamespaceWithoutVersion(String namespaceWithoutVersion) {
        this.namespaceWithoutVersion = namespaceWithoutVersion;
        return this;
    }

    /**
     * Builds the subsystem.
     * @return
     */
    Subsystem build() {
        return new Subsystem(name, namespaceWithoutVersion == null ? ("urn:jboss:domain:"+name) : namespaceWithoutVersion, extension);
    }
}
