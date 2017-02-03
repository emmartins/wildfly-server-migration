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

/**
 * @author emmartins
 */
public interface ComponentTaskBuilder<P extends BuildParameters, T extends ComponentTaskBuilder<P, T>> {

    default T name(String name) {
        return name(new ServerMigrationTaskName.Builder(name).build());
    }

    default T name(ServerMigrationTaskName name) {
        return name(parameters -> name);
    }

    T name(TaskNameBuilder<? super P> builder);

    default T skipPolicy(TaskSkipPolicy skipPolicy) {
        return skipPolicy((parameters, name) -> skipPolicy);
    }

    T skipPolicy(TaskSkipPolicy.Builder<? super P> builder);

    default T beforeRun(BeforeTaskRun beforeRun) {
        return beforeRun((parameters, name) -> beforeRun);
    }

    T beforeRun(BeforeTaskRun.Builder<? super P> builder);

    default T afterRun(AfterTaskRun afterRun) {
        return afterRun((parameters, name) -> afterRun);
    }

    T afterRun(AfterTaskRun.Builder<? super P> builder);

    ServerMigrationTask build(P params);
}