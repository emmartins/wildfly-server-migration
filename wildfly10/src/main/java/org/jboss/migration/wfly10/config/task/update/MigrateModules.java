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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.jboss.ModuleIdentifier;
import org.jboss.migration.core.jboss.ModuleSpecification;
import org.jboss.migration.wfly10.WildFlyServer10;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * @author emmartins
 */
public class MigrateModules<S extends JBossServer<S>> implements ServerConfigurationMigration.XMLConfigurationSubtaskFactory<ServerPath<S>> {

    public static final ServerMigrationTaskName TASK_NAME = new ServerMigrationTaskName.Builder("migrate-modules").build();
    private final List<ModulesFinder> modulesFinders;

    protected MigrateModules(Builder<S> builder) {
        modulesFinders = Collections.unmodifiableList(builder.moduleFinders);
    }

    @Override
    public ServerMigrationTask getTask(final ServerPath<S> source, final Path xmlConfigurationPath, final WildFlyServer10 target) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return TASK_NAME;
            }

            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                final ModuleMigrator moduleMigrator = new ModuleMigrator(source.getServer(), target, context);
                try (InputStream in = new BufferedInputStream(new FileInputStream(xmlConfigurationPath.toFile()))) {
                    XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(in);
                    reader.require(START_DOCUMENT, null, null);
                    while (reader.hasNext()) {
                        if (reader.next() == START_ELEMENT) {
                            processElement(reader, moduleMigrator);
                        }
                    }
                }
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }

    protected void processElement(XMLStreamReader reader, ModuleMigrator moduleMigrator) throws IOException {
        for (ModulesFinder modulesFinder : modulesFinders) {
            if (reader.getLocalName().equals(modulesFinder.getElementLocalName())) {
                modulesFinder.processElement(reader, moduleMigrator);
            }
        }
    }

    public static class ModuleMigrator {

        private final JBossServer.Modules sourceModules;
        private final JBossServer.Modules targetModules;
        private final ServerMigrationTaskContext context;

        private ModuleMigrator(JBossServer source, JBossServer target, ServerMigrationTaskContext context) {
            this.sourceModules = source.getModules();
            this.targetModules = target.getModules();
            this.context = context;
        }

        public void migrateModule(String moduleId, String reason) throws IOException {
            migrateModule(ModuleIdentifier.fromString(moduleId), reason);
        }

        public void migrateModule(ModuleIdentifier moduleIdentifier, String reason) throws IOException {
            final JBossServer.Module sourceModule = sourceModules.getModule(moduleIdentifier);
            if (sourceModule == null) {
                throw new IOException("Migration of module %s failed, not found in source server.");
            }
            if (targetModules.getModule(moduleIdentifier) != null) {
                context.getLogger().debugf("Skipping module %s migration, already exists in target.", moduleIdentifier, reason);
                return;
            }
            context.getServerMigrationContext().getMigrationFiles().copy(sourceModule.getModuleDir(), targetModules.getModuleDir(moduleIdentifier));
            context.getLogger().infof("Module %s migrated.", moduleIdentifier);
            for (ModuleSpecification.Dependency dependency : sourceModule.getModuleSpecification().getDependencies()) {
                migrateModule(dependency.getId(), "Migrated module "+moduleIdentifier+" dependencies.");
            }
        }
    }

    /**
     * A module finder.
     */
    public interface ModulesFinder {
        /**
         * The XML Element's local name the processor is interested.
         * @return
         */
        String getElementLocalName();
        /**
         *
         * @param reader the XML stream reader, positioned at the start of an element of interest
         * @param moduleMigrator the module migrator
         */
        void processElement(XMLStreamReader reader, ModuleMigrator moduleMigrator) throws IOException;
    }

    public static class Builder<S extends JBossServer<S>> {

        private final List<ModulesFinder> moduleFinders = new ArrayList<>();

        public static final String SERVER_MIGRATION_TASK_NAME_REMOVE_SUBSYSTEM = "remove-unsupported-subsystem";
        public static final String SERVER_MIGRATION_TASK_NAME_REMOVE_EXTENSION = "remove-unsupported-extension";
        public static final String SERVER_MIGRATION_TASK_NAME_ATTRIBUTE_MODULE = "module";
        public static final String SERVER_MIGRATION_TASK_NAME_ATTRIBUTE_NAMESPACE = "namespace";

        public Builder<S> modulesFinder(ModulesFinder modulesFinder) {
            moduleFinders.add(modulesFinder);
            return this;
        }

        public MigrateModules<S> build() {
            return new MigrateModules<>(this);
        }
    }
}
