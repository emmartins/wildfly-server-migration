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

import org.jboss.migration.core.console.BasicResultHandlers;
import org.jboss.migration.core.console.UserConfirmation;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.core.task.TaskContext;

/**
 * @author emmartins
 */
public interface TaskSkipPolicy {

    boolean isSkipped(TaskContext context);

    static TaskSkipPolicy skipByTaskEnvironment(String propertyNamesBase) {
        return context -> new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), propertyNamesBase).isSkippedByEnvironment();
    }

    static TaskSkipPolicy skipIfDefaultTaskSkipPropertyIsSet() {
        return context -> skipIfAnyPropertyIsSet(context.getTaskName().getName() + ".skip").isSkipped(context);
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

    static TaskSkipPolicy skipIfAnySkips(TaskSkipPolicy... policies) {
        return context -> {
            for (TaskSkipPolicy policy : policies) {
                if (policy.isSkipped(context)) {
                    return true;
                }
            }
            return false;
        };
    }

    static TaskSkipPolicy skipIfAllSkips(TaskSkipPolicy... policies) {
        return context -> {
            for (TaskSkipPolicy policy : policies) {
                if (!policy.isSkipped(context)) {
                    return false;
                }
            }
            return true;
        };
    }

    static TaskSkipPolicy skipIfNoUserConfirmation(String message) {
        return new TaskSkipPolicy() {
            @Override
            public boolean isSkipped(TaskContext context) {
                return !confirmTaskRun(context);
            }
            private boolean confirmTaskRun(final TaskContext context) {
                final BasicResultHandlers.UserConfirmation resultHandler = new BasicResultHandlers.UserConfirmation();
                new UserConfirmation(context.getServerMigrationContext().getConsoleWrapper(), message, ServerMigrationLogger.ROOT_LOGGER.yesNo(), resultHandler).execute();
                switch (resultHandler.getResult()) {
                    case NO:
                        return false;
                    case YES:
                        return true;
                    case ERROR:
                    default:
                        return confirmTaskRun(context);
                }
            }
        };
    }

    interface Builder<P extends BuildParameters> {
        TaskSkipPolicy build(P buildParameters);
    }

    interface Builders {
        static <P extends BuildParameters> Builder<P> skipIfDefaultTaskSkipPropertyIsSet() {
            return (buildParameters) -> TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet();
        }
        static <P extends BuildParameters> Builder<P> skipByTaskEnvironment(String taskEnvironmentPropertyNamePrefix) {
            return (buildParameters) -> TaskSkipPolicy.skipByTaskEnvironment(taskEnvironmentPropertyNamePrefix);
        }
        static <P extends BuildParameters> Builder<P> skipIfAnyPropertyIsSet(String... propertyNames) {
            return (buildParameters) -> TaskSkipPolicy.skipIfAnyPropertyIsSet(propertyNames);
        }
        static <P extends BuildParameters> Builder<P> skipIfNoUserConfirmation(String message) {
            return (buildParameters) -> TaskSkipPolicy.skipIfNoUserConfirmation(message);
        }

        static <P extends BuildParameters> Builder<P> skipIfAnySkips(Builder<P>... builders) {
            return (buildParameters) -> context -> {
                for (Builder<P> builder : builders) {
                    if (builder.build(buildParameters).isSkipped(context)) {
                        return true;
                    }
                }
                return false;
            };
        }
        static <P extends BuildParameters> Builder<P> skipIfAllSkips(Builder<P>... builders) {
            return (buildParameters) -> context -> {
                for (Builder<P> builder : builders) {
                    if (!builder.build(buildParameters).isSkipped(context)) {
                        return false;
                    }
                }
                return true;
            };
        }
    }
}
