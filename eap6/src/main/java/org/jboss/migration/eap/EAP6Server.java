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
package org.jboss.migration.eap;

import org.jboss.migration.core.AbstractServer;
import org.jboss.migration.core.ProductInfo;
import org.jboss.migration.core.ServerMigrationFailedException;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.util.xml.SimpleXMLFileMatcher;
import org.jboss.migration.core.util.xml.XMLFileMatcher;
import org.jboss.migration.core.util.xml.XMLFiles;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The EAP 6 {@link org.jboss.migration.core.Server}
 * @author emmartins
 */
public class EAP6Server extends AbstractServer {

    public EAP6Server(ProductInfo productInfo, Path baseDir) {
        super(productInfo, baseDir);
    }

    public Collection<ServerPath<EAP6Server>> getStandaloneConfigs() {
        try {
            final List<ServerPath<EAP6Server>> standaloneConfigs = new ArrayList<>();
            final XMLFileMatcher scanMatcher = new SimpleXMLFileMatcher() {
                @Override
                protected boolean documentElementLocalNameMatches(String localName) {
                    return "server".equals(localName);
                }
                @Override
                protected boolean documentNamespaceURIMatches(String namespaceURI) {
                    return namespaceURI.startsWith("urn:jboss:domain:");
                }
            };
            for (Path path : XMLFiles.scan(getStandaloneConfigurationDir(), false, scanMatcher)) {
                standaloneConfigs.add(new ServerPath<>(path, this));
            }
            return Collections.unmodifiableList(standaloneConfigs);
        } catch (IOException e) {
            throw new ServerMigrationFailedException(e);
        }
    }

    public Path getStandaloneDir() {
        return getBaseDir().resolve("standalone");
    }

    public Path getStandaloneConfigurationDir() {
        return getStandaloneDir().resolve("configuration");
    }

    public Path getModulesDir() {
        return getModulesDir(getBaseDir());
    }

    public Path getModulesSystemLayersBaseDir() {
        return getModulesSystemLayersBaseDir(getBaseDir());
    }

    public static Path getModulesDir(Path baseDir) {
        return baseDir.resolve("modules");
    }

    public static Path getModulesSystemLayersBaseDir(Path baseDir) {
        return getModulesDir(baseDir).resolve("system").resolve("layers").resolve("base");
    }
}
