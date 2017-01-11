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

import org.jboss.migration.core.ParentTask;
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
public abstract class AbstractSubsystemConfigurationTask<S> extends SubsystemsManagementTask<S> {

    protected AbstractSubsystemConfigurationTask(SubsystemsManagementTask.Builder<S> builder, S source, SubsystemsManagement... resourceManagements) {
        super(builder, source, resourceManagements);
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
/*
        protected ParentTask build() {
            return new SubsystemManagementParentTask(this);
        }

        private SubtaskExecutorContextFactory<Context<S>> getSubtaskExecutorContextFactory(final S source, final SubsystemsManagement subsystemsManagement) {
            return new SubtaskExecutorContextFactory<Context<S>>() {
                @Override
                public Context<S> getSubtaskExecutorContext(TaskContext context) throws Exception {
                    return new Context<>(source, extensionModule, subsystemName, subsystemsManagement, context);
                }
            };
        }
*/
        public ParentTask build(final S source, final SubsystemsManagement resourcesManagement) {
            final ParentTask.Builder builder = new ParentTask.Builder(this);
            for (ParentTask.Subtasks<Context<S>> subtask : subtasks) {
                builder.subtask(new )
            }

            return build(Collections.singletonList(getSubtaskExecutorContextFactory(source, resourcesManagement)));
        }
/*
        public ParentTask<Context<S>> build(S source, HostControllerConfiguration configuration) throws IOException {
            final List<SubtaskExecutorContextFactory<Context<S>>> subtaskContextFactories = new ArrayList<>();
            final ProfilesManagement profilesManagement = configuration.getProfilesManagement();
            for (String profileName : profilesManagement.getResourceNames()) {
                final ProfileManagement profileManagement = profilesManagement.getProfileManagement(profileName);
                subtaskContextFactories.add(getSubtaskExecutorContextFactory(source, profileManagement.getSubsystemsManagement()));
            }
            return build(subtaskContextFactories);
        }
*/
    }

    public static class SubtasksAdapter<S> implements ParentTask.Subtasks {
        private final Subtasks<S> subtasks;
        private final S source;
        private final SubsystemsManagement subsystemsManagement;

        public SubtasksAdapter(Subtasks<S> subtasks, S source, SubsystemsManagement subsystemsManagement) {
            this.subtasks = subtasks;
            this.source = source;
            this.subsystemsManagement = subsystemsManagement;
        }

        @Override
        public void run(TaskContext taskContext) throws Exception {
            final Context<S> context = new Context<>() {

            }
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

    public interface Subtasks<S> extends ParentTask.Subtasks<Context<S>> {

    }

}
