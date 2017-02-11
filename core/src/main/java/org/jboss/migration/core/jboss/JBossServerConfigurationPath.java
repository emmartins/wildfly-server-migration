/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.migration.core.jboss;

import org.jboss.migration.core.ServerPath;

import java.nio.file.Path;

/**
 * @author emmartins
 */
public class JBossServerConfigurationPath<S extends JBossServer<S>> extends ServerPath<S> implements AbsolutePathResolver {

    private final Path configurationDir;
    private final Path dataDir;
    private final Path contentDir;

    protected JBossServerConfigurationPath(Path path, S server, Path configurationDir, Path dataDir, Path contentDir) {
        super(path, server);
        this.configurationDir = configurationDir;
        this.dataDir = dataDir;
        this.contentDir = contentDir;
    }

    public Path getConfigurationDir() {
        return configurationDir;
    }

    public Path getDataDir() {
        return dataDir;
    }

    public Path getContentDir() {
        return contentDir;
    }

    @Override
    public Path resolveNamedPath(String path) {
        return getServer().resolveNamedPath(path);
    }
}
