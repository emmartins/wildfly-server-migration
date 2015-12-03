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
package org.wildfly.migration.core;

import org.wildfly.migration.core.logger.ServerMigrationLogger;

import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * @author emmartins
 */
public final class Servers {

    private static final ServiceLoader<ServerProvider> SERVER_PROVIDERS_LOADER = ServiceLoader.load(ServerProvider.class);

    private Servers() {
    }

    public static Server getServer(Path baseDir) throws ServerProviderNotFound {
        ServerMigrationLogger.ROOT_LOGGER.debugf("Retrieving server from base dir %s", baseDir);
        for (ServerProvider serverProvider : SERVER_PROVIDERS_LOADER) {
            try {
                Server server = serverProvider.getServer(baseDir);
                if (server != null) {
                    ServerMigrationLogger.ROOT_LOGGER.debugf("%s recognized as %s base dir. Server product info: %s", baseDir, serverProvider.getName(), server.getProductInfo());
                    return server;
                }
            } catch (Throwable e) {
                ServerMigrationLogger.ROOT_LOGGER.debugf(e, "Failure retrieving server from provider %s", serverProvider.getClass());
            }
        }
        ServerMigrationLogger.ROOT_LOGGER.debugf("%s not recognized as valid server base dir", baseDir);
        throw new ServerProviderNotFound(baseDir.toString());
    }
}
