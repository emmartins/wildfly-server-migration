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

import org.jboss.dmr.ModelNode;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.jboss.as.domain.management.ModelDescriptionConstants.PATH;
import static org.jboss.as.domain.management.ModelDescriptionConstants.RELATIVE_TO;

/**
 * @author emmartins
 */
public interface AbsolutePathResolver {

    Path resolveNamedPath(String path);

    default Path resolvePath(ResolvablePath resolvablePath) {
        return resolvePath(resolvablePath.getPath(), resolvablePath.getRelativeTo());
    }

    default Path resolvePath(ModelNode modelNode) {
        if (!modelNode.hasDefined(PATH)) {
            return null;
        } else {
            final Path path = Paths.get(modelNode.get(PATH).asString());
            if (!modelNode.hasDefined(RELATIVE_TO)) {
                return path;
            } else {
                return resolvePath(path, modelNode.get(RELATIVE_TO).asString());
            }
        }
    }

    default Path resolvePath(String string) {
        final Path namedPath = resolveNamedPath(string);
        if (namedPath != null && namedPath.isAbsolute()) {
            return namedPath;
        } else {
            final Path path = Paths.get(string);
            if (path.isAbsolute()) {
                return path;
            } else {
                return null;
            }
        }
    }

    default Path resolvePath(Path path, String relativeTo) {
        if (path.isAbsolute()) {
            return path;
        } else {
            final Path relativeToPath = resolvePath(relativeTo);
            return relativeToPath != null ? relativeToPath.resolve(path) : null;
        }
    }

    default Path resolvePath(String path, String relativeTo) {
        return resolvePath(Paths.get(path), relativeTo);

    }
}
