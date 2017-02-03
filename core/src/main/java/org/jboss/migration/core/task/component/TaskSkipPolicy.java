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

import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.TaskContext;

/**
 * @author emmartins
 */
public interface TaskSkipPolicy {

    boolean isSkipped(TaskContext context);

    static TaskSkipPolicy skipByTaskEnvironment(String taskEnvironmentPropertyNamePrefix) {
        return context -> new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), taskEnvironmentPropertyNamePrefix).isSkippedByEnvironment();
    }

    static TaskSkipPolicy skipIfDefaultSkipPropertyIsSet(ServerMigrationTaskName taskName) {
        return context -> context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(taskName.getName() + ".skip", Boolean.FALSE);
    }

    static TaskSkipPolicy skipIfAnyPropertyIsSet(String... propertyNames) {
        return context -> {
            for (String propertyName : propertyNames) {
                if (context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(propertyName, Boolean.FALSE)) {
                    return true;
                }
            }
            return false;
        };
    }

    interface Builder<P extends BuildParameters> {

        TaskSkipPolicy build(P buildParameters, ServerMigrationTaskName taskName);

        static <P extends BuildParameters> Builder<P> skipIfDefaultSkipPropertyIsSet() {
            return (buildParameters, taskName) -> TaskSkipPolicy.skipIfDefaultSkipPropertyIsSet(taskName);
        }
        static <P extends BuildParameters> Builder<P> skipByTaskEnvironment(String taskEnvironmentPropertyNamePrefix) {
            return (buildParameters, taskName) -> TaskSkipPolicy.skipByTaskEnvironment(taskEnvironmentPropertyNamePrefix);
        }
        static <P extends BuildParameters> Builder<P> skipIfAnyPropertyIsSet(String... propertyNames) {
            return (buildParameters, taskName) -> TaskSkipPolicy.skipIfAnyPropertyIsSet(propertyNames);
        }
    }

    interface Builders {
        static <P extends BuildParameters> Builder<P> skipIfDefaultSkipPropertyIsSet() {
            return (buildParameters, taskName) -> TaskSkipPolicy.skipIfDefaultSkipPropertyIsSet(taskName);
        }
        static <P extends BuildParameters> Builder<P> skipByTaskEnvironment(String taskEnvironmentPropertyNamePrefix) {
            return (buildParameters, taskName) -> TaskSkipPolicy.skipByTaskEnvironment(taskEnvironmentPropertyNamePrefix);
        }
        static <P extends BuildParameters> Builder<P> skipIfAnyPropertyIsSet(String... propertyNames) {
            return (buildParameters, taskName) -> TaskSkipPolicy.skipIfAnyPropertyIsSet(propertyNames);
        }
    }
}
