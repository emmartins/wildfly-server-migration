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
import org.jboss.migration.core.console.ConsoleWrapper;
import org.jboss.migration.core.console.UserChoiceWithOtherOption;
import org.jboss.migration.core.console.UserConfirmation;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.jboss.JBossSubsystemNames;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.migration.core.logger.ServerMigrationLogger.ROOT_LOGGER;

/**
 * A task which configures the default Java EE 7 bindings.
 * @author emmartins
 */
public class AddDefaultBindingsConfig<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    public static final String TASK_NAME = "setup-javaee7-default-bindings";

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

    public AddDefaultBindingsConfig() {
        subtaskName(TASK_NAME);
    }

    private static final String SERVER = "server";
    private static final String CONNECTION_FACTORY = "connection-factory";
    private static final String POOLED_CONNECTION_FACTORY = "pooled-connection-factory";
    private static final String DATA_SOURCE = "data-source";
    private static final String DATA_SOURCE_JNDI_NAME = "jndi-name";

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        final PathAddress subsystemPathAddress = subsystemResource.getResourcePathAddress();
        final ManageableServerConfiguration configurationManagement = subsystemResource.getServerConfiguration();
        // read env properties
        final MigrationEnvironment migrationEnvironment = context.getMigrationEnvironment();
        final String defaultDataSourceJndiName = taskEnvironment.getPropertyAsString(EnvironmentProperties.DEFAULT_DATA_SOURCE_JNDI_NAME);
        final String defaultDataSourceName = taskEnvironment.getPropertyAsString(EnvironmentProperties.DEFAULT_DATA_SOURCE_NAME);
        final String defaultJmsConnectionFactoryJndiName = taskEnvironment.getPropertyAsString(EnvironmentProperties.DEFAULT_JMS_CONNECTION_FACTORY_JNDI_NAME);
        final String defaultJmsConnectionFactoryName = taskEnvironment.getPropertyAsString(EnvironmentProperties.DEFAULT_JMS_CONNECTION_FACTORY_NAME);
        // do migration
        final ServerMigrationTaskResult.Builder taskResultBuilder = new ServerMigrationTaskResult.Builder();
        final PathAddress pathAddress = subsystemPathAddress.append(pathElement("service", "default-bindings"));
        final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
        // add ee concurrency utils defaults if configured
        ModelNode eeSubsystemConfig = subsystemResource.getParentResource().getSubsystemResourceConfiguration(JBossSubsystemNames.EE);
        if (eeSubsystemConfig != null) {
            if (eeSubsystemConfig.hasDefined("context-service", "default", "jndi-name")) {
                final String jndiName = eeSubsystemConfig.get("context-service", "default", "jndi-name").asString();
                addOp.get("context-service").set(jndiName);
                taskResultBuilder.addAttribute(TASK_RESULT_ATTR_CONTEXT_SERVICE, jndiName);
            }
            if (eeSubsystemConfig.hasDefined("managed-executor-service", "default", "jndi-name")) {
                final String jndiName = eeSubsystemConfig.get("managed-executor-service", "default", "jndi-name").asString();
                addOp.get("managed-executor-service").set(jndiName);
                taskResultBuilder.addAttribute(TASK_RESULT_ATTR_MANAGED_EXECUTOR_SERVICE, jndiName);
            }
            if (eeSubsystemConfig.hasDefined("managed-scheduled-executor-service", "default", "jndi-name")) {
                final String jndiName = eeSubsystemConfig.get("managed-scheduled-executor-service", "default", "jndi-name").asString();
                addOp.get("managed-scheduled-executor-service").set(jndiName);
                taskResultBuilder.addAttribute(TASK_RESULT_ATTR_MANAGED_SCHEDULED_EXECUTOR_SERVICE, jndiName);
            }
            if (eeSubsystemConfig.hasDefined("managed-thread-factory", "default", "jndi-name")) {
                final String jndiName = eeSubsystemConfig.get("managed-thread-factory", "default", "jndi-name").asString();
                addOp.get("managed-thread-factory").set(jndiName);
                taskResultBuilder.addAttribute(TASK_RESULT_ATTR_MANAGED_THREAD_FACTORY, jndiName);
            }
        }
        setupDefaultDatasource(defaultDataSourceJndiName, defaultDataSourceName, addOp, subsystemResource.getParentResource(), context, taskEnvironment, taskResultBuilder);
        setupDefaultJMSConnectionFactory(defaultJmsConnectionFactoryJndiName, defaultJmsConnectionFactoryName, addOp, subsystemResource.getParentResource(), context, taskEnvironment, taskResultBuilder);
        configurationManagement.executeManagementOperation(addOp);
        context.getLogger().infof("Java EE Default Bindings configured.");
        return taskResultBuilder.success().build();
    }

    private void setupDefaultJMSConnectionFactory(String defaultJmsConnectionFactoryJndiName, String defaultJmsConnectionFactoryName, final ModelNode addOp, SubsystemResource.Parent subsystemResources, final TaskContext context, final TaskEnvironment taskEnvironment, final ServerMigrationTaskResult.Builder taskResultBuilder) {
        // if the subsystem config defines expected default resource then use it
        final SubsystemResource resource = subsystemResources.getSubsystemResource(JBossSubsystemNames.MESSAGING_ACTIVEMQ);
        if (resource == null) {
            return;
        }
        ModelNode subsystemConfig = resource.getResourceConfiguration();
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
            context.getLogger().infof("Java EE Default JMS Connection Builder configured with JNDI name %s.", defaultJmsConnectionFactoryJndiName);
        } else {
            if (context.isInteractive()) {
                context.getLogger().infof("Default JMS Connection Builder not found");
            } else {
                // not interactive, skip it
                context.getLogger().infof("Default JMS Connection Builder not found, skipping its configuration due to non interactive mode");
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
                public void onChoice(String choice) {
                    final ConfiguredJmsConnectionFactory configuredJmsConnectionFactory = factoryNamesMap.get(choice);
                    final ModelNode jmsConnectionFactory = subsystemConfig.get(SERVER, configuredJmsConnectionFactory.serverName, configuredJmsConnectionFactory.factoryType, configuredJmsConnectionFactory.factoryName);
                    final String jmsConnectionFactoryJndiName = jmsConnectionFactory.get("entries").asList().get(0).asString();
                    processJmsConnectionFactoryJndiName(jmsConnectionFactoryJndiName);
                }
                @Override
                public void onError() {
                }
                @Override
                public void onOther(String otherChoice) {
                    processJmsConnectionFactoryJndiName(otherChoice);
                }
                private void processJmsConnectionFactoryJndiName(final String jmsConnectionFactoryJndiName) {
                    addOp.get("jms-connection-factory").set(jmsConnectionFactoryJndiName);
                    taskResultBuilder.addAttribute(TASK_RESULT_ATTR_JMS_CONNECTION_FACTORY, jmsConnectionFactoryJndiName);
                    context.getLogger().infof("Java EE Default JMS Connection Builder configured with JNDI name %s.", jmsConnectionFactoryJndiName);
                    final UserConfirmation.ResultHandler resultHandler = new org.jboss.migration.core.console.UserConfirmation.ResultHandler() {
                        @Override
                        public void onNo() {
                        }
                        @Override
                        public void onYes() {
                            // set env property
                            taskEnvironment.setProperty(EnvironmentProperties.DEFAULT_JMS_CONNECTION_FACTORY_JNDI_NAME, jmsConnectionFactoryJndiName);
                        }
                        @Override
                        public void onError() {
                            // repeat
                            processJmsConnectionFactoryJndiName(jmsConnectionFactoryJndiName);
                        }
                    };
                    final ConsoleWrapper consoleWrapper = context.getConsoleWrapper();
                    consoleWrapper.printf("%n");
                    new UserConfirmation(consoleWrapper, "Save this Java EE Default JMS Connection Builder JNDI name and use it when migrating other config files?", ROOT_LOGGER.yesNo(), resultHandler).execute();
                }
            };
            new UserChoiceWithOtherOption(context.getConsoleWrapper(), factoryNames, "Unconfigured JMS Connection Builder, I want to enter the JNDI name...", "Please select Java EE's Default JMS Connection Builder: ", resultHandler).execute();
        }
    }

    private void setupDefaultDatasource(String defaultDataSourceJndiName, final String defaultDataSourceName, final ModelNode addOp, SubsystemResource.Parent subsystemResources, final TaskContext context, final TaskEnvironment taskEnvironment, final ServerMigrationTaskResult.Builder taskResultBuilder) {
        // if the subsystem config defines expected default resource then use it
        final SubsystemResource resource = subsystemResources.getSubsystemResource(JBossSubsystemNames.DATASOURCES);
        if (resource == null) {
            return;
        }
        final ModelNode subsystemConfig = resource.getResourceConfiguration();
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
            if (context.isInteractive()) {
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
                public void onChoice(String choice) {
                    processDatasourceJndiName(subsystemConfig.get(DATA_SOURCE, choice).get(DATA_SOURCE_JNDI_NAME).asString());
                }
                @Override
                public void onError() {
                }
                @Override
                public void onOther(String otherChoice) {
                    processDatasourceJndiName(otherChoice);
                }
                private void processDatasourceJndiName(final String datasourceJndiName) {
                    addOp.get("datasource").set(datasourceJndiName);
                    taskResultBuilder.addAttribute(TASK_RESULT_ATTR_DATA_SOURCE, datasourceJndiName);
                    context.getLogger().infof("Java EE Default Datasource configured with JNDI name %s.", datasourceJndiName);
                    final UserConfirmation.ResultHandler resultHandler = new org.jboss.migration.core.console.UserConfirmation.ResultHandler() {
                        @Override
                        public void onNo() {
                        }
                        @Override
                        public void onYes() {
                            // set env property
                            taskEnvironment.setProperty(EnvironmentProperties.DEFAULT_DATA_SOURCE_JNDI_NAME, datasourceJndiName);
                        }
                        @Override
                        public void onError() {
                            // repeat
                            processDatasourceJndiName(datasourceJndiName);
                        }
                    };
                    final ConsoleWrapper consoleWrapper = context.getConsoleWrapper();
                    consoleWrapper.printf("%n");
                    new UserConfirmation(consoleWrapper, "Save this Java EE Default Datasource JNDI name and use it when migrating other config files?", ROOT_LOGGER.yesNo(), resultHandler).execute();
                }
            };
            new UserChoiceWithOtherOption(context.getConsoleWrapper(), dataSourceNames, "Unconfigured data source, I want to enter the JNDI name...", "Please select Java EE's Default Datasource: ", resultHandler).execute();
        }
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
