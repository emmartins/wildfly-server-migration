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

package org.jboss.migration.wfly10.config.task.subsystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public class SupportedExtensions {

    public static final Extension JACORB = new LegacyExtensionBuilder(ExtensionNames.JACORB)
            .addMigratedSubsystem(SubsystemNames.JACORB)
            .build();

    public static final Extension WEB = new LegacyExtensionBuilder(ExtensionNames.WEB)
            .addMigratedSubsystem(SubsystemNames.WEB)
            .build();

    public static final Extension MESSAGING = new LegacyExtensionBuilder(ExtensionNames.MESSAGING)
            .addMigratedSubsystem(SubsystemNames.MESSAGING)
            .build();

    public static final Extension INFINISPAN = new ExtensionBuilder(ExtensionNames.INFINISPAN)
            .addSupportedSubsystem(SubsystemNames.INFINISPAN)
            .build();

    public static final Extension JGROUPS = new ExtensionBuilder(ExtensionNames.JGROUPS)
            .addSupportedSubsystem(SubsystemNames.JGROUPS)
            .build();

    public static final Extension CONNECTOR = new ExtensionBuilder(ExtensionNames.CONNECTOR)
            .addSupportedSubsystem(SubsystemNames.DATASOURCES)
            .addSupportedSubsystem(SubsystemNames.JCA)
            .addSupportedSubsystem(SubsystemNames.RESOURCE_ADAPTERS)
            .build();

    public static final Extension DEPLOYMENT_SCANNER = new ExtensionBuilder(ExtensionNames.DEPLOYMENT_SCANNER)
            .addSupportedSubsystem(SubsystemNames.DEPLOYMENT_SCANNER)
            .build();

    public static final Extension EE = new ExtensionBuilder(ExtensionNames.EE)
            .addSupportedSubsystem(SubsystemNames.EE)
            .build();

    public static final Extension EJB3 = new ExtensionBuilder(ExtensionNames.EJB3)
            .addSupportedSubsystem(SubsystemNames.EJB3)
            .build();

    public static final Extension JAXRS = new ExtensionBuilder(ExtensionNames.JAXRS)
            .addSupportedSubsystem(SubsystemNames.JAXRS)
            .build();

    public static final Extension JDR = new ExtensionBuilder(ExtensionNames.JDR)
            .addSupportedSubsystem(SubsystemNames.JDR)
            .build();

    public static final Extension JMX = new ExtensionBuilder(ExtensionNames.JMX)
            .addSupportedSubsystem(SubsystemNames.JMX)
            .build();

    public static final Extension JPA = new ExtensionBuilder(ExtensionNames.JPA)
            .addSupportedSubsystem(SubsystemNames.JPA)
            .build();

    public static final Extension JSF = new ExtensionBuilder(ExtensionNames.JSF)
            .addSupportedSubsystem(SubsystemNames.JSF)
            .build();

    public static final Extension JSR77 =new ExtensionBuilder(ExtensionNames.JSR77)
            .addSupportedSubsystem(SubsystemNames.JSR77)
            .build();

    public static final Extension LOGGING = new ExtensionBuilder(ExtensionNames.LOGGING)
            .addSupportedSubsystem(SubsystemNames.LOGGING)
            .build();

    public static final Extension MAIL = new ExtensionBuilder(ExtensionNames.MAIL)
            .addSupportedSubsystem(SubsystemNames.MAIL)
            .build();

    public static final Extension MODCLUSTER = new ExtensionBuilder(ExtensionNames.MODCLUSTER)
            .addSupportedSubsystem(SubsystemNames.MODCLUSTER)
            .build();

    public static final Extension NAMING = new ExtensionBuilder(ExtensionNames.NAMING)
            .addSupportedSubsystem(SubsystemNames.NAMING)
            .build();

    public static final Extension POJO = new ExtensionBuilder(ExtensionNames.POJO)
            .addSupportedSubsystem(SubsystemNames.POJO)
            .build();

    public static final Extension REMOTING = new ExtensionBuilder(ExtensionNames.REMOTING)
            .addSupportedSubsystem(SubsystemNames.REMOTING)
            .build();

    public static final Extension SAR = new ExtensionBuilder(ExtensionNames.SAR)
            .addSupportedSubsystem(SubsystemNames.SAR)
            .build();

    public static final Extension SECURITY = new ExtensionBuilder(ExtensionNames.SECURITY)
            .addSupportedSubsystem(SubsystemNames.SECURITY)
            .build();

    public static final Extension TRANSACTIONS = new ExtensionBuilder(ExtensionNames.TRANSACTIONS)
            .addSupportedSubsystem(SubsystemNames.TRANSACTIONS)
            .build();

    public static final Extension WEBSERVICES = new ExtensionBuilder(ExtensionNames.WEBSERVICES)
            .addSupportedSubsystem(SubsystemNames.WEBSERVICES)
            .build();

    public static final Extension WELD = new ExtensionBuilder(ExtensionNames.WELD)
            .addSupportedSubsystem(SubsystemNames.WELD)
            .build();

    public static final Extension BATCH_JBERET = new ExtensionBuilder(ExtensionNames.BATCH_JBERET)
            .addSupportedSubsystem(SubsystemNames.BATCH_JBERET)
            .build();

    public static final Extension BEAN_VALIDATION = new ExtensionBuilder(ExtensionNames.BEAN_VALIDATION)
            .addSupportedSubsystem(SubsystemNames.BEAN_VALIDATION)
            .build();

    public static final Extension SINGLETON = new ExtensionBuilder(ExtensionNames.SINGLETON)
            .addSupportedSubsystem(SubsystemNames.SINGLETON)
            .build();

    public static final Extension IO = new ExtensionBuilder(ExtensionNames.IO)
            .addSupportedSubsystem(SubsystemNames.IO)
            .build();

    public static final Extension MESSAGING_ACTIVEMQ = new ExtensionBuilder(ExtensionNames.MESSAGING_ACTIVEMQ)
            .addSupportedSubsystem(SubsystemNames.MESSAGING_ACTIVEMQ)
            .build();

    public static final Extension REQUEST_CONTROLLER = new ExtensionBuilder(ExtensionNames.REQUEST_CONTROLLER)
            .addSupportedSubsystem(SubsystemNames.REQUEST_CONTROLLER)
            .build();

    public static final Extension SECURITY_MANAGER = new ExtensionBuilder(ExtensionNames.SECURITY_MANAGER)
            .addSupportedSubsystem(SubsystemNames.SECURITY_MANAGER)
            .build();

    public static final Extension UNDERTOW = new ExtensionBuilder(ExtensionNames.UNDERTOW)
            .addSupportedSubsystem(SubsystemNames.UNDERTOW)
            .build();

    public static final Extension IIOP_OPENJDK = new ExtensionBuilder(ExtensionNames.IIOP_OPENJDK)
            .addSupportedSubsystem(SubsystemNames.IIOP_OPENJDK)
            .build();

    public static List<Extension> all() {
        final List<Extension> result = new ArrayList<>();
        result.add(CONNECTOR);
        result.add(DEPLOYMENT_SCANNER);
        result.add(EE);
        result.add(EJB3);
        result.add(IIOP_OPENJDK);
        result.add(JACORB);
        result.add(JAXRS);
        result.add(JDR);
        result.add(JGROUPS);
        result.add(JMX);
        result.add(JPA);
        result.add(JSF);
        result.add(JSR77);
        result.add(LOGGING);
        result.add(MAIL);
        result.add(MESSAGING);
        result.add(MODCLUSTER);
        result.add(NAMING);
        result.add(POJO);
        result.add(REMOTING);
        result.add(SAR);
        result.add(SECURITY);
        result.add(TRANSACTIONS);
        result.add(WEBSERVICES);
        result.add(BATCH_JBERET);
        result.add(BEAN_VALIDATION);
        result.add(SINGLETON);
        result.add(INFINISPAN);
        result.add(IO);
        result.add(MESSAGING_ACTIVEMQ);
        result.add(REQUEST_CONTROLLER);
        result.add(SECURITY_MANAGER);
        result.add(UNDERTOW);
        result.add(WEB);
        result.add(WELD);
        return Collections.unmodifiableList(result);
    }

    public static List<Extension> allExcept(String... extensionNames) {
        final Set<String> excludeSet = new HashSet<>();
        for (String extensionName : extensionNames) {
            excludeSet.add(extensionName);
        }
        final List<Extension> result = new ArrayList<>();
        for (Extension extension : all()) {
            if (!excludeSet.contains(extension.getName())) {
                result.add(extension);
            }
        }
        return Collections.unmodifiableList(result);
    }
}