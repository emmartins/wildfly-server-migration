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
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.TaskContextDelegate;
import org.jboss.migration.wfly10.config.management.ResourceManagement;

/**
 * @author emmartins
 */
public class ResourceManagementTask2<S, R extends ResourceManagement> extends ParentTask<ResourceManagementTask2.SubtasksContext<R>> {

    public ResourceManagementTask2(BaseBuilder<R, ?> builder, SubtaskExecutorContextFactory<SubtasksContext<S, R>> subtaskExecutorContextFactory) {
        super(builder, subtaskExecutorContextFactory);
    }

    public static class BaseBuilder<R extends ResourceManagement, B extends BaseBuilder<R,B>> extends ParentTask.BaseBuilder<SubtasksContext<R>, B> {
        public BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName);
        }


    }

    public static class SubtasksContext<R extends ResourceManagement> extends TaskContextDelegate {
        private final R resourceManagement;

        protected SubtasksContext(TaskContext taskContext,R resourceManagement) {
            super(taskContext);
            this.resourceManagement = resourceManagement;
        }

        public R getResourceManagement() {
            return resourceManagement;
        }
    }
}
