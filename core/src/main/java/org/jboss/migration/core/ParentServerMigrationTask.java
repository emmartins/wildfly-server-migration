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

package org.jboss.migration.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A {@link ServerMigrationTask} which simply execute its subtasks.
 * @author emmartins
 */
public class ParentServerMigrationTask implements ServerMigrationTask {

    private final ServerMigrationTaskName name;
    private final EventListener eventListener;
    private final List<ServerMigrationTask> subtasks;
    private final boolean succeedOnlyIfHasSuccessfulSubtasks;

    public ParentServerMigrationTask(Builder builder) {
        this.name = builder.name;
        this.eventListener = builder.eventListener;
        this.subtasks = builder.subtasks != null ? Collections.unmodifiableList(builder.subtasks) : Collections.<ServerMigrationTask>emptyList();
        this.succeedOnlyIfHasSuccessfulSubtasks = builder.succeedOnlyIfHasSuccessfulSubtasks;
    }

    @Override
    public ServerMigrationTaskName getName() {
        return name;
    }

    @Override
    public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
        if (eventListener != null) {
            eventListener.started(context);
        }
        try {
            for (ServerMigrationTask subtask : subtasks) {
                context.execute(subtask);
            }
        } finally {
            if (eventListener != null) {
                eventListener.done(context);
            }
        }
        return (!succeedOnlyIfHasSuccessfulSubtasks || context.hasSucessfulSubtasks()) ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
    }

    public interface EventListener {
        void started(ServerMigrationTaskContext context);
        void done(ServerMigrationTaskContext context);
    }

    /**
     * The parent task builder.
     */
    public static class Builder {

        private final ServerMigrationTaskName name;
        private EventListener eventListener;
        private final List<ServerMigrationTask> subtasks;
        private boolean succeedOnlyIfHasSuccessfulSubtasks = true;

        public Builder(ServerMigrationTaskName name) {
            this.name = name;
            this.subtasks = new ArrayList<>();
        }

        public Builder addSubtask(ServerMigrationTask subtask) {
            subtasks.add(subtask);
            return this;
        }

        public Builder addSubtasks(Collection<ServerMigrationTask> subtasks) {
            subtasks.addAll(subtasks);
            return this;
        }

        public ServerMigrationTasks getSubtasks() {
            return new ServerMigrationTasks() {
                @Override
                public void add(ServerMigrationTask task) {
                    addSubtask(task);
                }
                @Override
                public void addAll(Collection<ServerMigrationTask> tasks) {
                    addSubtasks(tasks);
                }
            };
        }

        public Builder succeedOnlyIfHasSuccessfulSubtasks() {
            succeedOnlyIfHasSuccessfulSubtasks = true;
            return this;
        }

        public Builder succeedAlways() {
            succeedOnlyIfHasSuccessfulSubtasks = false;
            return this;
        }

        public Builder eventListener(EventListener eventListener) {
            this.eventListener = eventListener;
            return this;
        }

        public ParentServerMigrationTask build() {
            return new ParentServerMigrationTask(this);
        }
    }

}
