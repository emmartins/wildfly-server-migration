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

import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.task.TaskContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PATH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RELATIVE_TO;

/**
 * @author emmartins
 */
public interface PathMigrationUtils {
    static boolean migrate(String path, String relativeTo, JBossServer source, JBossServer target, TaskContext taskContext) {
        //final Path path = Paths.get(content.get(PATH).asString());
        //final String relativeTo = content.hasDefined(RELATIVE_TO) ? content.get(RELATIVE_TO).asString() : null;
        if (relativeTo != null) {
            Path sourceRelativeTo = sourceServer.resolvePath(relativeTo);
            if (sourceRelativeTo == null) {
                // give a try to the running server config, may have the path configured there
                final Path targetRelativeTo = params.getServerConfiguration().resolvePath(relativeTo);
                if (targetRelativeTo != null) {
                    if (targetRelativeTo.startsWith(targetServer.getBaseDir())) {
                        sourceRelativeTo = sourceServer.getBaseDir().resolve(targetServer.getBaseDir().relativize(targetRelativeTo));
                    } else {
                        sourceRelativeTo = targetRelativeTo;
                    }
                }
                if (sourceRelativeTo == null || !Files.isDirectory(sourceRelativeTo)) {
                    throw new ServerMigrationFailureException("Source server failed to resolve 'relative to' path "+relativeTo);
                }
            }
            final Path contentSource = sourceRelativeTo.resolve(path);
            context.getLogger().infof("Source deployment content's path: %s", contentSource);
            Path targetRelativeTo = targetServer.resolvePath(relativeTo);
            if (targetRelativeTo == null) {
                targetRelativeTo = params.getServerConfiguration().resolvePath(relativeTo);
                if (targetRelativeTo == null) {
                    throw new ServerMigrationFailureException("Target server failed to resolve 'relative to' path "+relativeTo);
                }
            }
            final Path contentTarget = targetRelativeTo.resolve(path);
            context.getLogger().infof("Target deployment content's path: %s", contentTarget);
            if (!contentSource.equals(contentTarget)) {
                context.getMigrationFiles().copy(contentSource, contentTarget);
            }
    }
}
