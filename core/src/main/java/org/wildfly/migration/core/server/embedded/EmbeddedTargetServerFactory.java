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
package org.wildfly.migration.core.server.embedded;

import org.wildfly.migration.core.ServerPaths;
import org.wildfly.migration.core.ServerPathsImpl;
import org.wildfly.migration.core.server.TargetServer;
import org.wildfly.migration.core.server.TargetServerFactory;

import java.nio.file.Path;

/**
 * @author emmartins
 */
public class EmbeddedTargetServerFactory implements TargetServerFactory {

    private final ServerPaths serverPaths;

    public EmbeddedTargetServerFactory(Path baseDir) {
        this.serverPaths = new ServerPathsImpl(baseDir);
    }

    @Override
    public TargetServer newStandaloneTargetServer(String config) {
        return new EmbeddedStandaloneTargetServer(config, serverPaths.getBaseDir());
    }

    @Override
    public ServerPaths getServerPaths() {
        return serverPaths;
    }
}
