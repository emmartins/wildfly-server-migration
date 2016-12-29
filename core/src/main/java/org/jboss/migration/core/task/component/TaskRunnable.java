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

import java.util.Collection;

/**
 * @author emmartins
 */
public interface TaskRunnable {

    ServerMigrationTaskResult run(TaskContext context);

    /**
     *
     * @param <P>
     */
    @FunctionalInterface
    interface Builder<P extends BuildParameters> {

        TaskRunnable build(P params);

        static <T extends BuildParameters, R extends BuildParameters> TaskRunnable.Builder<R> of(BuildParameters.Mapper<R, T> mapper, TaskRunnable.Builder<? super T> tBuilder) {
            return r -> context -> {
                final ServerMigrationTaskResult.Builder resultBuilder = new ServerMigrationTaskResult.Builder().skipped();
                for (T t : mapper.apply(r)) {
                    if (tBuilder.build(t).run(context).getStatus() == ServerMigrationTaskResult.Status.SUCCESS) {
                        resultBuilder.success();
                    }
                }
                return resultBuilder.build();
            };
        }

        static <T extends BuildParameters, R extends BuildParameters> TaskRunnable.Builder<R> of(BuildParameters.Mapper<R, T> mapper, ComponentTaskBuilder<? super T, ?> tBuilder) {
            return of(mapper, params -> context -> context.execute(tBuilder.build(params)).getResult());
        }

        static <T extends BuildParameters, R extends BuildParameters> TaskRunnable.Builder<R> of(Collection<T> params, TaskRunnable.Builder<? super T> tBuilder) {
            return r -> context -> {
                final ServerMigrationTaskResult.Builder resultBuilder = new ServerMigrationTaskResult.Builder().skipped();
                for (T t : params) {
                    if (tBuilder.build(t).run(context).getStatus() == ServerMigrationTaskResult.Status.SUCCESS) {
                        resultBuilder.success();
                    }
                }
                return resultBuilder.build();
            };
        }

        static <T extends BuildParameters, R extends BuildParameters> TaskRunnable.Builder<R> of(Collection<T> tParams, ComponentTaskBuilder<? super T, ?> tBuilder) {
            return of(tParams, params -> context -> context.execute(tBuilder.build(params)).getResult());
        }
    }
}
