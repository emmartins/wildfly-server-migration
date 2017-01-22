package org.jboss.migration.wfly10.config.task.management.subsystem;

import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.ManageableResourceTask;
import org.jboss.migration.wfly10.config.task.subsystem.EnvironmentProperties;

import java.util.Collection;

/**
 * @author emmartins
 */
public class SubsystemConfigurationTask<S> extends ManageableResourceTask<S, SubsystemConfiguration> {

    public SubsystemConfigurationTask(ManageableResourceTask.BaseBuilder<S, SubsystemConfiguration, ?> builder, S source, Collection<? extends SubsystemConfiguration> manageableResources) {
        super(builder, source, manageableResources);
    }

    public interface Subtask<S> {
        ServerMigrationTaskName getName(SubsystemConfiguration subsystemConfiguration, TaskContext parentContext);
        ServerMigrationTaskResult run(SubsystemConfiguration subsystemConfiguration, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception;
    }

    protected static abstract class BaseBuilder<S, B extends BaseBuilder<S, B>> extends ManageableResourceTask.BaseBuilder<S, SubsystemConfiguration, B> {
        private final String extension;
        private final String subsystem;

        protected BaseBuilder(String extension, String subsystem, ServerMigrationTaskName taskName) {
            super(taskName);
            this.extension = extension;
            this.subsystem = subsystem;
        }

        public B subtask(final Subtask<S> subtask) {
            final ManageableResourceTask.SubtaskExecutor<S, SubsystemConfiguration> subtasks = (source, resources, context) -> {
                for (final SubsystemConfiguration subsystemConfiguration : resources) {
                    final ServerMigrationTaskName subtaskName = subtask.getName(subsystemConfiguration, context);
                    if (subtaskName != null) {
                        final TaskEnvironment taskEnvironment = new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemSubtaskPropertiesPrefix(subsystem, subtaskName.getName()));
                        final AbstractServerMigrationTask.Builder builder = new AbstractServerMigrationTask.Builder(subtaskName)
                                .skipper(new Skipper() {
                                    @Override
                                    public boolean isSkipped(TaskContext context) {
                                        return taskEnvironment.isSkippedByEnvironment();
                                    }
                                });
                        final ServerMigrationTask task = new AbstractServerMigrationTask(builder) {
                            @Override
                            protected ServerMigrationTaskResult runTask(TaskContext taskContext) throws Exception {
                                return subtask.run(subsystemConfiguration, taskContext, taskEnvironment);
                            }
                        };
                        context.execute(task);
                    }
                }
            };
            return subtask(subtasks);
        }
    }

    public static class Builder<S> extends BaseBuilder<S, Builder<S>> {
        public Builder(String extension, String subsystem, ServerMigrationTaskName taskName) {
            super(extension, subsystem, taskName);
        }

        @Override
        public ServerMigrationTask build(S source, Collection<? extends SubsystemConfiguration> resources) {
            return new SubsystemConfigurationTask<>(this, source, resources);
        }
    }

}
