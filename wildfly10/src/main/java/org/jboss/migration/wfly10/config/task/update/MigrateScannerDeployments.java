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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.console.BasicResultHandlers;
import org.jboss.migration.core.console.UserConfirmation;
import org.jboss.migration.core.env.EnvironmentProperties;
import org.jboss.migration.core.env.EnvironmentProperty;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.jboss.JBossServerConfigurationPath;
import org.jboss.migration.core.jboss.ResolvablePath;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationLeafTask;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PATH;
import static org.jboss.migration.core.console.BasicResultHandlers.UserConfirmation.Result.NO;
import static org.jboss.migration.core.console.BasicResultHandlers.UserConfirmation.Result.YES;

/**
 * @author emmartins
 */
public class MigrateScannerDeployments<S extends JBossServer<S>> extends ManageableServerConfigurationLeafTask.Builder<JBossServerConfigurationPath<S>> {

    private static final EnvironmentProperty<List<String>> ENV_PROPERTY_PROCESSED_DEPLOYMENT_SCANNER_DIRS = EnvironmentProperties.newStringListProperty("processedDeploymentScannerDirs", new ArrayList<>());

    private static final String SCANNER = "scanner";

    public MigrateScannerDeployments() {
        name("deployments.migrate-deployment-scanner-deployments");
        skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
        runBuilder(params -> context -> {
            final JBossServerConfigurationPath sourceConfiguration = params.getSource();
            final ManageableServerConfiguration targetConfiguration = params.getServerConfiguration();
            for (SubsystemResource subsystemResource : targetConfiguration.findResources(SubsystemResource.class, SubsystemNames.DEPLOYMENT_SCANNER)) {
                context.getLogger().debugf("Deployment-scanner found, analysing its configuration %s...", subsystemResource.getResourceAbsoluteName());
                ModelNode subsystemConfig = subsystemResource.getResourceConfiguration();
                if (subsystemConfig != null && subsystemConfig.hasDefined(SCANNER)) {
                    for (Property property : subsystemConfig.get(SCANNER).asPropertyList()) {
                        ModelNode scannerConfig = property.getValue();
                        if (scannerConfig.hasDefined(PATH)) {
                            final ResolvablePath scannerConfigPath = new ResolvablePath(scannerConfig);
                            // resolve scanner's deployments dir for both servers
                            Path sourceDeploymentsDir = sourceConfiguration.resolvePath(scannerConfigPath);
                            Path targetDeploymentsDir = targetConfiguration.resolvePath(scannerConfigPath);
                            if (sourceDeploymentsDir == null) {
                                if (targetDeploymentsDir == null) {
                                    throw new ServerMigrationFailureException("Failed to resolve deployment scanner path "+scannerConfig);
                                } else {
                                    if (targetDeploymentsDir.startsWith(targetConfiguration.getServer().getBaseDir())) {
                                        // relativize for source base dir
                                        sourceDeploymentsDir = sourceConfiguration.getServer().getBaseDir().resolve(targetConfiguration.getServer().getBaseDir().relativize(targetDeploymentsDir));
                                    } else {
                                        sourceDeploymentsDir = targetDeploymentsDir;
                                    }
                                }
                            } else {
                                if (targetDeploymentsDir == null) {
                                    if (sourceDeploymentsDir.startsWith(sourceConfiguration.getServer().getBaseDir())) {
                                        // relativize for target base dir
                                        targetDeploymentsDir = targetConfiguration.getServer().getBaseDir().resolve(sourceConfiguration.getServer().getBaseDir().relativize(sourceDeploymentsDir));
                                    } else {
                                        targetDeploymentsDir = sourceDeploymentsDir;
                                    }
                                }
                            }
                            final TaskEnvironment taskEnvironment = new TaskEnvironment(context);
                            final List<String> processedDeploymentScannerDirs = ENV_PROPERTY_PROCESSED_DEPLOYMENT_SCANNER_DIRS.getValue(taskEnvironment);
                            final String sourceDeploymentsDirAsString = sourceDeploymentsDir.toString();
                            if (processedDeploymentScannerDirs.contains(sourceDeploymentsDirAsString)) {
                                context.getLogger().debugf("Already processed source's deployments directory '%s', skipping it...", sourceDeploymentsDir);
                            } else {
                                processedDeploymentScannerDirs.add(sourceDeploymentsDirAsString);
                                ENV_PROPERTY_PROCESSED_DEPLOYMENT_SCANNER_DIRS.setValue(processedDeploymentScannerDirs, taskEnvironment);
                                context.getLogger().infof("Found deployment scanner '%s' watching directory '%s', searching for deployments in it...", property.getName(), sourceDeploymentsDir);
                                final List<Path> deployments;
                                try {
                                    deployments = Files.list(sourceDeploymentsDir)
                                            .filter(path -> Files.exists(path.resolveSibling(path.getFileName().toString() + ".deployed")))
                                            .map(path -> path.getFileName())
                                            .collect(toList());
                                } catch (IOException e) {
                                    throw new ServerMigrationFailureException("Failed to read the scanner's deployments directory", e);
                                }
                                if (deployments.isEmpty()) {
                                    context.getLogger().debugf("No deployments found in '%s'.", sourceDeploymentsDir);
                                } else {
                                    context.getLogger().infof("Deployments found: %s", deployments);
                                    // find out if all deployments should be migrated
                                    final boolean confirmEachDeployment;
                                    if (context.isInteractive()) {
                                        if (deployments.size() > 1) {
                                            final BasicResultHandlers.UserConfirmation userConfirmation = new BasicResultHandlers.UserConfirmation();
                                            new UserConfirmation(context.getConsoleWrapper(), "Migrate all deployments?", "yes/no?", userConfirmation).execute();
                                            confirmEachDeployment = userConfirmation.getResult() == NO;
                                        } else {
                                            confirmEachDeployment = true;
                                        }
                                    } else {
                                        confirmEachDeployment = false;
                                    }
                                    // execute subtasks
                                    for (Path deployment : deployments) {
                                        final boolean migrateDeployment;
                                        if (confirmEachDeployment) {
                                            final BasicResultHandlers.UserConfirmation userConfirmation = new BasicResultHandlers.UserConfirmation();
                                            new UserConfirmation(context.getConsoleWrapper(), "Migrate deployment '" + deployment + "'?", "yes/no?", userConfirmation).execute();
                                            migrateDeployment = userConfirmation.getResult() == YES;
                                        } else {
                                            // TODO add env property for a config on this decision
                                            migrateDeployment = true;
                                        }
                                        if (migrateDeployment) {
                                            final Path sourcePath = sourceDeploymentsDir.resolve(deployment);
                                            final Path targetPath = targetDeploymentsDir.resolve(deployment);
                                            final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder("deployments.migrate-not-persistent-deployment")
                                                    .addAttribute("source", sourcePath.toString())
                                                    .addAttribute("target", targetPath.toString())
                                                    .build();
                                            context.execute(subtaskName, subtaskContext -> new MigratePath(sourcePath, targetPath).run(subtaskContext));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
        });
    }
}
