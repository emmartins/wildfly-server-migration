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
package org.wildfly.migration.wfly9.to.wfly10;

import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.wfly10.WildFly10Server;
import org.wildfly.migration.wfly10.standalone.config.WildFly10StandaloneConfigFilesMigration;
import org.wildfly.migration.wfly9.WildFly9Server;

import java.io.IOException;

/**
 * Migration of a standalone server, from WildFly 9 to WildFly 10.
 * @author emmartins
 */
public class WildFly9ToWildFly10FullStandaloneMigration {

    private final WildFly10StandaloneConfigFilesMigration<WildFly9Server> configFilesMigration;

    public WildFly9ToWildFly10FullStandaloneMigration(WildFly10StandaloneConfigFilesMigration<WildFly9Server> configFilesMigration) {
        this.configFilesMigration = configFilesMigration;
    }

    public void run(WildFly9Server source, WildFly10Server target, ServerMigrationContext context) throws IOException {
        configFilesMigration.run(source.getStandaloneConfigs(), target, context);
    }
}