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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract JBoss {@link Server} impl, which is usable only as migration source.
 * @author emmartins
 */
public abstract class JBossServer<S extends JBossServer> extends AbstractServer {

    public interface EnvironmentProperties {
        String PROPERTIES_PREFIX = "server.";

        String PROPERTIES_DOMAIN_PREFIX = PROPERTIES_PREFIX + "domain.";
        String PROPERTY_DOMAIN_BASE_DIR = PROPERTIES_DOMAIN_PREFIX + "domainDir";
        String PROPERTY_DOMAIN_CONFIG_DIR = PROPERTIES_DOMAIN_PREFIX + "configDir";
        String PROPERTY_DOMAIN_DOMAIN_CONFIG_FILES = PROPERTIES_DOMAIN_PREFIX + "domainConfigFiles";
        String PROPERTY_DOMAIN_HOST_CONFIG_FILES = PROPERTIES_DOMAIN_PREFIX + "hostConfigFiles";

        String PROPERTIES_STANDALONE_PREFIX = PROPERTIES_PREFIX + "standalone.";
        String PROPERTY_STANDALONE_SERVER_DIR = PROPERTIES_STANDALONE_PREFIX + "serverDir";
        String PROPERTY_STANDALONE_CONFIG_DIR = PROPERTIES_STANDALONE_PREFIX + "configDir";
        String PROPERTY_STANDALONE_CONFIG_FILES = PROPERTIES_STANDALONE_PREFIX + "configFiles";
    }

    private final Path domainBaseDir;
    private final Path domainConfigDir;
    private final Path standaloneServerDir;
    private final Path standaloneConfigDir;
    private final Map<String, Path> pathResolver;

    public JBossServer(String migrationName, ProductInfo productInfo, Path baseDir, MigrationEnvironment migrationEnvironment) {
        super(migrationName, productInfo, baseDir, migrationEnvironment);
        // build server paths from env
        Path domainBaseDir = FileSystems.getDefault().getPath(migrationEnvironment.getPropertyAsString(getFullEnvironmentPropertyName(EnvironmentProperties.PROPERTY_DOMAIN_BASE_DIR), "domain"));
        if (!domainBaseDir.isAbsolute()) {
            domainBaseDir = baseDir.resolve(domainBaseDir);
        }
        this.domainBaseDir = domainBaseDir;
        Path domainConfigDir = FileSystems.getDefault().getPath(migrationEnvironment.getPropertyAsString(getFullEnvironmentPropertyName(EnvironmentProperties.PROPERTY_DOMAIN_CONFIG_DIR), "configuration"));
        if (!domainConfigDir.isAbsolute()) {
            domainConfigDir = domainBaseDir.resolve(domainConfigDir);
        }
        this.domainConfigDir = domainConfigDir;
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
        this.pathResolver = new HashMap<>();
        this.pathResolver.put("jboss.server.base.dir", standaloneServerDir);
        this.pathResolver.put("jboss.server.config.dir", standaloneConfigDir);
        this.pathResolver.put("jboss.server.data.dir", standaloneServerDir.resolve("data"));
        this.pathResolver.put("jboss.server.log.dir", standaloneServerDir.resolve("log"));
        this.pathResolver.put("jboss.domain.base.dir", domainBaseDir);
        this.pathResolver.put("jboss.domain.config.dir", domainConfigDir);
        this.pathResolver.put("jboss.domain.data.dir", domainBaseDir.resolve("data"));
    }

    protected String getFullEnvironmentPropertyName(String propertyName) {
        return getMigrationName() + "." + propertyName;
    }

    protected Collection<ServerPath<S>> getConfigs(final Path configurationDir, final String xmlDocumentElementName, final String envPropertyName) {
        try {
            final List<ServerPath<S>> configs = new ArrayList<>();
            final String fullEnvPropertyName = getFullEnvironmentPropertyName(envPropertyName);
            final List<String> envConfigs = getMigrationEnvironment().getPropertyAsList(fullEnvPropertyName);
            if (envConfigs != null && !envConfigs.isEmpty()) {
                for (String envConfig : envConfigs) {
                    Path config = FileSystems.getDefault().getPath(envConfig);
                    if (!config.isAbsolute()) {
                        config = configurationDir.resolve(config);
                    }
                    if (Files.exists(config)) {
                        configs.add(new ServerPath(config, this));
                    } else {
                        ServerMigrationLogger.ROOT_LOGGER.warnf("Config file %s, specified by the environment property %s, does not exists.", config, fullEnvPropertyName);
                    }
                }
            } else {
                // scan config dir
                final XMLFileMatcher scanMatcher = new SimpleXMLFileMatcher() {
                    @Override
                    protected boolean documentElementLocalNameMatches(String localName) {
                        return xmlDocumentElementName.equals(localName);
                    }
                    @Override
                    protected boolean documentNamespaceURIMatches(String namespaceURI) {
                        return namespaceURI.startsWith("urn:jboss:domain:");
                    }
                };
                for (Path path : XMLFiles.scan(configurationDir, false, scanMatcher)) {
                    configs.add(new ServerPath(path, this));
                }
            }
            return Collections.unmodifiableList(configs);
        } catch (IOException e) {
            throw new ServerMigrationFailedException(e);
        }
    }

    public Collection<ServerPath<S>> getStandaloneConfigs() {
        return getConfigs(getStandaloneConfigurationDir(), "server", EnvironmentProperties.PROPERTY_STANDALONE_CONFIG_FILES);
    }

    public Collection<ServerPath<S>> getDomainDomainConfigs() {
        return getConfigs(getDomainConfigurationDir(), "domain", EnvironmentProperties.PROPERTY_DOMAIN_DOMAIN_CONFIG_FILES);
    }

    public Collection<ServerPath<S>> getDomainHostConfigs() {
        return getConfigs(getDomainConfigurationDir(), "host", EnvironmentProperties.PROPERTY_DOMAIN_HOST_CONFIG_FILES);
    }

    public static Path getModulesDir(Path baseDir) {
        return baseDir.resolve("modules");
    }

    public Path getModulesDir() {
        return getModulesDir(getBaseDir());
    }

    public Path getDomainDir() {
        return domainBaseDir;
    }

    public Path getDomainConfigurationDir() {
        return domainConfigDir;
    }

    public Path getStandaloneDir() {
        return standaloneServerDir;
    }

    public Path getStandaloneConfigurationDir() {
        return standaloneConfigDir;
    }

    @Override
    public Path resolvePath(String path) {
        return pathResolver.get(path);
    }
}
