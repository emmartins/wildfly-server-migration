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
package org.wildfly.migration.wildfly.server.embedded;

import org.wildfly.core.embedded.EmbeddedServerFactory;
import org.wildfly.core.embedded.ServerStartException;
import org.wildfly.core.embedded.StandaloneServer;
import org.wildfly.migration.wildfly.server.TargetServerManagement;
import org.wildfly.migration.wildfly.server.TargetServer;

import java.nio.file.Path;

/**
 * @author emmartins
 */
public class EmbeddedStandaloneTargetServer implements TargetServer {

    private final String config;
    private final Path baseDir;
    private StandaloneServer standaloneServer;

    public EmbeddedStandaloneTargetServer(String config, Path baseDir) {
        this.config = config;
        this.baseDir = baseDir;
    }

    @Override
    public synchronized void start() {
        if (standaloneServer != null) {
            throw new IllegalStateException("server started");
        }
        final String[] cmds = {"--server-config="+config,"--admin-only"};
        standaloneServer = EmbeddedServerFactory.create(baseDir.toString(), null, null, cmds);
        try {
            standaloneServer.start();
        } catch (ServerStartException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        if (standaloneServer == null) {
            throw new IllegalStateException("server not started");
        }
        standaloneServer.stop();
        standaloneServer = null;
    }

    @Override
    public synchronized TargetServerManagement getManagementClient() {
        return new EmbeddedTargetServerManagement(standaloneServer);
    }
}
