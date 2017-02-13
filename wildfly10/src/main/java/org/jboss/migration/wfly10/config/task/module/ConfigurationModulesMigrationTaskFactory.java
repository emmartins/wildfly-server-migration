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

package org.jboss.migration.wfly10.config.task.module;

import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.jboss.JBossServerConfigurationPath;
import org.jboss.migration.core.jboss.ModulesMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.TaskContext;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * @author emmartins
 */
public class ConfigurationModulesMigrationTaskFactory<S extends JBossServer<S>> implements ServerConfigurationMigration.XMLConfigurationSubtaskFactory<JBossServerConfigurationPath<S>> {

    private final Map<String, List<ModulesFinder>> modulesFinders;

    protected ConfigurationModulesMigrationTaskFactory(Builder<S> builder) {
        this.modulesFinders = Collections.unmodifiableMap(builder.modulesFinders);
    }

    @Override
    public ServerMigrationTask getTask(final JBossServerConfigurationPath<S> source, final Path xmlConfigurationPath, final WildFlyServer10 target) {
        return new SkippableByEnvServerMigrationTask(new Task(source.getServer(), target, xmlConfigurationPath, modulesFinders));
    }

    private static class Task extends ModulesMigrationTask {

        private final ServerMigrationTaskName taskName;
        private final Path xmlConfigurationPath;
        private final Map<String, List<ModulesFinder>> modulesFinders;

        public Task(JBossServer source, JBossServer target, Path xmlConfigurationPath, Map<String, List<ModulesFinder>> modulesFinders) {
            super(source, target, "configuration");
            this.taskName = new ServerMigrationTaskName.Builder("modules.migrate-modules-requested-by-configuration").addAttribute("path", xmlConfigurationPath.toString()).build();
            this.xmlConfigurationPath = xmlConfigurationPath;
            this.modulesFinders = modulesFinders;
        }

        @Override
        public ServerMigrationTaskName getName() {
            return taskName;
        }

        @Override
        protected void migrateModules(ModuleMigrator moduleMigrator, TaskContext context) {
            try (InputStream in = new BufferedInputStream(new FileInputStream(xmlConfigurationPath.toFile()))) {
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(in);
                reader.require(START_DOCUMENT, null, null);
                while (reader.hasNext()) {
                    if (reader.next() == START_ELEMENT) {
                        processElement(reader, moduleMigrator, context);
                    }
                }
            } catch (Exception e) {
                throw new ServerMigrationFailureException(e);
            }
        }

        protected void processElement(XMLStreamReader reader, ModuleMigrator moduleMigrator, TaskContext context) throws IOException {
            final List<ModulesFinder> elementModulesFinders = modulesFinders.get(reader.getLocalName());
            if (elementModulesFinders != null) {
                for (ModulesFinder modulesFinder : elementModulesFinders) {
                    modulesFinder.processElement(reader, moduleMigrator, context);
                }
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
        void processElement(XMLStreamReader reader, ModulesMigrationTask.ModuleMigrator moduleMigrator, TaskContext context) throws IOException;
    }

    public static class Builder<S extends JBossServer<S>> {
        private final Map<String, List<ModulesFinder>> modulesFinders = new HashMap<>();

        public synchronized Builder<S> modulesFinder(ModulesFinder modulesFinder) {
            List<ModulesFinder> elementModulesFinders = modulesFinders.get(modulesFinder.getElementLocalName());
            if (elementModulesFinders == null) {
                elementModulesFinders = new ArrayList<>();
                modulesFinders.put(modulesFinder.getElementLocalName(), elementModulesFinders);
            }
            elementModulesFinders.add(modulesFinder);
            return this;
        }

        public ConfigurationModulesMigrationTaskFactory<S> build() {
            return new ConfigurationModulesMigrationTaskFactory<>(this);
        }
    }
}
