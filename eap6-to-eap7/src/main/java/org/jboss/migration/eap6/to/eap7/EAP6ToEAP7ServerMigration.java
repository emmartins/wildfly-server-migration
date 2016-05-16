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
package org.jboss.migration.eap6.to.eap7;

import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.eap.EAP6Server;
import org.jboss.migration.eap.EAP7ServerMigration;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.standalone.config.WildFly10StandaloneConfigFilesMigration;

/**
 * Server migration, from EAP 6 to EAP 7.
 * @author emmartins
 */
public class EAP6ToEAP7ServerMigration implements EAP7ServerMigration<EAP6Server> {

    private final EAP6ToEAP7StandaloneMigration standaloneMigration;

    public EAP6ToEAP7ServerMigration() {
        standaloneMigration = new EAP6ToEAP7StandaloneMigration(new WildFly10StandaloneConfigFilesMigration<EAP6Server>(new EAP6ToEAP7StandaloneConfigFileMigration()));
    }

    @Override
    public ServerMigrationTaskResult run(final EAP6Server source, final WildFly10Server target, ServerMigrationTaskContext context) {
        context.execute(standaloneMigration.getServerMigrationTask(source, target));
        return ServerMigrationTaskResult.SUCCESS;
    }

    @Override
    public Class<EAP6Server> getSourceType() {
        return EAP6Server.class;
    }
}
