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
package org.wildfly.migration.wfly10.subsystem;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.console.UserChoiceWithOtherOption;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author emmartins
 */
public class EEWildFly10Extension extends WildFly10Extension {

    public static final EEWildFly10Extension INSTANCE = new EEWildFly10Extension();

    private EEWildFly10Extension() {
        super("org.jboss.as.ee");
        subsystems.add(new EEWildFly10Subsystem(this));
    }

    private static class EEWildFly10Subsystem extends BasicWildFly10Subsystem {

        private static final String DEFAULT_CONTEXT_SERVICE_JNDI_NAME = "java:jboss/ee/concurrency/context/default";
        private static final String DEFAULT_MANAGED_THREAD_FACTORY_JNDI_NAME = "java:jboss/ee/concurrency/factory/default";
        private static final String DEFAULT_MANAGED_EXECUTOR_SERVICE_JNDI_NAME = "java:jboss/ee/concurrency/executor/default";
        private static final String DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_JNDI_NAME = "java:jboss/ee/concurrency/scheduler/default";

        private static final String SERVER = "server";
        private static final String CONNECTION_FACTORY = "connection-factory";
        private static final String POOLED_CONNECTION_FACTORY = "pooled-connection-factory";
        private static final String DATA_SOURCE = "data-source";
        private static final String DATA_SOURCE_JNDI_NAME = "jndi-name";
        // TODO move to migration context properties
        private static final String DEFAULT_DATASOURCE_NAME = "ExampleDS";
        private static final String DEFAULT_JMS_CONNECTION_FACTORY_NAME = "hornetq-ra";
        private static final String DEFAULT_JMS_SERVER_NAME = "default";


        private EEWildFly10Subsystem(EEWildFly10Extension extension) {
            super("ee", extension);
        }

        @Override
        public void migrate(WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
            super.migrate(server, context);
            migrateConfig(server.getSubsystem(getName()), server, context);
        }

        protected void migrateConfig(ModelNode config, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
            // add Java EE 7's EE Concurrency Utilities and Default JNDI Bindings

            if (config == null) {
                return;
            }
            setupEEConcurrencyUtilities(config, server, context);
            setupDefaultBindings(config, server, context);
        }

        private void setupEEConcurrencyUtilities(ModelNode config, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
            final PathElement subsystemPathElement = pathElement(SUBSYSTEM, getName());
            // add default context service
            /*
            "context-service" => {"default" => {
                    "jndi-name" => "java:jboss/ee/concurrency/context/default",
                    "use-transaction-setup-provider" => true
                }},
             */
            final PathAddress defaultContextServicePathAddress = pathAddress(subsystemPathElement, pathElement("context-service", "default"));
            final ModelNode defaultContextServiceAddOp = Util.createEmptyOperation(ADD, defaultContextServicePathAddress);
            defaultContextServiceAddOp.get("jndi-name").set(DEFAULT_CONTEXT_SERVICE_JNDI_NAME);
            defaultContextServiceAddOp.get("use-transaction-setup-provider").set(true);
            server.executeManagementOperation(defaultContextServiceAddOp);
            ServerMigrationLogger.ROOT_LOGGER.debugf("Default ContextService added to subsystem EE configuration.");
            // add default managed thread factory
            /*
            "managed-thread-factory" => {"default" => {
                    "context-service" => "default",
                    "jndi-name" => "java:jboss/ee/concurrency/factory/default",
                    "priority" => 5
                }}
             */
            final PathAddress defaultManagedThreadFactoryPathAddress = pathAddress(subsystemPathElement, pathElement("managed-thread-factory", "default"));
            final ModelNode defaultManagedThreadFactoryAddOp = Util.createEmptyOperation(ADD, defaultManagedThreadFactoryPathAddress);
            defaultManagedThreadFactoryAddOp.get("jndi-name").set(DEFAULT_MANAGED_THREAD_FACTORY_JNDI_NAME);
            defaultManagedThreadFactoryAddOp.get("context-service").set("default");
            defaultManagedThreadFactoryAddOp.get("priority").set(5);
            server.executeManagementOperation(defaultManagedThreadFactoryAddOp);
            ServerMigrationLogger.ROOT_LOGGER.debugf("Default ManagedThreadFactory added to subsystem EE configuration.");
            // add default managed executor service
            /*
            "managed-executor-service" => {"default" => {
                    "context-service" => "default",
                    "core-threads" => undefined,
                    "hung-task-threshold" => 60000L,
                    "jndi-name" => "java:jboss/ee/concurrency/executor/default",
                    "keepalive-time" => 5000L,
                    "long-running-tasks" => false,
                    "max-threads" => undefined,
                    "queue-length" => undefined,
                    "reject-policy" => "ABORT",
                    "thread-factory" => undefined
                }}
             */
            final PathAddress defaultManagedExecutorServicePathAddress = pathAddress(subsystemPathElement, pathElement("managed-executor-service", "default"));
            final ModelNode defaultManagedExecutorServiceAddOp = Util.createEmptyOperation(ADD, defaultManagedExecutorServicePathAddress);
            defaultManagedExecutorServiceAddOp.get("jndi-name").set(DEFAULT_MANAGED_EXECUTOR_SERVICE_JNDI_NAME);
            defaultManagedExecutorServiceAddOp.get("context-service").set("default");
            defaultManagedExecutorServiceAddOp.get("hung-task-threshold").set(60000L);
            defaultManagedExecutorServiceAddOp.get("keepalive-time").set(5000L);
            defaultManagedExecutorServiceAddOp.get("long-running-tasks").set(false);
            defaultManagedExecutorServiceAddOp.get("reject-policy").set("ABORT");
            server.executeManagementOperation(defaultManagedExecutorServiceAddOp);
            ServerMigrationLogger.ROOT_LOGGER.debugf("Default ManagedExecutorService added to subsystem EE configuration.");
            // add default managed scheduled executor service
            /*
            "managed-scheduled-executor-service" => {"default" => {
                    "context-service" => "default",
                    "core-threads" => undefined,
                    "hung-task-threshold" => 60000L,
                    "jndi-name" => "java:jboss/ee/concurrency/scheduler/default",
                    "keepalive-time" => 3000L,
                    "long-running-tasks" => false,
                    "reject-policy" => "ABORT",
                    "thread-factory" => undefined
                }}
             */
            final PathAddress defaultManagedScheduledExecutorServicePathAddress = pathAddress(subsystemPathElement, pathElement("managed-scheduled-executor-service", "default"));
            final ModelNode defaultManagedScheduledExecutorServiceAddOp = Util.createEmptyOperation(ADD, defaultManagedScheduledExecutorServicePathAddress);
            defaultManagedScheduledExecutorServiceAddOp.get("jndi-name").set(DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_JNDI_NAME);
            defaultManagedScheduledExecutorServiceAddOp.get("context-service").set("default");
            defaultManagedScheduledExecutorServiceAddOp.get("hung-task-threshold").set(60000L);
            defaultManagedScheduledExecutorServiceAddOp.get("keepalive-time").set(3000L);
            defaultManagedScheduledExecutorServiceAddOp.get("long-running-tasks").set(false);
            defaultManagedScheduledExecutorServiceAddOp.get("reject-policy").set("ABORT");
            server.executeManagementOperation(defaultManagedScheduledExecutorServiceAddOp);
            ServerMigrationLogger.ROOT_LOGGER.debugf("Default ManagedScheduledExecutorService added to subsystem EE configuration.");
            ServerMigrationLogger.ROOT_LOGGER.infof("EE Concurrency Utilities added.");
        }

        private void setupDefaultBindings(ModelNode config, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
            final PathAddress pathAddress = pathAddress(pathElement(SUBSYSTEM, getName()), pathElement("service", "default-bindings"));
            final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
            addOp.get("context-service").set(DEFAULT_CONTEXT_SERVICE_JNDI_NAME);
            addOp.get("managed-executor-service").set(DEFAULT_MANAGED_EXECUTOR_SERVICE_JNDI_NAME);
            addOp.get("managed-scheduled-executor-service").set(DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_JNDI_NAME);
            addOp.get("managed-thread-factory").set(DEFAULT_MANAGED_THREAD_FACTORY_JNDI_NAME);
            setupDefaultDatasource(addOp, server, context);
            setupDefaultJMSConnectionFactory(addOp, server, context);
            server.executeManagementOperation(addOp);
            ServerMigrationLogger.ROOT_LOGGER.infof("Java EE Default Bindings configured.");
        }

        private void setupDefaultJMSConnectionFactory(final ModelNode addOp, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
            // if the subsystem config defines expected default resource then use it
            final String subsystemName = MessagingActiveMQWildFly10Extension.INSTANCE.getMessagingActiveMQSubsystem().getName();
            final ModelNode subsystemConfig = server.getSubsystem(subsystemName);
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
                ServerMigrationLogger.ROOT_LOGGER.infof("Default JMS Connection Factory not found");
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
                    public void onChoice(String choice) {
                        final ConfiguredJmsConnectionFactory configuredJmsConnectionFactory = factoryNamesMap.get(choice);
                        final ModelNode jmsConnectionFactory = subsystemConfig.get(SERVER, configuredJmsConnectionFactory.serverName, configuredJmsConnectionFactory.factoryType, configuredJmsConnectionFactory.factoryName);
                        final String jmsConnectionFactoryJndiName = jmsConnectionFactory.get("entries").asList().get(0).asString();
                        addOp.get("jms-connection-factory").set(jmsConnectionFactoryJndiName);
                        ServerMigrationLogger.ROOT_LOGGER.infof("JMS Connection Factory %s set as the Java EE Default JMS Connection Factory.", choice);
                    }
                    @Override
                    public void onError() {
                    }
                    @Override
                    public void onOther(String otherChoice) {
                        addOp.get("jms-connection-factory").set(otherChoice);
                        ServerMigrationLogger.ROOT_LOGGER.infof("Java EE Default JMS Connection Factory configured with JNDI name %s.", otherChoice);
                    }
                };
                new UserChoiceWithOtherOption(context.getConsoleWrapper(), factoryNames, "Unconfigured JMS Connection Factory, I want to enter the JNDI name...", "Please select Java EE's Default JMS Connection Factory: ", resultHandler).execute();
            }
        }

        private void setupDefaultDatasource(final ModelNode addOp, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
            // if the subsystem config defines expected default resource then use it
            final String subsystemName = ConnectorWildFly10Extension.INSTANCE.getDatasourceSubsystem().getName();
            final ModelNode subsystemConfig = server.getSubsystem(subsystemName);
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
                ServerMigrationLogger.ROOT_LOGGER.infof("Default datasource not found.");
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
                        final String jndiName = subsystemConfig.get(DATA_SOURCE, choice).get(DATA_SOURCE_JNDI_NAME).asString();
                        addOp.get("datasource").set(jndiName);
                        ServerMigrationLogger.ROOT_LOGGER.infof("Datasource %s set as the Java EE Default Datasource.", choice);
                    }
                    @Override
                    public void onError() {
                    }
                    @Override
                    public void onOther(String otherChoice) {
                        addOp.get("datasource").set(otherChoice);
                        ServerMigrationLogger.ROOT_LOGGER.infof("Java EE Default Datasource configured with JNDI name %s.", otherChoice);
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
}