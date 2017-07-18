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

package org.jboss.migration.eap.task;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceBuildParameters;
import org.jboss.migration.wfly10.config.task.management.subsystem.AddSubsystemResourceSubtaskBuilder;
import org.jboss.migration.wfly10.config.task.management.subsystem.AddSubsystemResources;
import org.jboss.migration.wfly10.config.task.subsystem.ExtensionNames;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;

import java.util.Arrays;
import java.util.List;

import static org.jboss.migration.core.task.component.TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet;

/**
 * @author emmartins
 */
public class AddElytronSubsystem<S> extends AddSubsystemResources<S> {
    public AddElytronSubsystem() {
        super(ExtensionNames.ELYTRON, new AddElytronSubsystemConfig<>());
    }

    public static class AddElytronSubsystemConfig<S> extends AddSubsystemResourceSubtaskBuilder<S> {
        protected AddElytronSubsystemConfig() {
            super(SubsystemNames.ELYTRON);
            skipPolicy(skipIfDefaultTaskSkipPropertyIsSet());
        }

        @Override
        protected void addConfiguration(ManageableResourceBuildParameters<S, SubsystemResource.Parent> params, TaskContext taskContext) {
            final ManageableServerConfiguration configuration = params.getServerConfiguration();
            final PathAddress subsystemPathAddress = params.getResource().getSubsystemResourcePathAddress(getSubsystem());
            // all ops will be executed in composed
            final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
            // add subsystem
            compositeOperationBuilder.addStep(getAddSubsystemOp(subsystemPathAddress));
            // add provider loaders
            compositeOperationBuilder.addStep(getAddProviderLoaderOp(subsystemPathAddress, "elytron", "org.wildfly.security.elytron"));
            compositeOperationBuilder.addStep(getAddProviderLoaderOp(subsystemPathAddress, "openssl", "org.wildfly.openssl"));
            // add aggregate providers
            compositeOperationBuilder.addStep(getAddAggregateProvidersOp(subsystemPathAddress, "elytron", "openssl"));
            // add file audit log
            compositeOperationBuilder.addStep(getAddFileAuditLogOp(subsystemPathAddress, "local-audit", "audit.log", "jboss.server.log.dir", "JSON"));
            // add security domains
            compositeOperationBuilder.addStep(getAddSecurityDomainOp(subsystemPathAddress, "ApplicationDomain", "default-permission-mapper", "ApplicationRealm", "local-audit", getSecurityDomainRealmNode("ApplicationRealm", "groups-to-roles"), getSecurityDomainRealmNode("local", null)));
            compositeOperationBuilder.addStep(getAddSecurityDomainOp(subsystemPathAddress, "ManagementDomain", "default-permission-mapper", "ManagementRealm", "local-audit", getSecurityDomainRealmNode("ManagementRealm", "groups-to-roles"), getSecurityDomainRealmNode("local", "super-user-mapper")));
            // add properties realms
            compositeOperationBuilder.addStep(getAddPropertiesRealmOp(subsystemPathAddress, "ApplicationRealm", "application-users.properties", "jboss.server.config.dir", "ApplicationRealm", "application-roles.properties", "jboss.server.config.dir"));
            compositeOperationBuilder.addStep(getAddPropertiesRealmOp(subsystemPathAddress, "ManagementRealm", "mgmt-users.properties", "jboss.server.config.dir", "ManagementRealm", "mgmt-groups.properties", "jboss.server.config.dir"));
            // add permission mappers
            compositeOperationBuilder.addStep(getLogicalPermissionMapperAddOperation(subsystemPathAddress, "default-permission-mapper", "unless", "constant-permission-mapper", "anonymous-permission-mapper"));
            compositeOperationBuilder.addStep(getSimplePermissionMapperAddOperation(subsystemPathAddress, "anonymous-permission-mapper", Arrays.asList("anonymous"), Arrays.asList(new Permission("org.wildfly.security.auth.permission.LoginPermission"))));
            compositeOperationBuilder.addStep(getConstantPermissionMapperAddOperation(subsystemPathAddress, "constant-permission-mapper",
                    Arrays.asList(new Permission("org.wildfly.security.auth.permission.LoginPermission"),
                            new Permission("org.wildfly.extension.batch.jberet.deployment.BatchPermission", "org.wildfly.extension.batch.jberet", "*"),
                            new Permission("org.wildfly.transaction.client.RemoteTransactionPermission", "org.wildfly.transaction.client"),
                            new Permission("org.jboss.ejb.client.RemoteEJBPermission", "org.jboss.ejb-client"))));
            // add realm mapper
            compositeOperationBuilder.addStep(getConstantRealmMapperOp(subsystemPathAddress, "local", "local"));
            // add simple role decoder
            compositeOperationBuilder.addStep(getSimpleRoleDecoderOp(subsystemPathAddress, "groups-to-roles", "groups"));
            // add http authentication factories
            compositeOperationBuilder.addStep(getHttpAuthenticationFactoryOp(subsystemPathAddress, "management-http-authentication", "ManagementDomain", "global", Arrays.asList(new MechanismConfiguration("DIGEST", Arrays.asList(new MechanismRealmConfiguration("ManagementRealm"))))));
            compositeOperationBuilder.addStep(getHttpAuthenticationFactoryOp(subsystemPathAddress, "application-http-authentication", "ApplicationDomain", "global", Arrays.asList(new MechanismConfiguration("BASIC", Arrays.asList(new MechanismRealmConfiguration("Application Realm"))), new MechanismConfiguration("FORM"))));
            // execute composed
            configuration.executeManagementOperation(compositeOperationBuilder.build().getOperation());
        }

        private ModelNode getAddSubsystemOp(final PathAddress subsystemPathAddress) {
            /*
            "final-providers" => "combined-providers",
            "disallowed-providers" => ["OracleUcrypto"],
            "operation" => "add",
            "address" => [("subsystem" => "elytron")]
             */
            final ModelNode operation = Util.createAddOperation(subsystemPathAddress);
            operation.get("final-providers").set("combined-providers");
            operation.get("disallowed-providers").setEmptyList().add("OracleUcrypto");
            return operation;
        }

        private ModelNode getAddProviderLoaderOp(final PathAddress subsystemPathAddress, final String providerLoader, final String module) {
            /*
            "module" => "org.wildfly.security.elytron",
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("provider-loader" => "elytron")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("provider-loader", providerLoader);
            final ModelNode operation = Util.createAddOperation(pathAddress);
            operation.get("module").set(module);
            return operation;
        }

        private ModelNode getAddAggregateProvidersOp(final PathAddress subsystemPathAddress, String... providers) {
            /*
            "providers" => [
                "elytron",
                "openssl"
            ],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("aggregate-providers" => "combined-providers")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("aggregate-providers", "combined-providers");
            final ModelNode operation = Util.createAddOperation(pathAddress);
            final ModelNode providersNode = operation.get("providers").setEmptyList();
            for (String provider : providers) {
                providersNode.add(provider);
            }
            return operation;
        }

        private ModelNode getAddFileAuditLogOp(final PathAddress subsystemPathAddress, String fileAuditLog, String path, String relativeTo, String format) {
            /*
            "path" => "audit.log",
            "relative-to" => "jboss.server.log.dir",
            "format" => "JSON",
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("file-audit-log" => "local-audit")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("file-audit-log", fileAuditLog);
            final ModelNode operation = Util.createAddOperation(pathAddress);
            operation.get("path").set(path);
            operation.get("relative-to").set(relativeTo);
            operation.get("format").set(format);
            return operation;
        }

        private static ModelNode getSecurityDomainRealmNode(String realm, String roleDecoder) {
            final ModelNode modelNode = new ModelNode();
            modelNode.get("realm").set(realm);
            if (roleDecoder != null) {
                modelNode.get("role-decoder").set(roleDecoder);
            }
            return modelNode;
        }

        private ModelNode getAddSecurityDomainOp(final PathAddress subsystemPathAddress, String securityDomain, String permissionMapper, String defaultRealm, String securityEventListener, ModelNode... realms) {
            /*
            "permission-mapper" => "default-permission-mapper",
            "default-realm" => "ApplicationRealm",
            "realms" => [
                {
                    "realm" => "ApplicationRealm",
                    "role-decoder" => "groups-to-roles"
                },
                {"realm" => "local"}
            ],
            "security-event-listener" => "local-audit",
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("security-domain" => "ApplicationDomain")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("security-domain", securityDomain);
            final ModelNode operation = Util.createAddOperation(pathAddress);
            operation.get("permission-mapper").set(permissionMapper);
            operation.get("default-realm").set(defaultRealm);
            if (securityEventListener != null) {
                operation.get("security-event-listener").set(securityEventListener);
            }
            final ModelNode operationRealms = operation.get("realms").setEmptyList();
            for (ModelNode realm : realms) {
                operationRealms.add(realm);
            }
            return operation;
        }

        private ModelNode getAddIdentityRealmOp(final PathAddress subsystemPathAddress, String identityRealm, String identity) {
            /*
             "identity" => "$local",
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("identity-realm" => "local")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("identity-realm", identityRealm);
            final ModelNode operation = Util.createAddOperation(pathAddress);
            operation.get("identity").set(identity);
            return operation;
        }

        private ModelNode getAddPropertiesRealmOp(final PathAddress subsystemPathAddress, String propertiesRealm, String usersPropertiesPath, String usersPropertiesRelativeTo, String usersPropertiesDigestRealmName, String groupsPropertiesPath, String groupsPropertiesRelativeTo) {
            /*
            "users-properties" => {
                "path" => "application-users.properties",
                "relative-to" => "jboss.server.config.dir",
                "digest-realm-name" => "ApplicationRealm"
            },
            "groups-properties" => {
                "path" => "application-roles.properties",
                "relative-to" => "jboss.server.config.dir"
            },
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("properties-realm" => "ApplicationRealm")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("properties-realm", propertiesRealm);
            final ModelNode operation = Util.createAddOperation(pathAddress);

            final ModelNode usersProperties = new ModelNode();
            usersProperties.get("path").set(usersPropertiesPath);
            usersProperties.get("relative-to").set(usersPropertiesRelativeTo);
            usersProperties.get("digest-realm-name").set(usersPropertiesDigestRealmName);
            operation.get("users-properties").set(usersProperties);

            final ModelNode groupsProperties = new ModelNode();
            groupsProperties.get("path").set(groupsPropertiesPath);
            groupsProperties.get("relative-to").set(groupsPropertiesRelativeTo);
            operation.get("groups-properties").set(groupsProperties);

            return operation;
        }

        private ModelNode getLogicalPermissionMapperAddOperation(final PathAddress subsystemPathAddress, String logicalPermissionMapper, String logicalOperation, String left, String right) {
            /*
            "logical-operation" => "unless",
            "left" => "constant-permission-mapper",
            "right" => "anonymous-permission-mapper",
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("logical-permission-mapper" => "default-permission-mapper")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("logical-permission-mapper", logicalPermissionMapper);
            final ModelNode operation = Util.createAddOperation(pathAddress);
            operation.get("logical-operation").set(logicalOperation);
            operation.get("left").set(left);
            operation.get("right").set(right);
            return operation;
        }

        private ModelNode getSimplePermissionMapperAddOperation(final PathAddress subsystemPathAddress, String simplePermissionMapper, List<String> principals, List<Permission> permissions) {
            /*
            "permission-mappings" => [{
                "principals" => ["anonymous"],
                "permissions" => [{"class-name" => "org.wildfly.security.auth.permission.LoginPermission"}]
            }],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("simple-permission-mapper" => "anonymous-permission-mapper")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("simple-permission-mapper", simplePermissionMapper);
            final ModelNode operation = Util.createAddOperation(pathAddress);
            final ModelNode permissionMappings = new ModelNode();
            final ModelNode principalsNode = permissionMappings.get("principals").setEmptyList();
            for (String principal : principals) {
                principalsNode.add(principal);
            }
            final ModelNode permissionsNode = permissionMappings.get("permissions").setEmptyList();
            for (Permission permission : permissions) {
                permissionsNode.add(permission.toModelNode());
            }
            operation.get("permission-mappings").set(permissionMappings);
            return operation;
        }

        private ModelNode getConstantPermissionMapperAddOperation(final PathAddress subsystemPathAddress, String contanstPermissionMapper, List<Permission> permissions) {
            /*
            "permissions" => [
                {"class-name" => "org.wildfly.security.auth.permission.LoginPermission"},
                {
                    "class-name" => "org.wildfly.extension.batch.jberet.deployment.BatchPermission",
                    "module" => "org.wildfly.extension.batch.jberet",
                    "target-name" => "*"
                },
                {
                    "class-name" => "org.wildfly.transaction.client.RemoteTransactionPermission",
                    "module" => "org.wildfly.transaction.client"
                },
                {
                    "class-name" => "org.jboss.ejb.client.RemoteEJBPermission",
                    "module" => "org.jboss.ejb-client"
                }
            ],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("constant-permission-mapper" => "constant-permission-mapper")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("constant-permission-mapper", contanstPermissionMapper);
            final ModelNode operation = Util.createAddOperation(pathAddress);
            final ModelNode permissionsNode = operation.get("permissions").setEmptyList();
            for (Permission permission : permissions) {
                permissionsNode.add(permission.toModelNode());
            }
            return operation;
        }

        private ModelNode getConstantRealmMapperOp(final PathAddress subsystemPathAddress, String constantRealmMapper, String realmName) {
            /*
              "realm-name" => "local",
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("constant-realm-mapper" => "local")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("constant-realm-mapper", constantRealmMapper);
            final ModelNode operation = Util.createAddOperation(pathAddress);
            operation.get("realm-name").set(realmName);
            return operation;
        }

        private ModelNode getSimpleRoleDecoderOp(final PathAddress subsystemPathAddress, String simpleRoleDecoder, String attribute) {
            /*
              "attribute" => "groups",
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("simple-role-decoder" => "groups-to-roles")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("simple-role-decoder", simpleRoleDecoder);
            final ModelNode operation = Util.createAddOperation(pathAddress);
            operation.get("attribute").set(attribute);
            return operation;
        }

        private ModelNode getConstantRoleMapperOp(final PathAddress subsystemPathAddress, String constantRoleMapper, List<String> roles) {
            /*
              "roles" => ["SuperUser"],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("constant-role-mapper" => "super-user-mapper")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("constant-role-mapper", constantRoleMapper);
            final ModelNode operation = Util.createAddOperation(pathAddress);
            final ModelNode rolesNode = operation.get("roles").setEmptyList();
            for (String role : roles) {
                rolesNode.add(role);
            }
            return operation;
        }


        private ModelNode getHttpAuthenticationFactoryOp(final PathAddress subsystemPathAddress, String httpAuthenticationFactory, String securityDomain, String httpServerMechanismFactory, List<MechanismConfiguration> mechanismConfigurations) {
            /*
              "security-domain" => "ManagementDomain",
            "http-server-mechanism-factory" => "global",
            "mechanism-configurations" => [{
                "mechanism-name" => "DIGEST",
                "mechanism-realm-configurations" => [{"realm-name" => "ManagementRealm"}]
            }],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("http-authentication-factory" => "management-http-authentication")
            ]
             */
            final PathAddress pathAddress = subsystemPathAddress.append("http-authentication-factory", httpAuthenticationFactory);
            final ModelNode operation = Util.createAddOperation(pathAddress);
            operation.get("security-domain").set(securityDomain);
            operation.get("http-server-mechanism-factory").set(httpServerMechanismFactory);
            final ModelNode mechanismConfigurationsNode = operation.get("mechanism-configurations").setEmptyList();
            for (MechanismConfiguration mechanismConfiguration : mechanismConfigurations) {
                mechanismConfigurationsNode.add(mechanismConfiguration.toModelNode());
            }
            return operation;
        }
        /* TODO

        {
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("provider-http-server-mechanism-factory" => "global")
            ]
        },


        {
            "security-domain" => "ManagementDomain",
            "sasl-server-factory" => "configured",
            "mechanism-configurations" => [
                {
                    "mechanism-name" => "JBOSS-LOCAL-USER",
                    "realm-mapper" => "local"
                },
                {
                    "mechanism-name" => "DIGEST-MD5",
                    "mechanism-realm-configurations" => [{"realm-name" => "ManagementRealm"}]
                }
            ],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("sasl-authentication-factory" => "management-sasl-authentication")
            ]
        },


        {
            "security-domain" => "ApplicationDomain",
            "sasl-server-factory" => "configured",
            "mechanism-configurations" => [
                {
                    "mechanism-name" => "JBOSS-LOCAL-USER",
                    "realm-mapper" => "local"
                },
                {
                    "mechanism-name" => "DIGEST-MD5",
                    "mechanism-realm-configurations" => [{"realm-name" => "ApplicationRealm"}]
                }
            ],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("sasl-authentication-factory" => "application-sasl-authentication")
            ]
        },


        {
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("provider-sasl-server-factory" => "global")
            ]
        },


        {
            "sasl-server-factory" => "global",
            "filters" => [{"provider-name" => "WildFlyElytron"}],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("mechanism-provider-filtering-sasl-server-factory" => "elytron")
            ]
        },


        {
            "sasl-server-factory" => "elytron",
            "properties" => {"wildfly.sasl.local-user.default-user" => "$local"},
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("configurable-sasl-server-factory" => "configured")
            ]
        }

         */

    }

    /**
     "class-name" => "org.wildfly.extension.batch.jberet.deployment.BatchPermission",
     "module" => "org.wildfly.extension.batch.jberet",
     "target-name" => "*"
     */
    private static class Permission {
        final String className;
        final String module;
        final String targetName;
        Permission(String className) {
            this(className, null, null);
        }
        Permission(String className, String module) {
            this(className, module, null);
        }
        Permission(String className, String module, String targetName) {
            this.className = className;
            this.module = module;
            this.targetName = targetName;
        }
        ModelNode toModelNode() {
            final ModelNode modelNode = new ModelNode();
            modelNode.get("class-name").set(className);
            if (module != null) {
                modelNode.get("module").set(module);
            }
            if (targetName != null) {
                modelNode.get("target-name").set(targetName);
            }
            return modelNode;
        }
    }

    /**
     "mechanism-name" => "DIGEST",
     "mechanism-realm-configurations" => [{"realm-name" => "ManagementRealm"}]
     */
    private static class MechanismConfiguration {
        final String mechanismName;
        final List<MechanismRealmConfiguration> mechanismRealmConfigurations;
        MechanismConfiguration(String mechanismName) {
            this(mechanismName, null);
        }
        MechanismConfiguration(String mechanismName, List<MechanismRealmConfiguration> mechanismRealmConfigurations) {
            this.mechanismName = mechanismName;
            this.mechanismRealmConfigurations = mechanismRealmConfigurations;
        }
        ModelNode toModelNode() {
            final ModelNode modelNode = new ModelNode();
            modelNode.get("mechanism-name").set(mechanismName);
            if (mechanismRealmConfigurations != null && !mechanismRealmConfigurations.isEmpty()) {
                final ModelNode mechanismRealmConfigurationsNode = modelNode.get("mechanism-realm-configurations").setEmptyList();
                for (MechanismRealmConfiguration mechanismRealmConfiguration : mechanismRealmConfigurations) {
                    mechanismRealmConfigurationsNode.add(mechanismRealmConfiguration.toModelNode());
                }
            }
            return modelNode;
        }
    }

    /**
     "realm-name" => "ManagementRealm"
     */
    private static class MechanismRealmConfiguration {
        final String realmName;
        MechanismRealmConfiguration(String realmName) {
            this.realmName = realmName;
        }
        ModelNode toModelNode() {
            final ModelNode modelNode = new ModelNode();
            modelNode.get("realm-name").set(realmName);
            return modelNode;
        }
    }
}
