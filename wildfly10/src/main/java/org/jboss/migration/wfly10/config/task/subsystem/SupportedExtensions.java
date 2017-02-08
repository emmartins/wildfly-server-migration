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
            .subsystem(SubsystemNames.JACORB)
            .build();

    public static final Extension WEB = new LegacyExtensionBuilder(ExtensionNames.WEB)
            .subsystem(SubsystemNames.WEB)
            .build();

    public static final Extension MESSAGING = new LegacyExtensionBuilder(ExtensionNames.MESSAGING)
            .subsystem(SubsystemNames.MESSAGING)
            .build();

    public static final Extension INFINISPAN = new ExtensionBuilder(ExtensionNames.INFINISPAN)
            .addSubsystem(SubsystemNames.INFINISPAN)
            .build();

    public static final Extension JGROUPS = new ExtensionBuilder(ExtensionNames.JGROUPS)
            .addSubsystem(SubsystemNames.JGROUPS)
            .build();

    public static final Extension CONNECTOR = new ExtensionBuilder(ExtensionNames.CONNECTOR)
            .addSubsystem(SubsystemNames.DATASOURCES)
            .addSubsystem(SubsystemNames.JCA)
            .addSubsystem(SubsystemNames.RESOURCE_ADAPTERS)
            .build();

    public static final Extension DEPLOYMENT_SCANNER = new ExtensionBuilder(ExtensionNames.DEPLOYMENT_SCANNER)
            .addSubsystem(SubsystemNames.DEPLOYMENT_SCANNER)
            .build();

    public static final Extension EE = new ExtensionBuilder(ExtensionNames.EE)
            .addSubsystem(SubsystemNames.EE)
            .build();

    public static final Extension EJB3 = new ExtensionBuilder(ExtensionNames.EJB3)
            .addSubsystem(SubsystemNames.EJB3)
            .build();

    public static final Extension JAXRS = new ExtensionBuilder(ExtensionNames.JAXRS)
            .addSubsystem(SubsystemNames.JAXRS)
            .build();

    public static final Extension JDR = new ExtensionBuilder(ExtensionNames.JDR)
            .addSubsystem(SubsystemNames.JDR)
            .build();

    public static final Extension JMX = new ExtensionBuilder(ExtensionNames.JMX)
            .addSubsystem(SubsystemNames.JMX)
            .build();

    public static final Extension JPA = new ExtensionBuilder(ExtensionNames.JPA)
            .addSubsystem(SubsystemNames.JPA)
            .build();

    public static final Extension JSF = new ExtensionBuilder(ExtensionNames.JSF)
            .addSubsystem(SubsystemNames.JSF)
            .build();

    public static final Extension JSR77 =new ExtensionBuilder(ExtensionNames.JSR77)
            .addSubsystem(SubsystemNames.JSR77)
            .build();

    public static final Extension LOGGING = new ExtensionBuilder(ExtensionNames.LOGGING)
            .addSubsystem(SubsystemNames.LOGGING)
            .build();

    public static final Extension MAIL = new ExtensionBuilder(ExtensionNames.MAIL)
            .addSubsystem(SubsystemNames.MAIL)
            .build();

    public static final Extension MODCLUSTER = new ExtensionBuilder(ExtensionNames.MODCLUSTER)
            .addSubsystem(SubsystemNames.MODCLUSTER)
            .build();

    public static final Extension NAMING = new ExtensionBuilder(ExtensionNames.NAMING)
            .addSubsystem(SubsystemNames.NAMING)
            .build();

    public static final Extension POJO = new ExtensionBuilder(ExtensionNames.POJO)
            .addSubsystem(SubsystemNames.POJO)
            .build();

    public static final Extension REMOTING = new ExtensionBuilder(ExtensionNames.REMOTING)
            .addSubsystem(SubsystemNames.REMOTING)
            .build();

    public static final Extension SAR = new ExtensionBuilder(ExtensionNames.SAR)
            .addSubsystem(SubsystemNames.SAR)
            .build();

    public static final Extension SECURITY = new ExtensionBuilder(ExtensionNames.SECURITY)
            .addSubsystem(SubsystemNames.SECURITY)
            .build();

    public static final Extension TRANSACTIONS = new ExtensionBuilder(ExtensionNames.TRANSACTIONS)
            .addSubsystem(SubsystemNames.TRANSACTIONS)
            .build();

    public static final Extension WEBSERVICES = new ExtensionBuilder(ExtensionNames.WEBSERVICES)
            .addSubsystem(SubsystemNames.WEBSERVICES)
            .build();

    public static final Extension WELD = new ExtensionBuilder(ExtensionNames.WELD)
            .addSubsystem(SubsystemNames.WELD)
            .build();

    public static final Extension BATCH_JBERET = new ExtensionBuilder(ExtensionNames.BATCH_JBERET)
            .addSubsystem(SubsystemNames.BATCH_JBERET)
            .build();

    public static final Extension BEAN_VALIDATION = new ExtensionBuilder(ExtensionNames.BEAN_VALIDATION)
            .addSubsystem(SubsystemNames.BEAN_VALIDATION)
            .build();

    public static final Extension SINGLETON = new ExtensionBuilder(ExtensionNames.SINGLETON)
            .addSubsystem(SubsystemNames.SINGLETON)
            .build();

    public static final Extension IO = new ExtensionBuilder(ExtensionNames.IO)
            .addSubsystem(SubsystemNames.IO)
            .build();

    public static final Extension MESSAGING_ACTIVEMQ = new ExtensionBuilder(ExtensionNames.MESSAGING_ACTIVEMQ)
            .addSubsystem(SubsystemNames.MESSAGING_ACTIVEMQ)
            .build();

    public static final Extension REQUEST_CONTROLLER = new ExtensionBuilder(ExtensionNames.REQUEST_CONTROLLER)
            .addSubsystem(SubsystemNames.REQUEST_CONTROLLER)
            .build();

    public static final Extension SECURITY_MANAGER = new ExtensionBuilder(ExtensionNames.SECURITY_MANAGER)
            .addSubsystem(SubsystemNames.SECURITY_MANAGER)
            .build();

    public static final Extension UNDERTOW = new ExtensionBuilder(ExtensionNames.UNDERTOW)
            .addSubsystem(SubsystemNames.UNDERTOW)
            .build();

    public static final Extension IIOP_OPENJDK = new ExtensionBuilder(ExtensionNames.IIOP_OPENJDK)
            .addSubsystem(SubsystemNames.IIOP_OPENJDK)
            .build();

    public static Set<Extension> all() {
        final Set<Extension> result = new HashSet<>();
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
        return Collections.unmodifiableSet(result);
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