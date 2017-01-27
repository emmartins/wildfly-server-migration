package org.jboss.migration.wfly10.config.task.management.subsystem;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.resource.composite.ManageableResourceCompositeTask;

import java.util.Collection;

/**
 * @author emmartins
 */
public class SubsystemConfigurationTask<S> extends ManageableResourceCompositeTask<S, SubsystemConfiguration> {

    protected SubsystemConfigurationTask(BaseBuilder<S, ?> builder, S source, Collection<? extends ManageableResource> resources) {
        super(builder, source, resources);
    }

    public interface SubtaskFactory<S> extends ManageableResourceCompositeTask.SubtaskFactory<S, SubsystemConfiguration> {
    }

    protected static abstract class BaseBuilder<S, B extends BaseBuilder<S, B>> extends ManageableResourceCompositeTask.BaseBuilder<S, SubsystemConfiguration, B> {
        protected BaseBuilder(String subsystem, ServerMigrationTaskName taskName) {
            super(taskName, ManageableResourceSelectors.selectResources(SubsystemConfiguration.class, subsystem));
        }
        public B subtask(SubtaskFactory<S> subtaskFactory) {
            return super.subtask(subtaskFactory);
        }
    }

    public static class Builder<S> extends BaseBuilder<S, Builder<S>> {
        public Builder(String subsystem, ServerMigrationTaskName taskName) {
            super(subsystem, taskName);
        }

        @Override
        public ServerMigrationTask build(S source, Collection<? extends ManageableResource> resources) {
            return new SubsystemConfigurationTask<>(this, source, resources);
        }
    }

}
