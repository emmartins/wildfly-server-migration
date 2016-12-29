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

package org.jboss.migration.core.jboss;

import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public class ModulesMigrationTask implements ServerMigrationTask {

    public static final String MODULES = "modules";
    public static final String ENVIRONMENT_PROPERTIES_PREFIX = MODULES + ".";
    public static final String ENVIRONMENT_PROPERTY_INCLUDES = ENVIRONMENT_PROPERTIES_PREFIX + "includes";
    public static final String ENVIRONMENT_PROPERTY_EXCLUDES = ENVIRONMENT_PROPERTIES_PREFIX + "excludes";

    private static final ServerMigrationTaskName TASK_NAME = new ServerMigrationTaskName.Builder(MODULES).build();

    private final JBossServer source;
    private final JBossServer target;
    private final String requestedBy;

    public ModulesMigrationTask(JBossServer source, JBossServer target) {
        this(source, target, "environment");
    }

    protected ModulesMigrationTask(JBossServer source, JBossServer target, String requestedBy) {
        this.source = source;
        this.target = target;
        this.requestedBy = requestedBy;
    }

    @Override
    public ServerMigrationTaskName getName() {
        return TASK_NAME;
    }

    @Override
    public ServerMigrationTaskResult run(TaskContext context) {
        if (new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), getName().getName()).isSkippedByEnvironment()) {
            return ServerMigrationTaskResult.SKIPPED;
        }
        context.getLogger().infof("Migrating modules requested by %s...", requestedBy);
        final ModuleMigrator moduleMigrator = new ModuleMigrator(source, target, context.getServerMigrationContext().getMigrationEnvironment());
        migrateModules(moduleMigrator, context);
        if (context.hasSucessfulSubtasks()) {
            return ServerMigrationTaskResult.SUCCESS;
        } else {
            context.getLogger().infof("No modules required migration.", requestedBy);
            return ServerMigrationTaskResult.SKIPPED;
        }
    }

    protected void migrateModules(ModuleMigrator moduleMigrator, TaskContext context) {
        final List<String> includedModules = context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsList(ENVIRONMENT_PROPERTY_INCLUDES, Collections.emptyList());
        for (String module : includedModules) {
            moduleMigrator.migrateModule(module, "requested by environment", context);
        }
    }

    public static class ModuleMigrator {

        private final JBossServer.Modules sourceModules;
        private final JBossServer.Modules targetModules;
        private final Set<ModuleIdentifier> excludedByEnvironment;

        protected ModuleMigrator(JBossServer source, JBossServer target, MigrationEnvironment environment) {
            this.sourceModules = source.getModules();
            this.targetModules = target.getModules();
            this.excludedByEnvironment = new HashSet<>();
            for (String excludedModule : environment.getPropertyAsList(ENVIRONMENT_PROPERTY_EXCLUDES, Collections.emptyList())) {
                this.excludedByEnvironment.add(ModuleIdentifier.fromString(excludedModule));
            }
        }

        public void migrateModule(String moduleId, String reason, final TaskContext context) {
            migrateModule(ModuleIdentifier.fromString(moduleId), reason, context);
        }

        public void migrateModule(final ModuleIdentifier moduleIdentifier, final String reason, final TaskContext context) throws IllegalStateException {
            if (excludedByEnvironment.contains(moduleIdentifier)) {
                context.getLogger().infof("Skipping module %s migration, it's excluded by environment.", moduleIdentifier);
                return;
            }
            final JBossServer.Module sourceModule = sourceModules.getModule(moduleIdentifier);
            if (sourceModule == null) {
                throw new IllegalStateException("Migration of module "+moduleIdentifier+" required, but module not found in source server.");
            }
            if (targetModules.getModule(moduleIdentifier) != null) {
                context.getLogger().debugf("Skipping module %s migration, already exists in target.", moduleIdentifier, reason);
                return;
            }
            final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder("migrate-module").addAttribute("id", moduleIdentifier.toString()).build();
            final ServerMigrationTask subtask = new ServerMigrationTask() {
                @Override
                public ServerMigrationTaskName getName() {
                    return taskName;
                }

                @Override
                public ServerMigrationTaskResult run(TaskContext context) {
                    context.getServerMigrationContext().getMigrationFiles().copy(sourceModule.getModuleDir(), targetModules.getModuleDir(moduleIdentifier));
                    context.getLogger().infof("Module %s migrated.", moduleIdentifier);
                    return new ServerMigrationTaskResult.Builder()
                            .success()
                            .addAttribute("reason", reason)
                            .build();
                }
            };
            context.execute(subtask);
            for (ModuleSpecification.Dependency dependency : sourceModule.getModuleSpecification().getDependencies()) {
                migrateModule(dependency.getId(), "migrated module " + moduleIdentifier + " depends on it", context);
            }
        }
    }
}
