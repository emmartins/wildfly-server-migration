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
public class ResolvablePath {

    final Path path;
    final String relativeTo;

    public ResolvablePath(Path path, String relativeTo) {
        this.path = path;
        this.relativeTo = relativeTo;
    }

    public ResolvablePath(ModelNode modelNode) {
        this((modelNode.hasDefined(PATH) ? Paths.get(modelNode.get(PATH).asString()) : null), (modelNode.hasDefined(RELATIVE_TO) ? modelNode.get(RELATIVE_TO).asString() : null));
    }

    public Path getPath() {
        return path;
    }

    public String getRelativeTo() {
        return relativeTo;
    }

    @Override
    public String toString() {
        return "JBossServerPath(path='"+path+"', relativeTo='"+String.valueOf(relativeTo)+"')";
    }
}
