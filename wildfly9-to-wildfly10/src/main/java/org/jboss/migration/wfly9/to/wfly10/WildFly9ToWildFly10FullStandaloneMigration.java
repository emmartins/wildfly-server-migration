/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.wfly9.to.wfly10;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServerMigration;
import org.jboss.migration.wfly10.standalone.config.WildFly10StandaloneConfigFilesMigration;
import org.jboss.migration.wfly9.WildFly9Server;

import java.util.ArrayList;
import java.util.List;

/**
 * Migration of a standalone server, from WildFly 9 to WildFly 10.
 * @author emmartins
 */
public class WildFly9ToWildFly10FullStandaloneMigration extends WildFly10StandaloneServerMigration<WildFly9Server> {

    private final WildFly10StandaloneConfigFilesMigration<WildFly9Server> configFilesMigration;

    public WildFly9ToWildFly10FullStandaloneMigration(WildFly10StandaloneConfigFilesMigration<WildFly9Server> configFilesMigration) {
        this.configFilesMigration = configFilesMigration;
    }

    @Override
    protected List<ServerMigrationTask> getSubtasks(WildFly9Server source, WildFly10Server target, ServerMigrationTaskContext context) {
        List<ServerMigrationTask> subtasks = new ArrayList<>();
        subtasks.add(configFilesMigration.getServerMigrationTask(source.getStandaloneConfigs(), target));
        return subtasks;
    }
}