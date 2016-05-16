/*
 * Copyright 2016 Red Hat, Inc.
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
package org.jboss.migration.wfly10.subsystem.ee;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskId;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.console.UserChoiceWithOtherOption;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTask;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTaskFactory;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemNames;

import java.util.HashMap;
import java.util.Map;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * A task which adds the default EE Concurrency Utilities config to the subsystem.
 * @author emmartins
 */
public class AddDefaultBindingsConfig implements WildFly10SubsystemMigrationTaskFactory {

    public static final AddDefaultBindingsConfig INSTANCE = new AddDefaultBindingsConfig();

    public static final ServerMigrationTaskId SERVER_MIGRATION_TASK_ID = new ServerMigrationTaskId.Builder().setName("Configure Java EE 7 default bindings").build();

    private AddDefaultBindingsConfig() {
    }

    private static final String SERVER = "server";
    private static final String CONNECTION_FACTORY = "connection-factory";
    private static final String POOLED_CONNECTION_FACTORY = "pooled-connection-factory";
    private static final String DATA_SOURCE = "data-source";
    private static final String DATA_SOURCE_JNDI_NAME = "jndi-name";
    // TODO move to migration context properties
    private static final String DEFAULT_DATASOURCE_NAME = "ExampleDS";
    private static final String DEFAULT_JMS_CONNECTION_FACTORY_NAME = "hornetq-ra";
    private static final String DEFAULT_JMS_SERVER_NAME = "default";

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server) {
        return new WildFly10SubsystemMigrationTask(config, subsystem, server) {
            @Override
            public ServerMigrationTaskId getId() {
                return SERVER_MIGRATION_TASK_ID;
            }
            @Override
            protected ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationTaskContext context) throws Exception {
                if (config == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final PathAddress pathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement("service", "default-bindings"));
                final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                addOp.get("context-service").set(AddConcurrencyUtilitiesDefaultConfig.DEFAULT_CONTEXT_SERVICE_JNDI_NAME);
                addOp.get("managed-executor-service").set(AddConcurrencyUtilitiesDefaultConfig.DEFAULT_MANAGED_EXECUTOR_SERVICE_JNDI_NAME);
                addOp.get("managed-scheduled-executor-service").set(AddConcurrencyUtilitiesDefaultConfig.DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_JNDI_NAME);
                addOp.get("managed-thread-factory").set(AddConcurrencyUtilitiesDefaultConfig.DEFAULT_MANAGED_THREAD_FACTORY_JNDI_NAME);
                setupDefaultDatasource(addOp, server, context);
                setupDefaultJMSConnectionFactory(addOp, server, context);
                server.executeManagementOperation(addOp);
                ServerMigrationLogger.ROOT_LOGGER.infof("Java EE Default Bindings configured.");
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }

    private void setupDefaultJMSConnectionFactory(final ModelNode addOp, WildFly10StandaloneServer server, ServerMigrationTaskContext context) throws Exception {
        // if the subsystem config defines expected default resource then use it
        final ModelNode subsystemConfig = server.getSubsystem(WildFly10SubsystemNames.MESSAGING_ACTIVEMQ);
        if (subsystemConfig == null) {
            return;
        }
        if (subsystemConfig.hasDefined(SERVER, DEFAULT_JMS_SERVER_NAME, POOLED_CONNECTION_FACTORY, DEFAULT_JMS_CONNECTION_FACTORY_NAME)) {
            // eap 6.4 default standalone config's example jms connection factory found, use it
            final ModelNode defaultJmsConnectionFactory = subsystemConfig.get(SERVER, DEFAULT_JMS_SERVER_NAME, POOLED_CONNECTION_FACTORY, DEFAULT_JMS_CONNECTION_FACTORY_NAME);
            final String defaultJmsConnectionFactoryJndiName = defaultJmsConnectionFactory.get("entries").asList().get(0).asString();
            addOp.get("jms-connection-factory").set(defaultJmsConnectionFactoryJndiName);
            ServerMigrationLogger.ROOT_LOGGER.infof("Default JMS Connection Factory %s found and set as the Java EE Default JMS Connection Factory.", DEFAULT_JMS_CONNECTION_FACTORY_NAME);
        } else {
            if (context.getServerMigrationContext().isInteractive()) {
                ServerMigrationLogger.ROOT_LOGGER.infof("Default JMS Connection Factory not found");
            } else {
                // not interactive, skip it
                ServerMigrationLogger.ROOT_LOGGER.infof("Default JMS Connection Factory not found, skipping its configuration due to non interactive mode");
                return;
            }
            // retrieve the names of configured datasources
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
                    addOp.get("jms-connection-factory").set(jmsConnectionFactoryJndiName);
                    ServerMigrationLogger.ROOT_LOGGER.infof("JMS Connection Factory %s set as the Java EE Default JMS Connection Factory.", choice);
                }
                @Override
                public void onError() throws Exception {
                }
                @Override
                public void onOther(String otherChoice) throws Exception {
                    addOp.get("jms-connection-factory").set(otherChoice);
                    ServerMigrationLogger.ROOT_LOGGER.infof("Java EE Default JMS Connection Factory configured with JNDI name %s.", otherChoice);
                }
            };
            new UserChoiceWithOtherOption(context.getServerMigrationContext().getConsoleWrapper(), factoryNames, "Unconfigured JMS Connection Factory, I want to enter the JNDI name...", "Please select Java EE's Default JMS Connection Factory: ", resultHandler).execute();
        }
    }

    private void setupDefaultDatasource(final ModelNode addOp, WildFly10StandaloneServer server, ServerMigrationTaskContext context) throws Exception {
        // if the subsystem config defines expected default resource then use it
        final ModelNode subsystemConfig = server.getSubsystem(WildFly10SubsystemNames.DATASOURCES);
        if (subsystemConfig == null) {
            return;
        }
        if (subsystemConfig.hasDefined(DATA_SOURCE, DEFAULT_DATASOURCE_NAME)) {
            // eap 6.4 default standalone config's example datasource found, use it
            final ModelNode defaultDatasource = subsystemConfig.get(DATA_SOURCE, DEFAULT_DATASOURCE_NAME);
            final String defaultDatasourceJndiName = defaultDatasource.get(DATA_SOURCE_JNDI_NAME).asString();
            addOp.get("datasource").set(defaultDatasourceJndiName);
            ServerMigrationLogger.ROOT_LOGGER.infof("Default datasource %s found and set as the Java EE Default Datasource.", DEFAULT_DATASOURCE_NAME);
        } else {
            if (context.getServerMigrationContext().isInteractive()) {
                ServerMigrationLogger.ROOT_LOGGER.infof("Default datasource not found.");
            } else {
                // not interactive, skip it
                ServerMigrationLogger.ROOT_LOGGER.infof("Default datasource not found, skipping its configuration due to non interactive mode");
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
                    final String jndiName = subsystemConfig.get(DATA_SOURCE, choice).get(DATA_SOURCE_JNDI_NAME).asString();
                    addOp.get("datasource").set(jndiName);
                    ServerMigrationLogger.ROOT_LOGGER.infof("Datasource %s set as the Java EE Default Datasource.", choice);
                }
                @Override
                public void onError() throws Exception {
                }
                @Override
                public void onOther(String otherChoice) throws Exception {
                    addOp.get("datasource").set(otherChoice);
                    ServerMigrationLogger.ROOT_LOGGER.infof("Java EE Default Datasource configured with JNDI name %s.", otherChoice);
                }
            };
            new UserChoiceWithOtherOption(context.getServerMigrationContext().getConsoleWrapper(), dataSourceNames, "Unconfigured data source, I want to enter the JNDI name...", "Please select Java EE's Default Datasource: ", resultHandler).execute();
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
