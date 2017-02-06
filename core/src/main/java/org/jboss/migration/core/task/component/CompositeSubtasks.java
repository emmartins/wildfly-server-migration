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

package org.jboss.migration.core.task.component;

import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class CompositeSubtasks<P extends BuildParameters> implements TaskRunnable {

    private final List<TaskRunnable.Builder<? super P>> builders;
    private final P params;

    protected CompositeSubtasks(BaseBuilder<P, ?> baseBuilder, P params) {
        this.builders = new ArrayList<>(baseBuilder.builders);
        this.params = params;
    }

    @Override
    public ServerMigrationTaskResult run(TaskContext context) {
        final ServerMigrationTaskResult.Builder result = new ServerMigrationTaskResult.Builder().skipped();
        for (TaskRunnable.Builder<? super P> builder : builders) {
            if (builder.build(params).run(context).getStatus() == ServerMigrationTaskResult.Status.SUCCESS) {
                result.success();
            }
        }
        return result.build();
    }

    public static abstract class BaseBuilder<P extends BuildParameters, T extends BaseBuilder<P, T>> implements CompositeSubtasksBuilder<P, T> {

        private final List<TaskRunnable.Builder<? super P>> builders = new ArrayList<>();

        protected abstract T getThis();

        public T subtask(TaskRunnable.Builder<? super P> runnableBuilder) {
            this.builders.add(runnableBuilder);
            return getThis();
        }

        @Override
        public CompositeSubtasks build(P params) {
            return new CompositeSubtasks(this, params);
        }
    }

    public static class Builder<P extends BuildParameters> extends BaseBuilder<P, Builder<P>> {
        @Override
        protected Builder<P> getThis() {
            return this;
        }
    }
}
