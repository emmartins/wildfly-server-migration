package org.jboss.migration.wfly10.config.task.management;

import org.jboss.migration.core.BuildableTask;
import org.jboss.migration.core.CompositeBuildableTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableResource;

/**
 * @author emmartins
 */
public class ManageableResourceTask2 extends BuildableTask {

    public ManageableResourceTask2(Builder builder) {
        super(builder);
    }

    public interface Runnable<S, R extends ManageableResource> {
        ServerMigrationTaskResult run(S source, R resource, TaskContext context) throws Exception;
    }

    protected static class RunnableAdapter<S, R extends ManageableResource> implements BuildableTask.Runnable {

        private final S source;
        private final R resource;
        private final Runnable<S, R> runnable;

        public RunnableAdapter(S source, R resource, Runnable<S, R> runnable) {
            this.source = source;
            this.resource = resource;
            this.runnable = runnable;
        }

        @Override
        public ServerMigrationTaskResult run(TaskContext context) throws Exception {
            return runnable.run(source, resource, context);
        }
    }

    protected static class Builder<S, R extends ManageableResource, B extends Builder<S, R, B>> extends BuildableTask.Builder<RunnableAdapter<S, R>, B> {

        public Builder(ServerMigrationTaskName name) {
            this(name, new CompositeBuildableTask.Runnable());
        }

        public Builder(ServerMigrationTaskName name, Runnable<S, R> runnable) {
            super(name, runnable);
        }

        public B succeedIfHasSuccessfulSubtasks() {
            runnable.succeedIfHasSuccessfulSubtasks = true;
            return (B) this;
        }

        public B succeedAlways() {
            runnable.succeedIfHasSuccessfulSubtasks = false;
            return (B) this;
        }

        public B subtask(ServerMigrationTask subtask) {
            runnable.subtasks.add(subtask);
            return (B) this;
        }

        @Override
        public CompositeBuildableTask build() {
            return new CompositeBuildableTask(this);
        }
    }
}
