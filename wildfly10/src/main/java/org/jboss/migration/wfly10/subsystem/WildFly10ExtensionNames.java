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
package org.jboss.migration.wfly10.subsystem;

/**
 * The names of WildFly 10 extensions.
 * @author emmartins
 */
public interface WildFly10ExtensionNames {

    String BATCH_JBERET = "org.wildfly.extension.batch.jberet";
    String BEAN_VALIDATION = "org.wildfly.extension.bean-validation";
    String CONNECTOR = "org.jboss.as.connector";
    String DEPLOYMENT_SCANNER = "org.jboss.as.deployment-scanner";
    String EE = "org.jboss.as.ee";
    String EJB3 = "org.jboss.as.ejb3";
    String IIOP_OPENJDK = "org.wildfly.iiop-openjdk";
    String INFINISPAN = "org.jboss.as.clustering.infinispan";
    String IO = "org.wildfly.extension.io";
    String JACORB = "org.jboss.as.jacorb";
    String JAXRS = "org.jboss.as.jaxrs";
    String JDR = "org.jboss.as.jdr";
    String JGROUPS = "org.jboss.as.clustering.jgroups";
    String JMX = "org.jboss.as.jmx";
    String JPA = "org.jboss.as.jpa";
    String JSF = "org.jboss.as.jsf";
    String JSR77 = "org.jboss.as.jsr77";
    String LOGGING = "org.jboss.as.logging";
    String MAIL = "org.jboss.as.mail";
    String MESSAGING = "org.jboss.as.messaging";
    String MESSAGING_ACTIVEMQ = "org.wildfly.extension.messaging-activemq";
    String MODCLUSTER = "org.jboss.as.modcluster";
    String NAMING = "org.jboss.as.naming";
    String POJO = "org.jboss.as.pojo";
    String REMOTING = "org.jboss.as.remoting";
    String REQUEST_CONTROLLER = "org.wildfly.extension.request-controller";
    String SAR = "org.jboss.as.sar";
    String SECURITY = "org.jboss.as.security";
    String SECURITY_MANAGER = "org.wildfly.extension.security.manager";
    String SINGLETON = "org.wildfly.extension.clustering.singleton";
    String TRANSACTIONS = "org.jboss.as.transactions";
    String UNDERTOW = "org.wildfly.extension.undertow";
    String WEB = "org.jboss.as.web";
    String WEBSERVICES = "org.jboss.as.webservices";
    String WELD = "org.jboss.as.weld";

}
