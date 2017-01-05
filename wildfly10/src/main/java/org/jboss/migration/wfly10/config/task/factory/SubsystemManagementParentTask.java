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

package org.jboss.migration.wfly10.config.task.factory;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ParentTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.TaskContextDelegate;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ProfileManagement;
import org.jboss.migration.wfly10.config.management.ProfilesManagement;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.subsystem.EnvironmentProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class SubsystemManagementParentTask<S> extends ParentTask<SubsystemManagementParentTask.Context<S>> {

    public SubsystemManagementParentTask(BaseBuilder<Context<S>, ?> builder, List<SubtaskExecutorContextFactory<Context<S>>> subtaskContextFactories) {
        super(builder, subtaskContextFactories);
    }

    public static class Builder<S> extends BaseBuilder<Context<S>, Builder<S>> {

        private final String extensionModule;
        private final String subsystemName;

        public Builder(final String extensionModule, final String subsystemName, ServerMigrationTaskName taskName) {
            super(taskName);
            this.extensionModule = extensionModule;
            this.subsystemName = subsystemName;
            skipper(new Skipper() {
                @Override
                public boolean isSkipped(TaskContext context) {
                    return new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemTaskPropertiesPrefix(subsystemName)).isSkippedByEnvironment();
                }
            });
        }

        @Override
        protected ParentTask<Context<S>> build(List<SubtaskExecutorContextFactory<Context<S>>> subtaskContextFactories) {
            return new SubsystemManagementParentTask<>(this, subtaskContextFactories);
        }

        private SubtaskExecutorContextFactory<Context<S>> getSubtaskExecutorContextFactory(final S source, final SubsystemsManagement subsystemsManagement) {
            return new SubtaskExecutorContextFactory<Context<S>>() {
                @Override
                public Context<S> getSubtaskExecutorContext(TaskContext context) throws Exception {
                    return new Context<>(source, extensionModule, subsystemName, subsystemsManagement, context);
                }
            };
        }

        public ParentTask<Context<S>> build(S source, SubsystemsManagement resourcesManagement) {
            return build(Collections.singletonList(getSubtaskExecutorContextFactory(source, resourcesManagement)));
        }

        public ParentTask<Context<S>> build(S source, HostControllerConfiguration configuration) throws IOException {
            final List<SubtaskExecutorContextFactory<Context<S>>> subtaskContextFactories = new ArrayList<>();
            final ProfilesManagement profilesManagement = configuration.getProfilesManagement();
            for (String profileName : profilesManagement.getResourceNames()) {
                final ProfileManagement profileManagement = profilesManagement.getProfileManagement(profileName);
                subtaskContextFactories.add(getSubtaskExecutorContextFactory(source, profileManagement.getSubsystemsManagement()));
            }
            return build(subtaskContextFactories);
        }
    }

    public static class Context<S> extends TaskContextDelegate {

        private final S source;
        private final String extension;
        private final String subsystem;
        private final SubsystemsManagement resourcesManagement;

        private Context(S source, String extension, String subsystem, SubsystemsManagement resourcesManagement, TaskContext taskContext) {
            super(taskContext);
            this.source = source;
            this.extension = extension;
            this.subsystem = subsystem;
            this.resourcesManagement = resourcesManagement;
        }

        public S getSource() {
            return source;
        }

        public String getExtension() {
            return extension;
        }

        public String getSubsystem() {
            return subsystem;
        }

        public SubsystemsManagement getResourcesManagement() {
            return resourcesManagement;
        }

        public String getConfigName() {
            return resourcesManagement.getResourcePathAddress(subsystem).toCLIStyleString();
        }
    }

    public interface SubtaskExecutor<S> extends ParentTask.SubtaskExecutor<Context<S>> {

    }

}
