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
package org.jboss.migration.wfly10.config.task.subsystem.ee;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.console.ConsoleWrapper;
import org.jboss.migration.core.console.UserChoiceWithOtherOption;
import org.jboss.migration.core.console.UserConfirmation;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;
import org.jboss.migration.wfly10.config.task.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.config.task.subsystem.WildFly10SubsystemMigrationTask;
import org.jboss.migration.wfly10.config.task.subsystem.WildFly10SubsystemMigrationTaskFactory;

import java.util.HashMap;
import java.util.Map;

import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.migration.core.logger.ServerMigrationLogger.ROOT_LOGGER;

/**
 * A task which configures the default Java EE 7 bindings.
 * @author emmartins
 */
public class AddDefaultBindingsConfig implements WildFly10SubsystemMigrationTaskFactory {

    public static final AddDefaultBindingsConfig INSTANCE = new AddDefaultBindingsConfig();

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder("setup-javaee7-default-bindings").build();

    public interface EnvironmentProperties {
        String DEFAULT_DATA_SOURCE_JNDI_NAME = "defaultDataSourceJndiName";
        String DEFAULT_DATA_SOURCE_NAME = "defaultDataSourceName";
        String DEFAULT_JMS_CONNECTION_FACTORY_JNDI_NAME = "defaultJmsConnectionFactoryJndiName";
        String DEFAULT_JMS_CONNECTION_FACTORY_NAME = "defaultJmsConnectionFactoryName";
    }

    public static final String TASK_RESULT_ATTR_CONTEXT_SERVICE = "default-context-service";
    public static final String TASK_RESULT_ATTR_MANAGED_THREAD_FACTORY = "default-managed-thread-factory";
    public static final String TASK_RESULT_ATTR_MANAGED_EXECUTOR_SERVICE = "default-managed-executor-service";
    public static final String TASK_RESULT_ATTR_MANAGED_SCHEDULED_EXECUTOR_SERVICE = "default-managed-scheduled-executor-service";
    public static final String TASK_RESULT_ATTR_DATA_SOURCE = "default-data-source";
    public static final String TASK_RESULT_ATTR_JMS_CONNECTION_FACTORY = "default-jms-connection-factory";

    private AddDefaultBindingsConfig() {
    }

    private static final String SERVER = "server";
    private static final String CONNECTION_FACTORY = "connection-factory";
    private static final String POOLED_CONNECTION_FACTORY = "pooled-connection-factory";
    private static final String DATA_SOURCE = "data-source";
    private static final String DATA_SOURCE_JNDI_NAME = "jndi-name";

    @Override
    public ServerMigrationTask getServerMigrationTask(final ModelNode config, WildFly10Subsystem subsystem, SubsystemsManagement subsystemsManagement) {
        return new WildFly10SubsystemMigrationTask(config, subsystem, subsystemsManagement) {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            protected ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, SubsystemsManagement subsystemsManagement, ServerMigrationTaskContext context, TaskEnvironment taskEnvironment) throws Exception {
                if (config == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final PathAddress subsystemPathAddress = subsystemsManagement.getResourcePathAddress(subsystem.getName());
                final ManageableServerConfiguration configurationManagement = subsystemsManagement.getServerConfiguration();
                // read env properties
                final MigrationEnvironment migrationEnvironment = context.getServerMigrationContext().getMigrationEnvironment();
                final String defaultDataSourceJndiName = taskEnvironment.getPropertyAsString(EnvironmentProperties.DEFAULT_DATA_SOURCE_JNDI_NAME);
                final String defaultDataSourceName = taskEnvironment.getPropertyAsString(EnvironmentProperties.DEFAULT_DATA_SOURCE_NAME);
                final String defaultJmsConnectionFactoryJndiName = taskEnvironment.getPropertyAsString(EnvironmentProperties.DEFAULT_JMS_CONNECTION_FACTORY_JNDI_NAME);
                final String defaultJmsConnectionFactoryName = taskEnvironment.getPropertyAsString(EnvironmentProperties.DEFAULT_JMS_CONNECTION_FACTORY_NAME);
                // do migration
                final ServerMigrationTaskResult.Builder taskResultBuilder = new ServerMigrationTaskResult.Builder();
                final PathAddress pathAddress = subsystemPathAddress.append(pathElement("service", "default-bindings"));
                final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                // add ee concurrency utils defaults if related task was not skipped
                final boolean addConcurrencyUtilitiesDefaultConfigSkipped = new TaskEnvironment(migrationEnvironment, org.jboss.migration.wfly10.config.task.subsystem.EnvironmentProperties.getSubsystemSubtaskPropertiesPrefix(subsystem.getName(), AddConcurrencyUtilitiesDefaultConfig.SERVER_MIGRATION_TASK_NAME.getName())).isSkippedByEnvironment();
                if (!addConcurrencyUtilitiesDefaultConfigSkipped) {
                    addOp.get("context-service").set(AddConcurrencyUtilitiesDefaultConfig.DEFAULT_CONTEXT_SERVICE_JNDI_NAME);
                    taskResultBuilder.addAttribute(TASK_RESULT_ATTR_CONTEXT_SERVICE, AddConcurrencyUtilitiesDefaultConfig.DEFAULT_CONTEXT_SERVICE_JNDI_NAME);
                    addOp.get("managed-executor-service").set(AddConcurrencyUtilitiesDefaultConfig.DEFAULT_MANAGED_EXECUTOR_SERVICE_JNDI_NAME);
                    taskResultBuilder.addAttribute(TASK_RESULT_ATTR_MANAGED_EXECUTOR_SERVICE, AddConcurrencyUtilitiesDefaultConfig.DEFAULT_MANAGED_EXECUTOR_SERVICE_JNDI_NAME);
                    addOp.get("managed-scheduled-executor-service").set(AddConcurrencyUtilitiesDefaultConfig.DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_JNDI_NAME);
                    taskResultBuilder.addAttribute(TASK_RESULT_ATTR_MANAGED_SCHEDULED_EXECUTOR_SERVICE, AddConcurrencyUtilitiesDefaultConfig.DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_JNDI_NAME);
                    addOp.get("managed-thread-factory").set(AddConcurrencyUtilitiesDefaultConfig.DEFAULT_MANAGED_THREAD_FACTORY_JNDI_NAME);
                    taskResultBuilder.addAttribute(TASK_RESULT_ATTR_MANAGED_THREAD_FACTORY, AddConcurrencyUtilitiesDefaultConfig.DEFAULT_MANAGED_THREAD_FACTORY_JNDI_NAME);
                }
                setupDefaultDatasource(defaultDataSourceJndiName, defaultDataSourceName, addOp, subsystemsManagement, context, taskEnvironment, taskResultBuilder);
                setupDefaultJMSConnectionFactory(defaultJmsConnectionFactoryJndiName, defaultJmsConnectionFactoryName, addOp, subsystemsManagement, context, taskEnvironment, taskResultBuilder);
                configurationManagement.executeManagementOperation(addOp);
                context.getLogger().infof("Java EE Default Bindings configured.");
                return taskResultBuilder.sucess().build();
            }
            private void setupDefaultJMSConnectionFactory(String defaultJmsConnectionFactoryJndiName, String defaultJmsConnectionFactoryName, final ModelNode addOp, SubsystemsManagement subsystemsManagement, final ServerMigrationTaskContext context, final TaskEnvironment taskEnvironment, final ServerMigrationTaskResult.Builder taskResultBuilder) throws Exception {
                // if the subsystem config defines expected default resource then use it
                final ModelNode subsystemConfig = subsystemsManagement.getResource(SubsystemNames.MESSAGING_ACTIVEMQ);
                if (subsystemConfig == null) {
                    return;
                }
                // retrieve jndi name from env and subsystem config
                if (defaultJmsConnectionFactoryJndiName == null) {
                    // env does not specify a jndi name
                    if (defaultJmsConnectionFactoryName != null && !defaultJmsConnectionFactoryName.isEmpty()) {
                        if (subsystemConfig.hasDefined(SERVER)) {
                            for (String serverName : subsystemConfig.get(SERVER).keys()) {
                                ModelNode defaultJmsConnectionFactory = null;
                                if (subsystemConfig.hasDefined(SERVER, serverName, POOLED_CONNECTION_FACTORY, defaultJmsConnectionFactoryName)) {
                                    defaultJmsConnectionFactory = subsystemConfig.get(SERVER, serverName, POOLED_CONNECTION_FACTORY, defaultJmsConnectionFactoryName);
                                } else if (subsystemConfig.hasDefined(SERVER, serverName, CONNECTION_FACTORY, defaultJmsConnectionFactoryName)) {
                                    defaultJmsConnectionFactory = subsystemConfig.get(SERVER, serverName, CONNECTION_FACTORY, defaultJmsConnectionFactoryName);
                                }
                                if (defaultJmsConnectionFactory != null) {
                                    defaultJmsConnectionFactoryJndiName = defaultJmsConnectionFactory.get("entries").asList().get(0).asString();
                                }
                            }
                        }
                    }
                } else if (defaultJmsConnectionFactoryJndiName.isEmpty()) {
                    defaultJmsConnectionFactoryJndiName = null;
                }
                if (defaultJmsConnectionFactoryJndiName != null) {
                    addOp.get("jms-connection-factory").set(defaultJmsConnectionFactoryJndiName);
                    taskResultBuilder.addAttribute(TASK_RESULT_ATTR_JMS_CONNECTION_FACTORY, defaultJmsConnectionFactoryJndiName);
                    context.getLogger().infof("Java EE Default JMS Connection Factory configured with JNDI name %s.", defaultJmsConnectionFactoryJndiName);
                } else {
                    if (context.getServerMigrationContext().isInteractive()) {
                        context.getLogger().infof("Default JMS Connection Factory not found");
                    } else {
                        // not interactive, skip it
                        context.getLogger().infof("Default JMS Connection Factory not found, skipping its configuration due to non interactive mode");
                        return;
                    }
                    // retrieve the names of configured factories
                    final Map<String, ConfiguredJmsConnectionFactory> factoryNamesMap = new HashMap<>();
                    if (subsystemConfig.hasDefined(SERVER)) {
                        for (String serverName : subsystemConfig.get(SERVER).keys()) {
                            if (subsystemConfig.hasDefined(SERVER, serverName, CONNECTION_FACTORY)) {
                                for (String factoryName : subsystemConfig.get(SERVER, serverName, CONNECTION_FACTORY).keys()) {
                                    final ConfiguredJmsConnectionFactory configuredJmsConnectionFactory = new ConfiguredJmsConnectionFactory();
                                    configuredJmsConnectionFactory.serverName = serverName;
                                    configuredJmsConnectionFactory.factoryType = CONNECTION_FACTORY;
                                    configuredJmsConnectionFactory.factoryName = factoryName;
                                    factoryNamesMap.put(configuredJmsConnectionFactory.toString(), configuredJmsConnectionFactory);
                                }
                            }
                            if (subsystemConfig.hasDefined(SERVER, serverName, POOLED_CONNECTION_FACTORY)) {
                                for (String factoryName : subsystemConfig.get(SERVER, serverName, POOLED_CONNECTION_FACTORY).keys()) {
                                    final ConfiguredJmsConnectionFactory configuredJmsConnectionFactory = new ConfiguredJmsConnectionFactory();
                                    configuredJmsConnectionFactory.serverName = serverName;
                                    configuredJmsConnectionFactory.factoryType = POOLED_CONNECTION_FACTORY;
                                    configuredJmsConnectionFactory.factoryName = factoryName;
                                    factoryNamesMap.put(configuredJmsConnectionFactory.toString(), configuredJmsConnectionFactory);
                                }
                            }
                        }
                    }
                    final String[] factoryNames = factoryNamesMap.keySet().toArray(new String[factoryNamesMap.keySet().size()]);
                    final UserChoiceWithOtherOption.ResultHandler resultHandler = new UserChoiceWithOtherOption.ResultHandler() {
                        @Override
                        public void onChoice(String choice) throws Exception {
                            final ConfiguredJmsConnectionFactory configuredJmsConnectionFactory = factoryNamesMap.get(choice);
                            final ModelNode jmsConnectionFactory = subsystemConfig.get(SERVER, configuredJmsConnectionFactory.serverName, configuredJmsConnectionFactory.factoryType, configuredJmsConnectionFactory.factoryName);
                            final String jmsConnectionFactoryJndiName = jmsConnectionFactory.get("entries").asList().get(0).asString();
                            processJmsConnectionFactoryJndiName(jmsConnectionFactoryJndiName);
                        }
                        @Override
                        public void onError() throws Exception {
                        }
                        @Override
                        public void onOther(String otherChoice) throws Exception {
                            processJmsConnectionFactoryJndiName(otherChoice);
                        }
                        private void processJmsConnectionFactoryJndiName(final String jmsConnectionFactoryJndiName) throws Exception {
                            addOp.get("jms-connection-factory").set(jmsConnectionFactoryJndiName);
                            taskResultBuilder.addAttribute(TASK_RESULT_ATTR_JMS_CONNECTION_FACTORY, jmsConnectionFactoryJndiName);
                            context.getLogger().infof("Java EE Default JMS Connection Factory configured with JNDI name %s.", jmsConnectionFactoryJndiName);
                            final UserConfirmation.ResultHandler resultHandler = new org.jboss.migration.core.console.UserConfirmation.ResultHandler() {
                                @Override
                                public void onNo() throws Exception {
                                }
                                @Override
                                public void onYes() throws Exception {
                                    // set env property
                                    taskEnvironment.setProperty(EnvironmentProperties.DEFAULT_JMS_CONNECTION_FACTORY_JNDI_NAME, jmsConnectionFactoryJndiName);
                                }
                                @Override
                                public void onError() throws Exception {
                                    // repeat
                                    processJmsConnectionFactoryJndiName(jmsConnectionFactoryJndiName);
                                }
                            };
                            final ConsoleWrapper consoleWrapper = context.getServerMigrationContext().getConsoleWrapper();
                            consoleWrapper.printf("%n");
                            new UserConfirmation(consoleWrapper, "Save this Java EE Default JMS Connection Factory JNDI name and use it when migrating other config files?", ROOT_LOGGER.yesNo(), resultHandler).execute();
                        }
                    };
                    new UserChoiceWithOtherOption(context.getServerMigrationContext().getConsoleWrapper(), factoryNames, "Unconfigured JMS Connection Factory, I want to enter the JNDI name...", "Please select Java EE's Default JMS Connection Factory: ", resultHandler).execute();
                }
            }
            private void setupDefaultDatasource(String defaultDataSourceJndiName, final String defaultDataSourceName, final ModelNode addOp, SubsystemsManagement subsystemsManagement, final ServerMigrationTaskContext context, final TaskEnvironment taskEnvironment, final ServerMigrationTaskResult.Builder taskResultBuilder) throws Exception {
                // if the subsystem config defines expected default resource then use it
                final ModelNode subsystemConfig = subsystemsManagement.getResource(SubsystemNames.DATASOURCES);
                if (subsystemConfig == null) {
                    return;
                }
                if (defaultDataSourceJndiName == null) {
                    if (defaultDataSourceName != null && !defaultDataSourceName.isEmpty()) {
                        if (subsystemConfig.hasDefined(DATA_SOURCE, defaultDataSourceName)) {
                            // default datasource found, use it
                            final ModelNode defaultDatasource = subsystemConfig.get(DATA_SOURCE, defaultDataSourceName);
                            defaultDataSourceJndiName = defaultDatasource.get(DATA_SOURCE_JNDI_NAME).asString();
                        }
                    }
                } else if (defaultDataSourceJndiName.isEmpty()) {
                    defaultDataSourceJndiName = null;
                }
                if (defaultDataSourceJndiName != null) {
                    addOp.get("datasource").set(defaultDataSourceJndiName);
                    taskResultBuilder.addAttribute(TASK_RESULT_ATTR_DATA_SOURCE, defaultDataSourceJndiName);
                    context.getLogger().infof("Java EE Default Datasource configured with JNDI name %s.", defaultDataSourceJndiName);
                } else {
                    if (context.getServerMigrationContext().isInteractive()) {
                        context.getLogger().infof("Default datasource not found.");
                    } else {
                        // not interactive, skip it
                        context.getLogger().infof("Default datasource not found, skipping its configuration due to non interactive mode");
                        return;
                    }
                    // retrieve the names of configured datasources
                    final String[] dataSourceNames;
                    if (subsystemConfig.hasDefined(DATA_SOURCE)) {
                        dataSourceNames =  subsystemConfig.get(DATA_SOURCE).keys().toArray(new String[]{});
                    } else {
                        dataSourceNames = new String[]{};
                    }
                    final UserChoiceWithOtherOption.ResultHandler resultHandler = new UserChoiceWithOtherOption.ResultHandler() {
                        @Override
                        public void onChoice(String choice) throws Exception {
                            processDatasourceJndiName(subsystemConfig.get(DATA_SOURCE, choice).get(DATA_SOURCE_JNDI_NAME).asString());
                        }
                        @Override
                        public void onError() throws Exception {
                        }
                        @Override
                        public void onOther(String otherChoice) throws Exception {
                            processDatasourceJndiName(otherChoice);
                        }
                        private void processDatasourceJndiName(final String datasourceJndiName) throws Exception {
                            addOp.get("datasource").set(datasourceJndiName);
                            taskResultBuilder.addAttribute(TASK_RESULT_ATTR_DATA_SOURCE, datasourceJndiName);
                            context.getLogger().infof("Java EE Default Datasource configured with JNDI name %s.", datasourceJndiName);
                            final UserConfirmation.ResultHandler resultHandler = new org.jboss.migration.core.console.UserConfirmation.ResultHandler() {
                                @Override
                                public void onNo() throws Exception {
                                }
                                @Override
                                public void onYes() throws Exception {
                                    // set env property
                                    taskEnvironment.setProperty(EnvironmentProperties.DEFAULT_DATA_SOURCE_JNDI_NAME, datasourceJndiName);
                                }
                                @Override
                                public void onError() throws Exception {
                                    // repeat
                                    processDatasourceJndiName(datasourceJndiName);
                                }
                            };
                            final ConsoleWrapper consoleWrapper = context.getServerMigrationContext().getConsoleWrapper();
                            consoleWrapper.printf("%n");
                            new UserConfirmation(consoleWrapper, "Save this Java EE Default Datasource JNDI name and use it when migrating other config files?", ROOT_LOGGER.yesNo(), resultHandler).execute();
                        }
                    };
                    new UserChoiceWithOtherOption(context.getServerMigrationContext().getConsoleWrapper(), dataSourceNames, "Unconfigured data source, I want to enter the JNDI name...", "Please select Java EE's Default Datasource: ", resultHandler).execute();
                }
            }
        };
    }

    private static class ConfiguredJmsConnectionFactory {
        private String serverName;
        private String factoryType;
        private String factoryName;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConfiguredJmsConnectionFactory that = (ConfiguredJmsConnectionFactory) o;

            if (!serverName.equals(that.serverName)) return false;
            if (!factoryType.equals(that.factoryType)) return false;
            return factoryName.equals(that.factoryName);

        }

        @Override
        public int hashCode() {
            int result = serverName.hashCode();
            result = 31 * result + factoryType.hashCode();
            result = 31 * result + factoryName.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "server = "+serverName+", factory type = "+factoryType+", factory name = "+factoryName;
        }
    }
}
