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

import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.wfly10.config.management.ServerGroupsManagement;

/**
 * Migration of domain config's server groups.
 *  @author emmartins
 */
public class ServerGroupsMigration<S> extends ResourcesMigration<S, ServerGroupsManagement> {

    public static final String SERVER_GROUPS = "server-groups";

    protected ServerGroupsMigration(Builder<S> builder) {
        super(builder);
    }

    public interface SubtaskFactory<S> extends ResourcesMigration.SubtaskFactory<S, ServerGroupsManagement> {
    }

    public static class Builder<S> extends ResourcesMigration.Builder<Builder<S>, S, ServerGroupsManagement> {
        public Builder() {
            super(SERVER_GROUPS);
            eventListener(new ResourcesMigration.EventListener() {
                @Override
                public void started(ServerMigrationTaskContext context) {
                    context.getLogger().infof("Server groups migration starting...");
                }
                @Override
                public void done(ServerMigrationTaskContext context) {
                    context.getLogger().infof("Server groups migration done.");
                }
            });
        }
        public Builder serverGroupMigration(ServerGroupMigration serverGroupMigration) {
            return subtask(serverGroupMigration);
        }
        @Override
        public ServerGroupsMigration<S> build() {
            return new ServerGroupsMigration(this);
        }
    }

    public static <S> ServerGroupsMigration<S> from(ServerGroupMigration<S> serverGroupMigration) {
        return new Builder<S>().serverGroupMigration(serverGroupMigration).build();
    }
}