/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.migration.core.jboss;

import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.ServerPath;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * @author emmartins
 */
public interface SJBossServer<S extends SJBossServer<S>> extends Server {

    Collection<ServerPath<S>> getStandaloneConfigs();

    Collection<ServerPath<S>> getDomainDomainConfigs();

    Collection<ServerPath<S>> getDomainHostConfigs();

    Path getDomainDir();

    Path getDomainConfigurationDir();

    Path getStandaloneDir();

    Path getStandaloneConfigurationDir();

    Modules getModules();

    class Module {
        private final Path moduleDir;
        private final ModuleSpecification moduleSpecification;

        public Module(Path moduleDir, ModuleSpecification moduleSpecification) {
            this.moduleDir = moduleDir;
            this.moduleSpecification = moduleSpecification;
        }

        public ModuleSpecification getModuleSpecification() {
            return moduleSpecification;
        }

        public Path getModuleDir() {
            return moduleDir;
        }
    }

    class Modules {

        private final Path modulesDir;
        private final Path systemLayersBaseDir;
        private final Path overlayDir;

        public Modules(Path serverBaseDir) {
            this.modulesDir = serverBaseDir.resolve("modules");
            this.systemLayersBaseDir = modulesDir.resolve("system").resolve("layers").resolve("base");
            final Path overlaysDir = systemLayersBaseDir.resolve(".overlays");
            final Path overlaysFile = overlaysDir.resolve(".overlays");
            if (Files.exists(overlaysFile)) {
                try {
                    final String activeOverlayFileName = new String(Files.readAllBytes(overlaysFile)).trim();
                    if (!activeOverlayFileName.isEmpty()) {
                        overlayDir = overlaysDir.resolve(activeOverlayFileName);
                    } else {
                        overlayDir = null;
                    }
                } catch (IOException e) {
                    throw new ServerMigrationFailureException("failed to read overlays file", e);
                }
            } else {
                overlayDir = null;
            }
        }

        public Path getModulesDir() {
            return modulesDir;
        }

        public Module getModule(ModuleIdentifier moduleId) throws ServerMigrationFailureException {
            final Path moduleDir = getModuleDir(moduleId);
            if (!Files.exists(moduleDir)) {
                return null;
            }
            final Path moduleSpecPath = moduleDir.resolve("module.xml");
            if (!Files.exists(moduleSpecPath)) {
                return null;
            }
            final ModuleSpecification moduleSpecification;
            try {
                moduleSpecification = ModuleSpecification.Parser.parse(moduleSpecPath);
            } catch (XMLStreamException | IOException e) {
                throw new ServerMigrationFailureException(e);
            }
            return new Module(moduleDir, moduleSpecification);
        }

        public Module getModule(String moduleId) throws ServerMigrationFailureException {
            return getModule(ModuleIdentifier.fromString(moduleId));
        }

        public Path getModuleDir(ModuleIdentifier moduleId) {
            if (moduleId == null) {
                throw new IllegalArgumentException("The module identifier cannot be null.");
            }
            final Path modulePath = Paths.get(new StringBuilder(moduleId.getName().replace('.', File.separatorChar)).
                    append(File.separator).
                    append(moduleId.getSlot()).
                    toString());
            if (overlayDir != null) {
                final Path overlayModuleDir = overlayDir.resolve(modulePath);
                if (Files.exists(overlayModuleDir)) {
                    return overlayModuleDir;
                }
            }
            final Path systemLayersBaseModuleDir = systemLayersBaseDir.resolve(modulePath);
            return systemLayersBaseModuleDir;
        }
    }
}
