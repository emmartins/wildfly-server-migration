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
package org.wildfly.migration.wildfly.standalone;

import org.jboss.dmr.ModelNode;
import org.wildfly.migration.wildfly.WildFly10Server;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public interface WildFly10StandaloneServer {
    void start();
    void stop();
    boolean isStarted();
    WildFly10Server getServer();
    Set<String> getExtensions() throws IOException;
    List<ModelNode> getSecurityRealms() throws IOException;
    Set<String> getSubsystems() throws IOException;
    void removeSubsystem(String subsystem) throws IOException;
    void removeExtension(String extension) throws IOException;
    Path resolvePath(String path)  throws IOException;
    void migrateSubsystem(String subsystem) throws IOException;
}