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
package org.jboss.migration.wfly10.subsystem;

import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;

/**
 * @author emmartins
 */
public class LegacyWildFly10Extension extends WildFly10Extension {

    public LegacyWildFly10Extension(String name) {
        super(name);
    }

    @Override
    public void migrate(WildFly10StandaloneServer server, ServerMigrationTaskContext context) throws IOException {
        super.migrate(server, context);
        // FIXME tmp workaround for legacy extensions which do not remove itself
        if (server.getExtensions().contains(getName())) {
            // remove itself after migration
            server.removeExtension(getName());
            ServerMigrationLogger.ROOT_LOGGER.debugf("Extension %s removed after migration.", getName());
        }
    }
}
