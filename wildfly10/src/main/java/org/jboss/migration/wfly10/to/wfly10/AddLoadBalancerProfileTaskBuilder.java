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

package org.jboss.migration.wfly10.to.wfly10;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupResource;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.profile.AddProfileTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceBuildParameters;
import org.jboss.migration.wfly10.config.task.management.subsystem.AddSubsystemConfigurationSubtaskBuilder;
import org.jboss.migration.wfly10.config.task.management.subsystem.AddSubsystemConfigurationTaskBuilder;
import org.jboss.migration.wfly10.config.task.subsystem.ExtensionNames;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;

/**
 * @author emmartins
 */
public class AddLoadBalancerProfileTaskBuilder<S> extends AddProfileTaskBuilder<S> {

    public AddLoadBalancerProfileTaskBuilder() {
        super("load-balancer");
        subtasks.subtask(new AddLoadBalancerSocketBindingsGroup<>());
        subsystemSubtasks.subtask(new AddSubsystemConfigurationTaskBuilder<S>(ExtensionNames.IO, new AddIOSubsystemConfig<>()));
        subsystemSubtasks.subtask(new AddSubsystemConfigurationTaskBuilder<S>(ExtensionNames.UNDERTOW, new AddUndertowSubsystemConfig<>()));
        subsystemSubtasks.subtask(new AddSubsystemConfigurationTaskBuilder<S>(ExtensionNames.LOGGING, new AddLoggingSubsystemConfig<>()));
    }

    public static class AddLoadBalancerSocketBindingsGroup<S> extends ServerConfigurationLeafTask.Builder<S> {

        private static final String SOCKET_BINDING_GROUP_NAME = "load-balancer-sockets";

        protected AddLoadBalancerSocketBindingsGroup() {
            name("add-"+SOCKET_BINDING_GROUP_NAME);
            skipPolicyBuilder(buildParameters -> context -> buildParameters.getServerConfiguration().getSocketBindingGroupResource(SOCKET_BINDING_GROUP_NAME) != null);
            runBuilder(params -> context -> {
                final ManageableServerConfiguration configuration = params.getServerConfiguration();
                final PathAddress socketBindingGroupPathAddress = configuration.getSocketBindingGroupResourcePathAddress(SOCKET_BINDING_GROUP_NAME);
                final ModelNode socketBindingGroupOp = Util.createAddOperation(socketBindingGroupPathAddress);
                socketBindingGroupOp.get("default-interface").set("public");
                configuration.executeManagementOperation(socketBindingGroupOp);
                // retreive socket binding group
                final SocketBindingGroupResource socketBindingGroupResource = configuration.getSocketBindingGroupResource(SOCKET_BINDING_GROUP_NAME);
                // add http
                final PathAddress httpPathAddress = socketBindingGroupResource.getSocketBindingResourcePathAddress("http");
                final ModelNode httpOp = Util.createAddOperation(httpPathAddress);
                httpOp.get("port").set(new ValueExpression("${jboss.http.port:8080}"));
                configuration.executeManagementOperation(httpOp);
                // add https
                final PathAddress httpsPathAddress = socketBindingGroupResource.getSocketBindingResourcePathAddress("https");
                final ModelNode httpsOp = Util.createAddOperation(httpsPathAddress);
                httpsOp.get("port").set(new ValueExpression("${jboss.https.port:8443}"));
                configuration.executeManagementOperation(httpsOp);
                // add mcmp-management
                final PathAddress mcmpManagementPathAddress = socketBindingGroupResource.getSocketBindingResourcePathAddress("mcmp-management");
                final ModelNode mcmpManagementOp = Util.createAddOperation(mcmpManagementPathAddress);
                mcmpManagementOp.get("interface").set("private");
                mcmpManagementOp.get("port").set(new ValueExpression("${jboss.mcmp.port:8090}"));
                configuration.executeManagementOperation(mcmpManagementOp);
                // add modcluster
                final PathAddress modclusterPathAddress = socketBindingGroupResource.getSocketBindingResourcePathAddress("modcluster");
                final ModelNode modclusterOp = Util.createAddOperation(modclusterPathAddress);
                modclusterOp.get("interface").set("private");
                modclusterOp.get("multicast-address").set(new ValueExpression("${jboss.modcluster.multicast.address:224.0.1.105}"));
                modclusterOp.get("multicast-port").set(23364);
                configuration.executeManagementOperation(modclusterOp);
                return ServerMigrationTaskResult.SUCCESS;

            });
        }
    }

    public static class AddIOSubsystemConfig<S> extends AddSubsystemConfigurationSubtaskBuilder<S> {
        protected AddIOSubsystemConfig() {
            super(SubsystemNames.IO);
        }
        @Override
        protected void addConfiguration(ResourceBuildParameters<S, SubsystemConfiguration.Parent> params, ServerMigrationTaskName taskName, TaskContext taskContext) {
            // adds subsystem
            super.addConfiguration(params, taskName, taskContext);
            final ManageableServerConfiguration configuration = params.getServerConfiguration();
            final PathAddress subsystemPathAddress = params.getResource().getSubsystemConfigurationPathAddress(getSubsystem());
            // add default worker
            final PathAddress workerDefaultPathAddress = subsystemPathAddress.append("worker","default");
            configuration.executeManagementOperation(Util.createAddOperation(workerDefaultPathAddress));
            // add default buffer pool
            final PathAddress bufferPoolDefaultPathAddress = subsystemPathAddress.append("buffer-pool","default");
            configuration.executeManagementOperation(Util.createAddOperation(bufferPoolDefaultPathAddress));
        }
    }

    public static class AddUndertowSubsystemConfig<S> extends AddSubsystemConfigurationSubtaskBuilder<S> {
        protected AddUndertowSubsystemConfig() {
            super(SubsystemNames.UNDERTOW);
        }
        @Override
        protected void addConfiguration(ResourceBuildParameters<S, SubsystemConfiguration.Parent> params, ServerMigrationTaskName taskName, TaskContext taskContext) {
            // adds subsystem
            super.addConfiguration(params, taskName, taskContext);
            final ManageableServerConfiguration configuration = params.getServerConfiguration();
            final PathAddress subsystemPathAddress = params.getResource().getSubsystemConfigurationPathAddress(getSubsystem());
            // add default servlet container
            final PathAddress defaultServletContainerPathAddress = subsystemPathAddress.append("servlet-container","default");
            configuration.executeManagementOperation(Util.createAddOperation(defaultServletContainerPathAddress));
            // add configuration filter
            final PathAddress configurationFilterPathAddress = subsystemPathAddress.append("configuration","filter");
            configuration.executeManagementOperation(Util.createAddOperation(configurationFilterPathAddress));
            // add modcluster load-balancer
            final PathAddress modClusterLoadBalancerPathAddress = configurationFilterPathAddress.append("mod-cluster","load-balancer");
            final ModelNode modClusterLoadBalancerOp = Util.createAddOperation(modClusterLoadBalancerPathAddress);
            modClusterLoadBalancerOp.get("management-socket-binding").set("mcmp-management");
            modClusterLoadBalancerOp.get("advertise-socket-binding").set("modcluster");
            modClusterLoadBalancerOp.get("enable-http2").set(true);
            modClusterLoadBalancerOp.get("max-retries").set(3);
            configuration.executeManagementOperation(modClusterLoadBalancerOp);
            // add configuration handler
            final PathAddress configurationHandlerPathAddress = subsystemPathAddress.append("configuration","handler");
            configuration.executeManagementOperation(Util.createAddOperation(configurationHandlerPathAddress));
            // add default server
            final PathAddress serverDefaultServerPathAddress = subsystemPathAddress.append("server","default-server");
            configuration.executeManagementOperation(Util.createAddOperation(serverDefaultServerPathAddress));
            // add default host
            final PathAddress hostDefaultHostPathAddress = serverDefaultServerPathAddress.append("host","default-host");
            final ModelNode hostDefaultHostOp = Util.createAddOperation(hostDefaultHostPathAddress);
            hostDefaultHostOp.get("alias").setEmptyList().add("localhost");
            configuration.executeManagementOperation(hostDefaultHostOp);
            // add default host filter ref
            final PathAddress filterRefLoadBalancerPathAddress = hostDefaultHostPathAddress.append("filter-ref","load-balancer");
            configuration.executeManagementOperation(Util.createAddOperation(filterRefLoadBalancerPathAddress));
            // add default http-listener
            final PathAddress httpListenerDefaultPathAddress = serverDefaultServerPathAddress.append("http-listener","default");
            final ModelNode httpListenerDefaultOp = Util.createAddOperation(httpListenerDefaultPathAddress);
            httpListenerDefaultOp.get("socket-binding").set("http");
            httpListenerDefaultOp.get("redirect-socket").set("https");
            httpListenerDefaultOp.get("enable-http2").set(true);
            configuration.executeManagementOperation(httpListenerDefaultOp);
            // add management http-listener
            final PathAddress httpListenerManagementPathAddress = serverDefaultServerPathAddress.append("http-listener","management");
            final ModelNode httpListenerManagementOp = Util.createAddOperation(httpListenerManagementPathAddress);
            httpListenerManagementOp.get("socket-binding").set("mcmp-management");
            httpListenerManagementOp.get("enable-http2").set(true);
            configuration.executeManagementOperation(httpListenerManagementOp);
            // add buffer cache
            final PathAddress bufferCacheDefaultPathAddress = subsystemPathAddress.append("buffer-cache","default");
            configuration.executeManagementOperation(Util.createAddOperation(bufferCacheDefaultPathAddress));
        }
    }

    public static class AddLoggingSubsystemConfig<S> extends AddSubsystemConfigurationSubtaskBuilder<S> {
        protected AddLoggingSubsystemConfig() {
            super(SubsystemNames.LOGGING);
        }
        @Override
        protected void addConfiguration(ResourceBuildParameters<S, SubsystemConfiguration.Parent> params, ServerMigrationTaskName taskName, TaskContext taskContext) {
            // adds subsystem
            super.addConfiguration(params, taskName, taskContext);
            final ManageableServerConfiguration configuration = params.getServerConfiguration();
            final PathAddress subsystemPathAddress = params.getResource().getSubsystemConfigurationPathAddress(getSubsystem());
            // add pattern formatter PATTERN
            final PathAddress patternFormatterPATTERNPathAddress = subsystemPathAddress.append("pattern-formatter","PATTERN");
            final ModelNode patternFormatterPATTERNOp = Util.createAddOperation(patternFormatterPATTERNPathAddress);
            patternFormatterPATTERNOp.get("pattern").set("%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
            configuration.executeManagementOperation(patternFormatterPATTERNOp);
            // add pattern formatter COLOR-PATTERN
            final PathAddress patternFormatterCOLORPATTERNPathAddress = subsystemPathAddress.append("pattern-formatter","COLOR-PATTERN");
            final ModelNode patternFormatterCOLORPATTERNOp = Util.createAddOperation(patternFormatterCOLORPATTERNPathAddress);
            patternFormatterCOLORPATTERNOp.get("pattern").set("%K{level}%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
            configuration.executeManagementOperation(patternFormatterCOLORPATTERNOp);
            // add console handler CONSOLE
            final PathAddress consoleHandlerCONSOLEPathAddress = subsystemPathAddress.append("console-handler","CONSOLE");
            final ModelNode consoleHandlerCONSOLEOp = Util.createAddOperation(consoleHandlerCONSOLEPathAddress);
            consoleHandlerCONSOLEOp.get("level").set("INFO");
            configuration.executeManagementOperation(consoleHandlerCONSOLEOp);
            // add periodic-rotating-file-handler FILE
            final PathAddress periodicRotatingFileHandlerFILEPathAddress = subsystemPathAddress.append("periodic-rotating-file-handler","FILE");
            final ModelNode periodicRotatingFileHandlerFILEOp = Util.createAddOperation(periodicRotatingFileHandlerFILEPathAddress);
            periodicRotatingFileHandlerFILEOp.get("autoflush").set(true);
            periodicRotatingFileHandlerFILEOp.get("append").set(true);
            final ModelNode file = new ModelNode();
            file.get("relative-to").set("jboss.server.log.dir");
            file.get("path").set("server.log");
            periodicRotatingFileHandlerFILEOp.get("file").set(file);
            periodicRotatingFileHandlerFILEOp.get("suffix").set(".yyyy-MM-dd");
            periodicRotatingFileHandlerFILEOp.get("named-formatter").set("PATTERN");
            configuration.executeManagementOperation(periodicRotatingFileHandlerFILEOp);
            // add com.arjuna logger
            final PathAddress comArjunaLoggerPathAddress = subsystemPathAddress.append("logger","com.arjuna");
            final ModelNode comArjunaLoggerOp = Util.createAddOperation(comArjunaLoggerPathAddress);
            comArjunaLoggerOp.get("level").set("WARN");
            configuration.executeManagementOperation(comArjunaLoggerOp);
            // add org.jboss.as.config logger
            final PathAddress orgJbossAsConfigLoggerPathAddress = subsystemPathAddress.append("logger","org.jboss.as.config");
            final ModelNode orgJbossAsConfigLoggerOp = Util.createAddOperation(orgJbossAsConfigLoggerPathAddress);
            orgJbossAsConfigLoggerOp.get("level").set("DEBUG");
            configuration.executeManagementOperation(orgJbossAsConfigLoggerOp);
            // add sun.rmi logger
            final PathAddress sunRmiLoggerPathAddress = subsystemPathAddress.append("logger","sun.rmi");
            final ModelNode sunRmiLoggerOp = Util.createAddOperation(sunRmiLoggerPathAddress);
            sunRmiLoggerOp.get("level").set("WARN");
            configuration.executeManagementOperation(sunRmiLoggerOp);
            // add root-logger
            final PathAddress rootLoggerROOTPathAddress = subsystemPathAddress.append("root-logger","ROOT");
            final ModelNode rootLoggerROOTOp = Util.createAddOperation(rootLoggerROOTPathAddress);
            rootLoggerROOTOp.get("level").set("INFO");
            rootLoggerROOTOp.get("handlers").setEmptyList().add("CONSOLE").add("FILE");
            configuration.executeManagementOperation(rootLoggerROOTOp);
        }
    }
}
