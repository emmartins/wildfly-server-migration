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

import java.nio.file.Path;

/**
 * @author emmartins
 */
public interface AbsolutePathResolver {

    Path resolveNamedPath(String string);

    Path resolvePath(String path, String relativeTo);

    default Path resolvePath(String path) {
        return resolvePath(path, null);
    }

    default Path resolvePath(ResolvablePath resolvablePath) {
        return resolvePath(resolvablePath.getPath(), resolvablePath.getRelativeTo());
    }
}
