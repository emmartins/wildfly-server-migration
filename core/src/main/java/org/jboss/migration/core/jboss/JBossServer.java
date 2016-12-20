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

package org.jboss.migration.core.jboss;

import org.jboss.migration.core.AbstractServer;
import org.jboss.migration.core.ProductInfo;
import org.jboss.migration.core.ServerMigrationFailedException;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.core.util.xml.SimpleXMLFileMatcher;
import org.jboss.migration.core.util.xml.XMLFileMatcher;
import org.jboss.migration.core.util.xml.XMLFiles;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract JBoss {@link org.jboss.migration.core.Server} impl, which is usable only as migration source.
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
    private final Modules modules;

    public JBossServer(String migrationName, ProductInfo productInfo, Path baseDir, MigrationEnvironment migrationEnvironment) {
        super(migrationName, productInfo, baseDir, migrationEnvironment);
        // build server paths from env
        Path domainBaseDir = Paths.get(migrationEnvironment.getPropertyAsString(getFullEnvironmentPropertyName(EnvironmentProperties.PROPERTY_DOMAIN_BASE_DIR), "domain"));
        if (!domainBaseDir.isAbsolute()) {
            domainBaseDir = baseDir.resolve(domainBaseDir);
        }
        this.domainBaseDir = domainBaseDir;
        Path domainConfigDir = Paths.get(migrationEnvironment.getPropertyAsString(getFullEnvironmentPropertyName(EnvironmentProperties.PROPERTY_DOMAIN_CONFIG_DIR), "configuration"));
        if (!domainConfigDir.isAbsolute()) {
            domainConfigDir = domainBaseDir.resolve(domainConfigDir);
        }
        this.domainConfigDir = domainConfigDir;
        Path standaloneServerDir = Paths.get(migrationEnvironment.getPropertyAsString(getFullEnvironmentPropertyName(EnvironmentProperties.PROPERTY_STANDALONE_SERVER_DIR), "standalone"));
        if (!standaloneServerDir.isAbsolute()) {
            standaloneServerDir = baseDir.resolve(standaloneServerDir);
        }
        this.standaloneServerDir = standaloneServerDir;
        Path standaloneConfigDir = Paths.get(migrationEnvironment.getPropertyAsString(getFullEnvironmentPropertyName(EnvironmentProperties.PROPERTY_STANDALONE_CONFIG_DIR), "configuration"));
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
        this.modules = new Modules(baseDir);
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
                    Path config = Paths.get(envConfig);
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

    public Modules getModules() {
        return modules;
    }

    public static class Module {
        private final Path moduleDir;
        private final ModuleSpecification moduleSpecification;

        public Module(Path moduleDir, ModuleSpecification moduleSpecification) {
            this.moduleDir = moduleDir;
            this.moduleSpecification = moduleSpecification;
        }

        public ModuleSpecification getModuleSpecification() {
            return moduleSpecification;
        }

        public Path getModuleDir() {
            return moduleDir;
        }
    }

    public static class Modules {

        private final Path modulesDir;
        private final Path systemLayersBaseDir;
        private final Path overlayDir;

        public Modules(Path serverBaseDir) {
            this.modulesDir = serverBaseDir.resolve("modules");
            this.systemLayersBaseDir = modulesDir.resolve("system").resolve("layers").resolve("base");
            final Path overlaysDir = systemLayersBaseDir.resolve(".overlays");
            final Path overlaysFile = overlaysDir.resolve(".overlays");
            if (Files.exists(overlaysFile)) {
                try {
                    final String activeOverlayFileName = new String(Files.readAllBytes(overlaysFile)).trim();
                    if (!activeOverlayFileName.isEmpty()) {
                        overlayDir = overlaysDir.resolve(activeOverlayFileName);
                    } else {
                        overlayDir = null;
                    }
                } catch (IOException e) {
                    throw new RuntimeException("failed to read overlays file", e);
                }
            } else {
                overlayDir = null;
            }
        }

        public Path getModulesDir() {
            return modulesDir;
        }

        public Module getModule(ModuleIdentifier moduleId) throws IOException {
            final Path moduleDir = getModuleDir(moduleId);
            if (!Files.exists(moduleDir)) {
                return null;
            }
            final Path moduleSpecPath = moduleDir.resolve("module.xml");
            if (!Files.exists(moduleSpecPath)) {
                return null;
            }
            final ModuleSpecification moduleSpecification;
            try {
                moduleSpecification = ModuleSpecification.Parser.parse(moduleSpecPath);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
            return new Module(moduleDir, moduleSpecification);
        }

        public Module getModule(String moduleId) throws IOException {
            return getModule(ModuleIdentifier.fromString(moduleId));
        }

        public Path getModuleDir(ModuleIdentifier moduleId) {
            if (moduleId == null) {
                throw new IllegalArgumentException("The module identifier cannot be null.");
            }
            final Path modulePath = Paths.get(new StringBuilder(moduleId.getName().replace('.', File.separatorChar)).
                    append(File.separator).
                    append(moduleId.getSlot()).
                    toString());
            if (overlayDir != null) {
                final Path overlayModuleDir = overlayDir.resolve(modulePath);
                if (Files.exists(overlayModuleDir)) {
                    return overlayModuleDir;
                }
            }
            final Path systemLayersBaseModuleDir = systemLayersBaseDir.resolve(modulePath);
            return systemLayersBaseModuleDir;
        }
    }
}
