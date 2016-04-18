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
package org.jboss.migration.core;

import org.jboss.migration.core.logger.ServerMigrationLogger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The {@link Server}s for migration.
 *
 * Each supported {@link Server} is provided by a {@link ServerProvider}, which is loaded through the {@link ServiceLoader} framework.
 * @author emmartins
 */
public final class Servers {

    private static final ServiceLoader<ServerProvider> SERVER_PROVIDERS_LOADER = ServiceLoader.load(ServerProvider.class);

    private Servers() {
    }

    /**
     * Retrieves a {@link Server} from its base directory {@link Path}
     * @param baseDir the {@link Server}'s base directory {@link Path}
     * @return the {@link Server} retrieved from its base directory {@link Path}; null if no {@link ServerProvider} was able to retrieve a {@link Server} from the specified base directory {@link Path}
     */
    public static Server getServer(Path baseDir) {
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
        return null;
    }

    /**
     * Retrieves the supported {@link Server} names.
     * @return a list containing the supported {@link Server} names.
     */
    public static List<String> getServerNames() {
        final List<String> serverNames = new ArrayList<>();
        for (ServerProvider serverProvider : SERVER_PROVIDERS_LOADER) {
            serverNames.add(serverProvider.getName());
        }
        return serverNames;
    }
}
