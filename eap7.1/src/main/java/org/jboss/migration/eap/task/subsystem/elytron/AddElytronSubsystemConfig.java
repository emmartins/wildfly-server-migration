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

package org.jboss.migration.eap.task.subsystem.elytron;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableServerConfigurationType;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly11.task.subsystem.elytron.ConstantRealmMapperAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.ConstantRoleMapperAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.Permission;
import org.jboss.migration.wfly11.task.subsystem.elytron.PermissionMapping;
import org.jboss.migration.wfly11.task.subsystem.elytron.SecurityDomainAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.SimplePermissionMapperAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.SimpleRoleDecoderAddOperation;

/**
 * @author emmartins
 */
public class AddElytronSubsystemConfig<S> extends org.jboss.migration.wfly11.task.subsystem.elytron.AddElytronSubsystemConfig<S> {

    @Override
    protected void addSecurityDomains(final ManageableServerConfiguration configuration, final PathAddress subsystemPathAddress, final Operations.CompositeOperationBuilder compositeOperationBuilder) {
        final ManageableServerConfigurationType configurationType = configuration.getConfigurationType();
        if (configurationType == StandaloneServerConfiguration.RESOURCE_TYPE || configurationType == HostControllerConfiguration.RESOURCE_TYPE) {
            final SecurityDomainAddOperation securityDomainAddOperation = new SecurityDomainAddOperation(subsystemPathAddress, "ApplicationDomain")
                    .permissionMapper("default-permission-mapper")
                    .defaultRealm("ApplicationRealm")
                    .addRealm(new SecurityDomainAddOperation.Realm("ApplicationRealm").roleDecoder("groups-to-roles"));
            if (configurationType == StandaloneServerConfiguration.RESOURCE_TYPE) {
                securityDomainAddOperation.addRealm(new SecurityDomainAddOperation.Realm("local"));
            }
            compositeOperationBuilder.addStep(securityDomainAddOperation.toModelNode());
        }
        if (configurationType == StandaloneServerConfiguration.RESOURCE_TYPE || configurationType == HostConfiguration.RESOURCE_TYPE) {
            compositeOperationBuilder.addStep(new SecurityDomainAddOperation(subsystemPathAddress, "ManagementDomain")
                    .permissionMapper("default-permission-mapper")
                    .defaultRealm("ManagementRealm")
                    .addRealm(new SecurityDomainAddOperation.Realm("ManagementRealm").roleDecoder("groups-to-roles"))
                    .addRealm(new SecurityDomainAddOperation.Realm("local").roleMapper("super-user-mapper"))
                    .toModelNode());
        }
    }

    @Override
    protected void addMappers(final ManageableServerConfiguration configuration, final PathAddress subsystemPathAddress, final Operations.CompositeOperationBuilder compositeOperationBuilder) {
        final ManageableServerConfigurationType configurationType = configuration.getConfigurationType();
        final PermissionMapping anonymousPermissionMapping = new PermissionMapping()
                .addPrincipal("anonymous");
        if (configurationType == StandaloneServerConfiguration.RESOURCE_TYPE || configurationType == HostControllerConfiguration.RESOURCE_TYPE) {
            anonymousPermissionMapping.addPermission(new Permission("org.wildfly.extension.batch.jberet.deployment.BatchPermission").module("org.wildfly.extension.batch.jberet").targetName("*"))
                    .addPermission(new Permission("org.wildfly.transaction.client.RemoteTransactionPermission").module("org.wildfly.transaction.client"))
                    .addPermission(new Permission("org.jboss.ejb.client.RemoteEJBPermission").module("org.jboss.ejb-client"));
        }
        final PermissionMapping matchAllPermissionMapping = new PermissionMapping()
                .matchAll(true)
                .addPermission(new Permission("org.wildfly.security.auth.permission.LoginPermission"));
        if (configurationType == StandaloneServerConfiguration.RESOURCE_TYPE || configurationType == HostControllerConfiguration.RESOURCE_TYPE) {
            matchAllPermissionMapping.addPermission(new Permission("org.wildfly.extension.batch.jberet.deployment.BatchPermission").module("org.wildfly.extension.batch.jberet").targetName("*"))
                    .addPermission(new Permission("org.wildfly.transaction.client.RemoteTransactionPermission").module("org.wildfly.transaction.client"))
                    .addPermission(new Permission("org.jboss.ejb.client.RemoteEJBPermission").module("org.jboss.ejb-client"));
        }
        compositeOperationBuilder.addStep(new SimplePermissionMapperAddOperation(subsystemPathAddress, "default-permission-mapper")
                .mappingMode("first")
                .addPermissionMapping(anonymousPermissionMapping)
                .addPermissionMapping(matchAllPermissionMapping)
                .toModelNode());
        compositeOperationBuilder.addStep(new ConstantRealmMapperAddOperation(subsystemPathAddress, "local")
                .realmName("local")
                .toModelNode());
        compositeOperationBuilder.addStep(new SimpleRoleDecoderAddOperation(subsystemPathAddress, "groups-to-roles")
                .attribute("groups")
                .toModelNode());
        compositeOperationBuilder.addStep(new ConstantRoleMapperAddOperation(subsystemPathAddress, "super-user-mapper")
                .addRole("SuperUser")
                .toModelNode());
    }
}
