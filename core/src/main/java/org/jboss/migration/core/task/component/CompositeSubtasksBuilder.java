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

import org.jboss.migration.core.task.ServerMigrationTask;

/**
 * @author emmartins
 */
public interface CompositeSubtasksBuilder<P extends BuildParameters, T extends CompositeSubtasksBuilder<P, T>> extends TaskRunnable.Builder<P> {

    T subtask(TaskRunnable.Builder<? super P> subtasksBuilder);

    default T subtask(ServerMigrationTask task) {
        return subtask(params -> context -> context.execute(task).getResult());
    }

    default T subtask(ComponentTaskBuilder<? super P, ?> builder) {
        return subtask(params -> context -> context.execute(builder.build(params)).getResult());
    }

    default <Q extends BuildParameters> T subtask(BuildParameters.Mapper<P, Q> pqMapper, TaskRunnable.Builder<? super Q> qBuilder) {
        return subtask(TaskRunnable.Builder.of(pqMapper, qBuilder));
    }

    default <Q extends BuildParameters> T subtask(BuildParameters.Mapper<P, Q> pqMapper, ComponentTaskBuilder<? super Q, ?> qBuilder) {
        return subtask(TaskRunnable.Builder.of(pqMapper, qBuilder));
    }
}
