/*
 * Copyright 2018 Red Hat, Inc.
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
package org.jboss.migration.eap.task.subsystem.web;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.subsystem.ExtensionNames;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;
import org.jboss.migration.wfly10.config.task.subsystem.web.MigrateWebSubsystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ENABLED_CIPHER_SUITES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ENABLED_PROTOCOLS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODULE;
import static org.jboss.as.controller.operations.common.Util.*;

/**
 * A task which, besides migrating jboss web subsystem configs, also replaces usage of legacy security reals by undertow, with elytron ssl contexts.
 * @author emmartins
 */
public class EAP7_1MigrateWebSubsystem<S> extends MigrateWebSubsystem<S> {

    public EAP7_1MigrateWebSubsystem() {
        this(new EAP7_1MigrateWebSubsystemSubtaskBuilder<>());
    }

    protected EAP7_1MigrateWebSubsystem(EAP7_1MigrateWebSubsystemSubtaskBuilder<S> subtaskBuilder) {
        super(subtaskBuilder);
    }

    protected static class EAP7_1MigrateWebSubsystemSubtaskBuilder<S> extends MigrateWebSubsystemSubtaskBuilder<S> {

        private static final String CONNECTOR = "connector";
        private static final String CONFIGURATION = "configuration";
        private static final String SSL = "ssl";
        private static final String SERVER = "server";
        private static final String DEFAULT_SERVER = "default-server";
        private static final String HTTPS_LISTENER = "https-listener";
        private static final String SSL_CONTEXT = "ssl-context";
        private static final String SECURITY_REALM = "security-realm";
        private static final String CA_CERTIFICATE_FILE = "ca-certificate-file";
        private static final String CA_CERTIFICATE_PASSWORD = "ca-certificate-password";
        private static final String CERTIFICATE_KEY_FILE = "certificate-key-file";
        private static final String CIPHER_SUITE = "cipher-suite";
        private static final String KEY_ALIAS = "key-alias";
        private static final String KEYSTORE_TYPE = "keystore-type";
        private static final String PASSWORD = "password";
        private static final String PROTOCOL = "protocol";
        private static final String SESSION_CACHE_SIZE = "session-cache-size";
        private static final String SESSION_TIMEOUT = "session-timeout";
        private static final String TRUSTSTORE_TYPE = "truststore-type";
        private static final String VERIFY_CLIENT = "verify-client";
        private static final String SSL_SESSION_CACHE_SIZE = "ssl-session-cache-size";
        private static final String SSL_SESSION_TIMEOUT = "ssl-session-timeout";
        private static final String PATH = "path";
        private static final String TYPE = "type";
        private static final String CREDENTIAL_REFERENCE = "credential-reference";
        private static final String MAXIMUM_SESSION_CACHE_SIZE = "maximum-session-cache-size";
        private static final String KEY_MANAGER = "key-manager";
        private static final String KEY_STORE = "key-store";
        private static final String SERVER_SSL_CONTEXT = "server-ssl-context";
        private static final String CLEAR_TEXT = "clear-text";
        private static final String TRUST_MANAGER = "trust-manager";
        private static final String CIPHER_SUITE_FILTER = "cipher-suite-filter";
        private static final String WANT_CLIENT_AUTH = "want-client-auth";
        private static final String NEED_CLIENT_AUTH = "need-client-auth";
        private static final String ALIAS_FILTER = "alias-filter";
        private static final String PROTOCOLS = "protocols";

        @Override
        protected ServerMigrationTaskResult migrateConfiguration(SubsystemResource webSubsystemResource, TaskContext taskContext) {
            // collect jboss web ssl connector(s) data
            final ModelNode webSubsystemConfig = webSubsystemResource.getResourceConfiguration();
            final Map<String, ModelNode> sslConfigs = new HashMap<>();
            if (webSubsystemConfig.hasDefined(CONNECTOR)) {
                for (String connectorName : webSubsystemConfig.get(CONNECTOR).keys()) {
                    final ModelNode sslConfig = webSubsystemConfig.get(CONNECTOR, connectorName, CONFIGURATION, SSL);
                    if (sslConfig.isDefined()) {
                        // save ssl config data
                        sslConfigs.put(connectorName, sslConfig.clone());
                    }
                }
            }
            // do standard subsystem config migration
            final ServerMigrationTaskResult taskResult = super.migrateConfiguration(webSubsystemResource, taskContext);
            // replace any undertow usage of legacy security realms, with elytron ssl context(s)
            if (taskResult.getStatus() == ServerMigrationTaskResult.Status.SUCCESS && !sslConfigs.isEmpty()) {
                final SubsystemResource undertowSubsystemResource = webSubsystemResource.getParentResource().getSubsystemResource(SubsystemNames.UNDERTOW);
                final ModelNode undertowDefaultServerConfig = undertowSubsystemResource.getResourceConfiguration().get(SERVER, DEFAULT_SERVER);
                for (Map.Entry<String, ModelNode> entry : sslConfigs.entrySet()) {
                    updateSslConfig(entry.getKey(), entry.getValue(), undertowSubsystemResource, undertowDefaultServerConfig, taskContext);
                }
            }
            return taskResult;
        }

        protected void updateSslConfig(String connector, ModelNode sslConfig, SubsystemResource undertowSubsystemResource, ModelNode undertowDefaultServerConfig, TaskContext taskContext) {

            final ModelNode httpsListener = undertowDefaultServerConfig.get(HTTPS_LISTENER, connector);

            if (httpsListener.isDefined() && httpsListener.hasDefined(SECURITY_REALM)) {

                // read all the info from the SSL config
                ModelNode keyAlias = sslConfig.get(KEY_ALIAS);
                ModelNode password = sslConfig.get(PASSWORD);
                ModelNode certificateKeyFile = sslConfig.get(CERTIFICATE_KEY_FILE);
                ModelNode cipherSuite = sslConfig.get(CIPHER_SUITE);
                ModelNode protocol = sslConfig.get(PROTOCOL);
                ModelNode verifyClient = sslConfig.get(VERIFY_CLIENT);
                ModelNode caCertificateFile = sslConfig.get(CA_CERTIFICATE_FILE);
                ModelNode caCertificatePassword = sslConfig.get(CA_CERTIFICATE_PASSWORD);
                ModelNode trustStoreType = sslConfig.get(TRUSTSTORE_TYPE);
                ModelNode keystoreType = sslConfig.get(KEYSTORE_TYPE);
                ModelNode sessionCacheSize = sslConfig.get(SESSION_CACHE_SIZE);
                ModelNode sessionTimeout = sslConfig.get(SESSION_TIMEOUT);

                final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
                final ManageableServerConfiguration serverConfiguration = undertowSubsystemResource.getServerConfiguration();

                // add elytron extension and subsystem if missing
                if (!serverConfiguration.hasExtensionResource(ExtensionNames.ELYTRON)) {
                    final ModelNode op = Util.createAddOperation(serverConfiguration.getExtensionResourcePathAddress(ExtensionNames.ELYTRON));
                    op.get(MODULE).set(ExtensionNames.ELYTRON);
                    compositeOperationBuilder.addStep(op);
                }
                final PathAddress subsystemAddress = undertowSubsystemResource.getParentResource().getSubsystemResourcePathAddress(SubsystemNames.ELYTRON);
                if (!undertowSubsystemResource.getParentResource().hasSubsystemResource(SubsystemNames.ELYTRON)) {
                    final ModelNode op = Util.createAddOperation(subsystemAddress);
                    compositeOperationBuilder.addStep(op);
                }

                final String resourceNamePrefix = "jbossweb-migrated-connector-"+connector;

                // add key store
                final String keystoreName = connector + "-" + KEY_STORE;
                final PathAddress keystoreAddress = subsystemAddress.append(KEY_STORE, keystoreName);
                final ModelNode keystoreAddOp = createAddOperation(keystoreAddress);
                if (certificateKeyFile.isDefined()) {
                    keystoreAddOp.get(PATH).set(certificateKeyFile);
                } else {
                    // set the default jboss web value
                    keystoreAddOp.get(PATH).set("${user.home}/.keystore");
                }
                final ModelNode keystoreCredentialReference = new ModelNode();
                keystoreCredentialReference.get(CLEAR_TEXT).set(password);
                keystoreAddOp.get(CREDENTIAL_REFERENCE).set(keystoreCredentialReference);
                if (keystoreType.isDefined()) {
                    keystoreAddOp.get(TYPE).set(keystoreType);
                } else {
                    // set the default jboss web value
                    keystoreAddOp.get(TYPE).set("JKS");
                }
                compositeOperationBuilder.addStep(keystoreAddOp);

                // add key manager
                final String keyManagerName = resourceNamePrefix + "-" + KEY_MANAGER;
                final PathAddress keyManagerAddress = subsystemAddress.append(KEY_MANAGER, keyManagerName);
                final ModelNode keyManagerAddOp = createAddOperation(keyManagerAddress);
                keyManagerAddOp.get(KEY_STORE).set(keystoreName);
                if (keyAlias.isDefined()) {
                    keyManagerAddOp.get(ALIAS_FILTER).set(keyAlias);
                }
                keyManagerAddOp.get(CREDENTIAL_REFERENCE).set(keystoreCredentialReference.clone());
                compositeOperationBuilder.addStep(keyManagerAddOp);

                // add ssl context
                final String serverSslContextName = resourceNamePrefix + "-" + SERVER_SSL_CONTEXT;
                final PathAddress serverSslContextAddress = subsystemAddress.append(SERVER_SSL_CONTEXT, serverSslContextName);
                final ModelNode serverSslContextAddOp = createAddOperation(serverSslContextAddress);
                serverSslContextAddOp.get(KEY_MANAGER).set(keyManagerName);
                if (sessionCacheSize.isDefined()) {
                    serverSslContextAddOp.get(MAXIMUM_SESSION_CACHE_SIZE).set(sessionCacheSize);
                }
                if (sessionTimeout.isDefined()) {
                    serverSslContextAddOp.get(SESSION_TIMEOUT).set(sessionTimeout);
                }
                if (protocol.isDefined()) {
                    final Set<String> protocols = new HashSet<>();
                    switch (protocol.asString()) {
                        case "ALL":
                            break;
                        case "SSLv2Hello":
                            break;
                        case "SSLv2+SSLv3":
                            protocols.add("SSLv2");
                            protocols.add("SSLv3");
                            break;
                        default:
                            protocols.add(protocol.asString());
                    }
                    if (!protocols.isEmpty()) {
                        ModelNode modelNode = new ModelNode().setEmptyList();
                        for (String s : protocols) {
                            modelNode.add(s);
                        }
                        serverSslContextAddOp.get(PROTOCOLS).set(modelNode);
                    }
                }
                if (cipherSuite.isDefined()) {
                    serverSslContextAddOp.get(CIPHER_SUITE_FILTER).set(cipherSuite);
                }
                if (verifyClient.isDefined()) {
                    switch (verifyClient.asString()) {
                        case "optionalNoCA":
                        case "optional": {
                            serverSslContextAddOp.get(WANT_CLIENT_AUTH).set(true);
                            break;
                        }
                        case "true":
                        case "require": {
                            serverSslContextAddOp.get(NEED_CLIENT_AUTH).set(true);
                            break;
                        }
                        case "none":
                        case "false": {
                            break;
                        }
                    }
                }
                compositeOperationBuilder.addStep(serverSslContextAddOp);

                if (caCertificateFile.isDefined()) {
                    // add trust store
                    final String trustStoreName = resourceNamePrefix + "-trust-store";
                    final PathAddress trustStoreAddress = subsystemAddress.append(KEY_STORE, trustStoreName);
                    final ModelNode trustStoreAddOp = createAddOperation(trustStoreAddress);
                    trustStoreAddOp.get(PATH).set(caCertificateFile);
                    final ModelNode trustStoreCredentialReference;
                    if (!caCertificatePassword.isDefined()) {
                        // if not defined use keystore password
                        trustStoreCredentialReference = keystoreCredentialReference.clone();
                    } else {
                        trustStoreCredentialReference = new ModelNode();
                        trustStoreCredentialReference.get(CLEAR_TEXT).set(caCertificatePassword);
                    }
                    trustStoreAddOp.get(CREDENTIAL_REFERENCE).set(trustStoreCredentialReference);
                    if (trustStoreType.isDefined()) {
                        trustStoreAddOp.get(TYPE).set(trustStoreType);
                    } else {
                        // set the default jboss web value
                        trustStoreAddOp.get(TYPE).set("JKS");
                    }
                    compositeOperationBuilder.addStep(trustStoreAddOp);
                    // add trust manager
                    final String trustManagerName = resourceNamePrefix + "-" + TRUST_MANAGER;
                    final PathAddress trustManagerAddress = subsystemAddress.append(TRUST_MANAGER, trustManagerName);
                    final ModelNode trustManagerAddOp = createAddOperation(trustManagerAddress);
                    trustManagerAddOp.get(KEY_STORE).set(trustStoreName);
                    trustManagerAddOp.get(CREDENTIAL_REFERENCE).set(trustStoreCredentialReference.clone());
                    compositeOperationBuilder.addStep(trustManagerAddOp);
                    // add to ssl context
                    serverSslContextAddOp.get(TRUST_MANAGER).set(trustManagerName);
                }

                // update https listener config
                final PathAddress undertowConnectorAddress = undertowSubsystemResource.getResourcePathAddress().append(SERVER, DEFAULT_SERVER).append(HTTPS_LISTENER, connector);
                compositeOperationBuilder.addStep(getUndefineAttributeOperation(undertowConnectorAddress, SECURITY_REALM));
                compositeOperationBuilder.addStep(getUndefineAttributeOperation(undertowConnectorAddress, VERIFY_CLIENT));
                compositeOperationBuilder.addStep(getUndefineAttributeOperation(undertowConnectorAddress, ENABLED_CIPHER_SUITES));
                compositeOperationBuilder.addStep(getUndefineAttributeOperation(undertowConnectorAddress, ENABLED_PROTOCOLS));
                compositeOperationBuilder.addStep(getUndefineAttributeOperation(undertowConnectorAddress, SSL_SESSION_CACHE_SIZE));
                compositeOperationBuilder.addStep(getUndefineAttributeOperation(undertowConnectorAddress, SSL_SESSION_TIMEOUT));
                compositeOperationBuilder.addStep(getWriteAttributeOperation(undertowConnectorAddress, SSL_CONTEXT, serverSslContextName));
                serverConfiguration.executeManagementOperation(compositeOperationBuilder.build().getOperation());
            }
        }
    }
}
