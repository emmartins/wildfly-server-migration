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
package org.wildfly.migration.wildfly.standalone.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class Extensions {

    public static final List<Extension> SUPPORTED = initSupportedExtensions();

    private static List<Extension> initSupportedExtensions() {
        List<Extension> supportedExtensions = new ArrayList<>();
        supportedExtensions.add(new Extension("org.jboss.as.clustering.infinispan").addSubsystem(new Subsystem("infinispan")));
        supportedExtensions.add(new Extension("org.jboss.as.clustering.jgroups").addSubsystem(new Subsystem("jgroups")));
        supportedExtensions.add(new Extension("org.jboss.as.connector").addSubsystem(new Subsystem("jca")).addSubsystem(new Subsystem("resource-adapters")).addSubsystem(new Subsystem("datasources")));
        supportedExtensions.add(new Extension("org.jboss.as.deployment-scanner").addSubsystem(new Subsystem("deployment-scanner")));
        supportedExtensions.add(new Extension("org.jboss.as.ee").addSubsystem(new Subsystem("ee")));
        supportedExtensions.add(new Extension("org.jboss.as.ejb3").addSubsystem(new Subsystem("ejb3")));
        supportedExtensions.add(new Extension("org.jboss.as.jaxrs").addSubsystem(new Subsystem("jaxrs")));
        supportedExtensions.add(new Extension("org.jboss.as.jdr").addSubsystem(new Subsystem("jdr")));
        supportedExtensions.add(new Extension("org.jboss.as.jmx").addSubsystem(new Subsystem("jmx")));
        supportedExtensions.add(new Extension("org.jboss.as.jpa").addSubsystem(new Subsystem("jpa")));
        supportedExtensions.add(new Extension("org.jboss.as.jsf").addSubsystem(new Subsystem("jsf")));
        supportedExtensions.add(new Extension("org.jboss.as.jsr77").addSubsystem(new Subsystem("jsr77")));
        supportedExtensions.add(new Extension("org.jboss.as.logging").addSubsystem(new Subsystem("logging")));
        supportedExtensions.add(new Extension("org.jboss.as.mail").addSubsystem(new Subsystem("mail")));
        supportedExtensions.add(new Extension("org.jboss.as.modcluster").addSubsystem(new Subsystem("modcluster")));
        supportedExtensions.add(new Extension("org.jboss.as.naming").addSubsystem(new Subsystem("naming")));
        supportedExtensions.add(new Extension("org.jboss.as.pojo").addSubsystem(new Subsystem("pojo")));
        supportedExtensions.add(new Extension("org.jboss.as.remoting").addSubsystem(new Subsystem("remoting")));
        supportedExtensions.add(new Extension("org.jboss.as.sar").addSubsystem(new Subsystem("sar")));
        supportedExtensions.add(new Extension("org.jboss.as.security").addSubsystem(new Subsystem("security")));
        supportedExtensions.add(new Extension("org.jboss.as.transactions").addSubsystem(new Subsystem("transactions")));
        supportedExtensions.add(new Extension("org.jboss.as.webservices").addSubsystem(new Subsystem("webservices")));
        supportedExtensions.add(new Extension("org.jboss.as.weld").addSubsystem(new Subsystem("weld")));
        supportedExtensions.add(new Extension("org.wildfly.extension.batch.jberet").addSubsystem(new Subsystem("batch-jberet")));
        supportedExtensions.add(new Extension("org.wildfly.extension.bean-validation").addSubsystem(new Subsystem("bean-validation")));
        supportedExtensions.add(new Extension("org.wildfly.extension.clustering.singleton").addSubsystem(new Subsystem("singleton")));
        supportedExtensions.add(new Extension("org.wildfly.extension.io").addSubsystem(new Subsystem("io")));
        supportedExtensions.add(new Extension("org.wildfly.extension.messaging-activemq").addSubsystem(new Subsystem("messaging-activemq")));
        supportedExtensions.add(new Extension("org.wildfly.extension.request-controller").addSubsystem(new Subsystem("request-controller")));
        supportedExtensions.add(new Extension("org.wildfly.extension.security.manager").addSubsystem(new Subsystem("security-manager")));
        supportedExtensions.add(new Extension("org.wildfly.extension.undertow").addSubsystem(new Subsystem("undertow")));
        supportedExtensions.add(new Extension("org.wildfly.iiop-openjdk").addSubsystem(new Subsystem("iiop-openjdk")));
        // add legacy extensions
        supportedExtensions.add(new Extension("org.jboss.as.jacorb").addSubsystem(new Subsystem("jacorb", true)));
        supportedExtensions.add(new Extension("org.jboss.as.web").addSubsystem(new Subsystem("web", true)));
        supportedExtensions.add(new Extension("org.jboss.as.messaging").addSubsystem(new Subsystem("messaging", true)));
        return Collections.unmodifiableList(supportedExtensions);
    }

    private Extensions() {
    }

}
