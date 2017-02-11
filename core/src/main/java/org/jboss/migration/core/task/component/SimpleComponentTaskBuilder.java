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
import org.jboss.migration.core.task.ServerMigrationTaskName;

import java.util.stream.Stream;

/**
 * @author emmartins
 */
public interface SimpleComponentTaskBuilder<T extends SimpleComponentTaskBuilder<T>> {

    default T name(String name) {
        return name(new ServerMigrationTaskName.Builder(name).build());
    }

    T name(ServerMigrationTaskName name);

    T skipPolicy(TaskSkipPolicy skipPolicy);

    default T skipPolicies(TaskSkipPolicy... skipPolicies) {
        return skipPolicy(context -> {
            for (TaskSkipPolicy skipPolicy : skipPolicies) {
                if (skipPolicy.isSkipped(context)) {
                    return true;
                }
            }
            return false;
        });
    }

    T runnable(TaskRunnable runnable);

    default T runnables(TaskRunnable... runnables) {
        return runnable(new CompositeTaskRunnable.Builder().runnables(runnables).build());
    }

    default T subtask(SimpleComponentTaskBuilder<?> taskBuilder) {
        return runnable(context -> context.execute(taskBuilder.build()).getResult());
    }

    default T subtasks(SimpleComponentTaskBuilder<?>... taskBuilders) {
        return runnables(Stream.of(taskBuilders).map(taskBuilder -> (TaskRunnable) context -> context.execute(taskBuilder.build()).getResult()).toArray(TaskRunnable[]::new));
    }

    T beforeRun(BeforeTaskRun beforeRun);

    T afterRun(AfterTaskRun afterRun);

    ServerMigrationTask build();
}
