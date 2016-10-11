/*
 * Copyright 2016 Red Hat, Inc.
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

package org.jboss.migration.wfly10.config.task;

import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTasks;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.wfly10.config.management.ResourcesManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class ResourcesMigration<S, T extends ResourcesManagement> {

    protected final List<SubtaskFactory<S, T>> subtaskFactories;
    protected final ServerMigrationTaskName taskName;
    protected final String skipTaskPropertyName;
    protected final EventListener eventListener;

    protected ResourcesMigration(Builder builder) {
        this.subtaskFactories = Collections.unmodifiableList(builder.subtaskFactories);
        this.taskName = builder.taskName;
        this.skipTaskPropertyName = builder.skipTaskPropertyName;
        this.eventListener = builder.eventListener;
    }

    public ServerMigrationTask getTask(S source, T resourceManagement) throws Exception {
        final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(taskName)
                .succeedOnlyIfHasSuccessfulSubtasks();
        if (eventListener != null) {
            taskBuilder.eventListener(eventListener);
        }
        final ServerMigrationTasks subtasks = taskBuilder.getSubtasks();
        for (SubtaskFactory subtaskFactory : subtaskFactories) {
            subtaskFactory.addSubtasks(source, resourceManagement, subtasks);
        }
        ServerMigrationTask task = taskBuilder.build();
        if (skipTaskPropertyName != null) {
            task = new SkippableByEnvServerMigrationTask(task, skipTaskPropertyName);
        }
        return task;
    }

    public interface SubtaskFactory<S, T extends ResourcesManagement> {
        void addSubtasks(S source, T resourceManagement, ServerMigrationTasks subtasks) throws Exception;
    }

    public static class Builder<B extends Builder, S, T extends ResourcesManagement> {

        private final List<SubtaskFactory<S, T>> subtaskFactories = new ArrayList<>();
        private final ServerMigrationTaskName taskName;
        private String skipTaskPropertyName;
        private EventListener eventListener;

        public Builder(ServerMigrationTaskName taskName) {
            this.taskName = taskName;
        }

        public Builder(String resourcesName) {
            this(new ServerMigrationTaskName.Builder(resourcesName).build());
            skipTaskPropertyName(resourcesName+".skip");
        }

        public B eventListener(EventListener eventListener) {
            this.eventListener = eventListener;
            return (B) this;
        }

        public B skipTaskPropertyName(String skipTaskPropertyName) {
            this.skipTaskPropertyName = skipTaskPropertyName;
            return (B) this;
        }

        public B addSubtaskFactory(final SubtaskFactory<S, T> subtaskFactory) {
            subtaskFactories.add(subtaskFactory);
            return (B) this;
        }

        public ResourcesMigration<S, T> build() {
            return new ResourcesMigration(this);
        }
    }

    public interface EventListener extends ParentServerMigrationTask.EventListener {
    }
}
