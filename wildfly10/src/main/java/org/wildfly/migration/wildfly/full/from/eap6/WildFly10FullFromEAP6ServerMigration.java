/*
 * Copyright 2015 Red Hat, Inc.
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
package org.wildfly.migration.wildfly.full.from.eap6;

import org.wildfly.migration.core.Server;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.wildfly.WildFly10Server;
import org.wildfly.migration.wildfly.from.eap6.WildFly10FromEAP6ServerMigration;
import org.wildfly.migration.wildfly.from.eap6.domain.WildFly10FromEAP6DomainMigration;
import org.wildfly.migration.wildfly.full.WildFly10FullServerMigration;
import org.wildfly.migration.wildfly.full.from.eap6.standalone.WildFly10FullFromEAP6StandaloneMigration;

import java.io.IOException;

/**
 * @author emmartins
 */
public class WildFly10FullFromEAP6ServerMigration extends WildFly10FromEAP6ServerMigration implements WildFly10FullServerMigration {

    public WildFly10FullFromEAP6ServerMigration() {
        super(new WildFly10FullFromEAP6StandaloneMigration(), new WildFly10FromEAP6DomainMigration());
    }

    @Override
    public void run(Server source, WildFly10Server target, ServerMigrationContext context) throws IOException {
        context.getConsoleWrapper().printf("Server migration starting...%n");
        super.run(source, target, context);
        context.getConsoleWrapper().printf("Server migration done.%n");
    }
}
