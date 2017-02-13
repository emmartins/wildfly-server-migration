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
package org.jboss.migration.core.task;

import java.io.Serializable;

/**
 * A path which defines the place of a server migration task in the migration tasks tree.
 * @author emmartins
 */
public class ServerMigrationTaskPath implements Serializable {

    private final ServerMigrationTaskName taskName;
    private final ServerMigrationTaskPath parent;

    public ServerMigrationTaskPath(ServerMigrationTaskName taskName) {
        this(taskName, null);
    }

    public ServerMigrationTaskPath(ServerMigrationTaskName taskName, ServerMigrationTaskPath parent) {
        this.taskName = taskName;
        this.parent = parent;
    }

    public ServerMigrationTaskPath getParent() {
        return parent;
    }

    public ServerMigrationTaskName getTaskName() {
        return taskName;
    }

    public int size() {
        int size = 1;
        if (parent != null) {
            size += parent.size();
        }
        return size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (parent != null) {
            sb.append(parent).append(" > ");
        }
        sb.append(taskName);
        return sb.toString();
    }
}
