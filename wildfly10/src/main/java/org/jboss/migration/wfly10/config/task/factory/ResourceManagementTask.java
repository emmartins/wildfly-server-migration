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
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.management.ResourceManagement;
import org.jboss.migration.wfly10.config.task.executor.ResourceManagementSubtaskExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class ResourceManagementTask<S, R extends ResourceManagement> extends ParentTask {

    protected ResourceManagementTask(BaseBuilder<S, R, ?, ?> builder, List<ParentTask.Subtasks> subtasks) {
        super(builder, subtasks);
    }

    protected static abstract class BaseBuilder<S, R extends ResourceManagement, T extends ResourceManagementSubtaskExecutor<S, R>, B extends BaseBuilder<S, R, T, B>> extends ParentTask.BaseBuilder<T, B> {

        public BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        public ServerMigrationTask build(final S source, final R resourceManagement) {
            return build(source, Collections.singleton(resourceManagement));
        }

        public ServerMigrationTask build(final S source, final Collection<R> resourceManagements) {
            if (resourceManagements == null || resourceManagements.isEmpty()) {
                return null;
            }
            final List<ParentTask.Subtasks> subtasksList = new ArrayList<>();
            for (final ResourceManagementSubtaskExecutor<S, R> subtask : super.subtasks) {
                subtasksList.add(new ParentTask.Subtasks() {
                    @Override
                    public void run(TaskContext context) throws Exception {
                        for (R r : resourceManagements) {
                            subtask.executeSubtasks(source, r, context);
                        }
                    }
                });
            }
            return build(subtasksList);
        }

        protected abstract ServerMigrationTask build(List<ParentTask.Subtasks> subtasks);
    }

    public static class Builder<S, R extends ResourceManagement> extends BaseBuilder<S, R, ResourceManagementSubtaskExecutor<S, R>, Builder<S, R>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        protected ServerMigrationTask build(List<Subtasks> subtasks) {
            return new ResourceManagementTask<>(this, subtasks);
        }
    }
}
