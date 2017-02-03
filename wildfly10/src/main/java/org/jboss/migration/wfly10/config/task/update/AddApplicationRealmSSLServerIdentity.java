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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.TaskRunnable;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SecurityRealmResource;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationLeafTask;

import java.util.Collection;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Migration of security realms fully compatible with WildFly 10.
 * @author emmartins
 */
public class AddApplicationRealmSSLServerIdentity<S extends JBossServer<S>> extends ServerConfigurationLeafTask.Builder<S> {



    public static final AddApplicationRealmSSLServerIdentity INSTANCE = new AddApplicationRealmSSLServerIdentity();

    private static final String TASK_NAME_NAME = "add-application-realm-ssl-server-identity";
    private static final ServerMigrationTaskName TASK_NAME = new ServerMigrationTaskName.Builder(TASK_NAME_NAME).build();

    private static final String RESOURCE_NAME = "ApplicationRealm";
    public static final String SERVER_IDENTITY_NAME = "ssl";

    public AddApplicationRealmSSLServerIdentity() {
        name("add-application-realm-ssl-server-identity");
        beforeRun(context -> context.getLogger().infof("Security Realm '%s' SSL Server Identity configuration starting...", RESOURCE_NAME));
        run((params, taskName) -> (TaskRunnable) context -> {
            final ManageableServerConfiguration serverConfiguration = params.getServerConfiguration();
            final Collection<SecurityRealmResource> securityRealmResources = serverConfiguration.findResources(SecurityRealmResource.class, RESOURCE_NAME);
            if (securityRealmResources.isEmpty()) {
                context.getLogger().debugf("Security realm %s not defined, skipping task to add SSL server identity", RESOURCE_NAME);
                return ServerMigrationTaskResult.SKIPPED;
            }
            if (securityRealmResources.size() > 1) {
                throw new IllegalStateException("Multiple security realms named " + RESOURCE_NAME + " found!");
            }
            final SecurityRealmResource resource = securityRealmResources.iterator().next();
            final ModelNode resourceConfig = resource.getResourceConfiguration();
            if (resourceConfig.hasDefined(SERVER_IDENTITY, SERVER_IDENTITY_NAME)) {
                context.getLogger().debugf("Security realm %s already includes SSL server identify, skipping task to add it.", RESOURCE_NAME);
                return ServerMigrationTaskResult.SKIPPED;
            }
                /*
    XML:
    <server-identities>
        <ssl>
            <keystore path="application.keystore" relative-to="jboss.server.config.dir" keystore-password="password" alias="server" key-password="password" generate-self-signed-certificate-host="localhost"/>
        </ssl>
    </server-identities>
    RESOURCE:
    "server-identity" => {"ssl" => {
                "alias" => "server",
                "enabled-cipher-suites" => undefined,
                "enabled-protocols" => [
                    "TLSv1",
                    "TLSv1.1",
                    "TLSv1.2"
                ],
                "generate-self-signed-certificate-host" => "localhost",
                "key-password" => "password",
                "keystore-password" => "password",
                "keystore-path" => "application.keystore",
                "keystore-provider" => "JKS",
                "keystore-relative-to" => "jboss.server.config.dir",
                "protocol" => "TLS"
            }}

     */
            final ModelNode addOperation = Util.createAddOperation(resource.getResourcePathAddress().append(SERVER_IDENTITY, SERVER_IDENTITY_NAME));
            addOperation.get(KEYSTORE_PATH).set("application.keystore");
            final String keystoreRelativeTo = serverConfiguration instanceof StandaloneServerConfiguration ? "jboss.server.config.dir" : "jboss.domain.config.dir";
            addOperation.get(KEYSTORE_RELATIVE_TO).set(keystoreRelativeTo);
            addOperation.get(KEYSTORE_PASSWORD).set("password");
            addOperation.get(ALIAS).set("server");
            addOperation.get(KEY_PASSWORD).set("password");
            addOperation.get(GENERATE_SELF_SIGNED_CERTIFICATE_HOST).set("localhost");
            serverConfiguration.executeManagementOperation(addOperation);
            context.getLogger().infof("SSL server identity added to security realm %s.", RESOURCE_NAME);
            return ServerMigrationTaskResult.SUCCESS;
        });
        afterRun(context -> context.getLogger().infof("Security Realm '%s' SSL Server Identity configuration complete.", RESOURCE_NAME));
    }
}