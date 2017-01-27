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

import org.jboss.migration.core.Server;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.WildFlyServer10;
import org.jboss.migration.wfly10.WildFlyServerMigration10;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class ServerMigration<S extends Server> implements WildFlyServerMigration10<S> {

    protected final List<SubtaskFactory<S>> subtaskFactories;

    public ServerMigration(Builder<S> builder) {
        subtaskFactories = Collections.unmodifiableList(builder.subtaskFactories);
    }

    public ServerMigrationTaskResult run(S source, WildFlyServer10 target, TaskContext context) {
        for (SubtaskFactory subtaskFactory : subtaskFactories) {
            ServerMigrationTask subtask = subtaskFactory.getTask(source, target);
            if (subtask != null) {
                context.execute(subtask);
            }
        }
        return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
    }

    public interface SubtaskFactory<S extends Server> {
        ServerMigrationTask getTask(S source, WildFlyServer10 target);
    }

    public static class Builder<S extends Server> {

        private final List<SubtaskFactory<S>> subtaskFactories = new ArrayList<>();

        public Builder subtask(SubtaskFactory<S> subtaskFactory) {
            subtaskFactories.add(subtaskFactory);
            return this;
        }

        public ServerMigration<S> build() {
            return new ServerMigration(this);
        }
    }
}
