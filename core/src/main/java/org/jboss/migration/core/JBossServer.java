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
package org.jboss.migration.core;

import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.core.util.xml.SimpleXMLFileMatcher;
import org.jboss.migration.core.util.xml.XMLFileMatcher;
import org.jboss.migration.core.util.xml.XMLFiles;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An abstract JBoss {@link Server} impl, which is usable only as migration source.
 * @author emmartins
 */
public abstract class JBossServer<S extends JBossServer> extends AbstractServer {

    public interface EnvironmentProperties {
        String PROPERTIES_PREFIX = "server.";
        String PROPERTIES_STANDALONE_PREFIX = PROPERTIES_PREFIX + "standalone.";
        String PROPERTY_STANDALONE_SERVER_DIR = PROPERTIES_STANDALONE_PREFIX + "serverDir";
        String PROPERTY_STANDALONE_CONFIG_DIR = PROPERTIES_STANDALONE_PREFIX + "configDir";
        String PROPERTY_STANDALONE_CONFIG_FILES = PROPERTIES_STANDALONE_PREFIX + "configFiles";
    }

    private final Path standaloneServerDir;
    private final Path standaloneConfigDir;

    public JBossServer(String migrationName, ProductInfo productInfo, Path baseDir, MigrationEnvironment migrationEnvironment) {
        super(migrationName, productInfo, baseDir, migrationEnvironment);
        // build server paths from env
        Path standaloneServerDir = FileSystems.getDefault().getPath(migrationEnvironment.getPropertyAsString(getFullEnvironmentPropertyName(EnvironmentProperties.PROPERTY_STANDALONE_SERVER_DIR), "standalone"));
        if (!standaloneServerDir.isAbsolute()) {
            standaloneServerDir = baseDir.resolve(standaloneServerDir);
        }
        this.standaloneServerDir = standaloneServerDir;
        Path standaloneConfigDir = FileSystems.getDefault().getPath(migrationEnvironment.getPropertyAsString(getFullEnvironmentPropertyName(EnvironmentProperties.PROPERTY_STANDALONE_CONFIG_DIR), "configuration"));
        if (!standaloneConfigDir.isAbsolute()) {
            standaloneConfigDir = standaloneServerDir.resolve(standaloneConfigDir);
        }
        this.standaloneConfigDir = standaloneConfigDir;
    }

    protected String getFullEnvironmentPropertyName(String propertyName) {
        return getMigrationName() + "." + propertyName;
    }

    public Collection<ServerPath<S>> getStandaloneConfigs() {
        try {
            final List<ServerPath<S>> standaloneConfigs = new ArrayList<>();

            final List<String> envStandaloneConfigs = getMigrationEnvironment().getPropertyAsList(getFullEnvironmentPropertyName(EnvironmentProperties.PROPERTY_STANDALONE_CONFIG_FILES));
            if (envStandaloneConfigs != null && !envStandaloneConfigs.isEmpty()) {
                for (String envStandaloneConfig : envStandaloneConfigs) {
                    Path standaloneConfig = FileSystems.getDefault().getPath(envStandaloneConfig);
                    if (!standaloneConfig.isAbsolute()) {
                        standaloneConfig = getStandaloneConfigurationDir().resolve(standaloneConfig);
                    }
                    if (Files.exists(standaloneConfig)) {
                        standaloneConfigs.add(new ServerPath(standaloneConfig, this));
                    } else {
                        ServerMigrationLogger.ROOT_LOGGER.warnf("Standalone config file %s, specified by the environment, does not exists.", standaloneConfig);
                    }
                }
            } else {
                // scan config dir
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
                    standaloneConfigs.add(new ServerPath(path, this));
                }
            }
            return Collections.unmodifiableList(standaloneConfigs);
        } catch (IOException e) {
            throw new ServerMigrationFailedException(e);
        }
    }

    public static Path getModulesDir(Path baseDir) {
        return baseDir.resolve("modules");
    }

    public Path getModulesDir() {
        return getModulesDir(getBaseDir());
    }

    public Path getStandaloneDir() {
        return standaloneServerDir;
    }

    public Path getStandaloneConfigurationDir() {
        return standaloneConfigDir;
    }
}
