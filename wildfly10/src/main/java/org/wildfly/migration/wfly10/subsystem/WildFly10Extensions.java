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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class WildFly10Extensions {

    public static final List<WildFly10Extension> SUPPORTED = initSupportedExtensions();

    private static List<WildFly10Extension> initSupportedExtensions() {
        List<WildFly10Extension> supportedExtensions = new ArrayList<>();
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.clustering.infinispan").addBasicSubsystem("infinispan"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.clustering.jgroups").addBasicSubsystem("jgroups"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.connector").addBasicSubsystem("jca").addBasicSubsystem("resource-adapters").addBasicSubsystem("datasources"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.deployment-scanner").addBasicSubsystem("deployment-scanner"));
        supportedExtensions.add(EEWildFly10Extension.INSTANCE);
        supportedExtensions.add(EJb3WildFly10Extension.INSTANCE);
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.jaxrs").addBasicSubsystem("jaxrs"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.jdr").addBasicSubsystem("jdr"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.jmx").addBasicSubsystem("jmx"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.jpa").addBasicSubsystem("jpa"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.jsf").addBasicSubsystem("jsf"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.jsr77").addBasicSubsystem("jsr77"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.logging").addBasicSubsystem("logging"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.mail").addBasicSubsystem("mail"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.modcluster").addBasicSubsystem("modcluster"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.naming").addBasicSubsystem("naming"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.pojo").addBasicSubsystem("pojo"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.remoting").addBasicSubsystem("remoting"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.sar").addBasicSubsystem("sar"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.security").addBasicSubsystem("security"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.transactions").addBasicSubsystem("transactions"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.webservices").addBasicSubsystem("webservices"));
        supportedExtensions.add(new BasicWildFly10Extension("org.jboss.as.weld").addBasicSubsystem("weld"));
        supportedExtensions.add(BatchJBeretWildFly10Extension.INSTANCE);
        supportedExtensions.add(BeanValidationWildFly10Extension.INSTANCE);
        supportedExtensions.add(new BasicWildFly10Extension("org.wildfly.extension.clustering.singleton").addBasicSubsystem("singleton"));
        supportedExtensions.add(new BasicWildFly10Extension("org.wildfly.extension.io").addBasicSubsystem("io"));
        supportedExtensions.add(new BasicWildFly10Extension("org.wildfly.extension.messaging-activemq").addBasicSubsystem("messaging-activemq"));
        supportedExtensions.add(RequestControllerWildFly10Extension.INSTANCE);
        supportedExtensions.add(new BasicWildFly10Extension("org.wildfly.extension.security.manager").addBasicSubsystem("security-manager"));
        supportedExtensions.add(new BasicWildFly10Extension("org.wildfly.extension.undertow").addBasicSubsystem("undertow"));
        supportedExtensions.add(new BasicWildFly10Extension("org.wildfly.iiop-openjdk").addBasicSubsystem("iiop-openjdk"));
        // add legacy extensions
        supportedExtensions.add(new LegacyWildFly10Extension("org.jboss.as.jacorb").addLegacySubsystem("jacorb"));
        supportedExtensions.add(new LegacyWildFly10Extension("org.jboss.as.web").addLegacySubsystem("web"));
        supportedExtensions.add(new LegacyWildFly10Extension("org.jboss.as.messaging").addLegacySubsystem("messaging"));
        return Collections.unmodifiableList(supportedExtensions);
    }

    private WildFly10Extensions() {
    }

}
