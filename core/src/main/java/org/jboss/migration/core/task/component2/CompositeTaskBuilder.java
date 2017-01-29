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

package org.jboss.migration.core.task.component2;

import org.jboss.migration.core.task.ServerMigrationTask;

/**
 * @author emmartins
 */
public interface CompositeTaskBuilder<P extends TaskBuilder.Params, T extends CompositeTaskBuilder<P, T>> extends TaskBuilder<P,T> {

    default T run(ServerMigrationTask task) {
        final RunnableFactory<P> runnableFactory = params -> (taskName, context) -> context.execute(task).getResult();
        return run(runnableFactory);
    }

    default T run(TaskBuilder<? super P, ?> builder) {
        final TaskBuilder<? super P, ?> clone = builder.clone();
        final RunnableFactory<P> runnableFactory = params -> (taskName, context) -> context.execute(clone.build(params)).getResult();
        return run(runnableFactory);
    }

    default <Q extends Params> T run(ParamsConverter<P, Q> paramsConverter, TaskBuilder<? super Q, ?> q) {
        final TaskBuilder<? super Q, ?> clone = q.clone();
        final RunnableFactory<P> p = params -> (taskName, context) -> context.execute(clone.build(paramsConverter.apply(params))).getResult();
        return run(p);
    }
}
