/*
 * Copyright 2016 Red Hat, Inc.
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

package org.jboss.migration.core.util.xml;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A matcher for XML files.
 * @author emmartins
 */
public interface XMLFileMatcher {
    /**
     * Indicates if the specified path matches a XML file.
     * @param path the XML file absolute path.
     * @return true if the path matches a XML file, false otherwise.
     * @throws IOException if the matching failed due to an error.
     */
    boolean matches(Path path) throws IOException;
}
