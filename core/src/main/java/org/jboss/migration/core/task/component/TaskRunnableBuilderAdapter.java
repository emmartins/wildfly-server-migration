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

import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;

/**
 * @author emmartins
 */
public class TaskRunnableBuilderAdapter<T extends BuildParameters, R extends BuildParameters> implements TaskRunnable.Builder<T> {

    private final TaskRunnable.Builder<R> rBuilder;
    private final BuildParameters.Mapper<T, R> trMapper;

    public TaskRunnableBuilderAdapter(BuildParameters.Mapper<T, R> trMapper, TaskRunnable.Builder<R> rBuilder) {
        this.trMapper = trMapper;
        this.rBuilder = rBuilder;
    }

    public TaskRunnableBuilderAdapter(BuildParameters.Mapper<T, R> trMapper, ComponentTask.Builder<? super R, ?> rBuilder) {
        this(trMapper, ((params, taskName) -> context -> context.execute(rBuilder.build(params)).getResult()));
    }

    @Override
    public TaskRunnable build(T tParams, ServerMigrationTaskName taskName) {
        return context -> {
            final ServerMigrationTaskResult.Builder resultBuilder = new ServerMigrationTaskResult.Builder().skipped();
            for (R rParams : trMapper.apply(tParams)) {
                if (rBuilder.build(rParams, taskName).run(context).getStatus() == ServerMigrationTaskResult.Status.SUCCESS) {
                    resultBuilder.success();
                }
            }
            return resultBuilder.build();
        };
    }
}
