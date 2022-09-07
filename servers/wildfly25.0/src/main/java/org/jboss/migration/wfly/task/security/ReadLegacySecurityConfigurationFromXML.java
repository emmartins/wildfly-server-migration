/*
 * Copyright 2022 Red Hat, Inc.
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

package org.jboss.migration.wfly.task.security;

import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.jboss.JBossServerConfiguration;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.component.SimpleComponentTask;
import org.jboss.migration.core.util.xml.XMLFileProcessor;
import org.jboss.migration.core.util.xml.XMLFiles;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Iterator;

import static org.jboss.migration.core.task.component.TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet;

/**
 * @author emmartins
 */
public class ReadLegacySecurityConfigurationFromXML<S extends JBossServer<S>> implements ServerConfigurationMigration.XMLConfigurationSubtaskFactory<JBossServerConfiguration<S>> {

    public static final String TASK_NAME = "security.read-legacy-security-configuration";

    public static final String ALIAS = "alias";
    public static final String ALLOWED_USERS = "allowed-users";

    public static final String AUTHENTICATION = "authentication";
    public static final String AUTHORIZATION = "authorization";
    public static final String GENERATE_SELF_SIGNED_CERTIFICATE_HOST = "generate-self-signed-certificate-host";

    public static final String HTTP_INTERFACE = "http-interface";

    public static final String KEY_PASSWORD = "key-password";
    public static final String KEYSTORE = "keystore";
    public static final String KEYSTORE_PASSWORD = "keystore-password";
    public static final String LOCAL = "local";

    public static final String MANAGEMENT = "management";
    public static final String MANAGEMENT_INTERFACES = "management-interfaces";
    public static final String PATH = "path";
    public static final String PLAIN_TEXT = "plain-text";
    public static final String PROPERTIES = "properties";
    public static final String RELATIVE_TO = "relative-to";

    public static final String SECURITY_REALM = "security-realm";
    public static final String SECURITY_REALMS = "security-realms";
    public static final String SERVER_IDENTITIES = "server-identities";
    public static final String SSL = "ssl";

    private static final String DEFAULT_USER = "default-user";
    private static final String SKIP_GROUP_LOADING = "skip-group-loading";

    private final LegacySecurityConfigurations legacySecurityConfigurations;

    public ReadLegacySecurityConfigurationFromXML(LegacySecurityConfigurations legacySecurityConfigurations) {
        this.legacySecurityConfigurations = legacySecurityConfigurations;
    }

    @Override
    public ServerMigrationTask getTask(final JBossServerConfiguration<S> source, final JBossServerConfiguration targetConfigurationPath) {
        return new SimpleComponentTask.Builder()
                .name(TASK_NAME)
                .skipPolicy(skipIfDefaultTaskSkipPropertyIsSet())
                .runnable(context -> {
                    context.getLogger().debugf("Searching for legacy security XML configuration, not supported by the target server...");
                    final LegacySecurityConfiguration legacySecurityConfiguration = processXMLConfiguration(source, targetConfigurationPath, context);
                    ServerMigrationTaskResult taskResult = legacySecurityConfiguration != null ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
                    if (taskResult.getStatus() == ServerMigrationTaskResult.Status.SKIPPED) {
                        context.getLogger().debugf("No legacy security XML configuration found.");
                    } else {
                        legacySecurityConfigurations.getSecurityConfigurations().putIfAbsent(legacySecurityConfiguration.getTargetConfiguration().getPath().toString(), legacySecurityConfiguration);
                        context.getLogger().infof("Legacy security XML configuration found: "+legacySecurityConfiguration);
                    }
                    return taskResult;
                })
                .build();
    }

    protected LegacySecurityConfiguration processXMLConfiguration(final JBossServerConfiguration<S> source, final JBossServerConfiguration targetConfigurationPath, final TaskContext context) {
        final LegacySecurityConfiguration legacySecurityConfiguration = new LegacySecurityConfiguration(targetConfigurationPath);
        // setup and run the xml processor
        final XMLFileProcessor xmlFileProcessor = (startElement, xmlEventReader) -> {
            try {
                while (xmlEventReader.hasNext()) {
                    XMLEvent xmlEvent = xmlEventReader.nextEvent();
                    if (xmlEvent.isStartElement()) {
                        final StartElement element = xmlEvent.asStartElement();
                        final String elementLocalName = element.getName().getLocalPart();
                        if (elementLocalName.equals(MANAGEMENT)) {
                            processElementManagement(element, xmlEventReader, legacySecurityConfiguration);
                        } else {
                            // ignore element
                            System.out.println("Skipping unexpected element: "+elementLocalName);
                            skipElement(element, xmlEventReader);
                        }
                    } else if (xmlEvent.isEndElement()) {
                        break;
                    }
                }
            } catch (Throwable e) {
                throw new ServerMigrationFailureException(e);
            }
        };
        XMLFiles.process(targetConfigurationPath.getPath(), xmlFileProcessor);
        return legacySecurityConfiguration;
    }

    private void processElementManagement(StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityConfiguration legacySecurityConfiguration) throws XMLStreamException {
        System.out.println("Processing management...");
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals(SECURITY_REALMS)) {
                    processElementSecurityRealms(element, xmlEventReader, legacySecurityConfiguration);
                } else if (elementLocalName.equals(MANAGEMENT_INTERFACES)) {
                    processElementManagementInterfaces(element, xmlEventReader, legacySecurityConfiguration);
                } else {
                    // ignore element
                    System.out.println("Skipping unexpected child element of management: "+elementLocalName);
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
    }

    private void processElementManagementInterfaces(StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityConfiguration legacySecurityConfiguration) throws XMLStreamException {
        System.out.println("Processing management-interfaces...");
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals(HTTP_INTERFACE)) {
                    processElementHttpInterface(element, xmlEventReader, legacySecurityConfiguration);
                } else {
                    // ignore element
                    System.out.println("Skipping unexpected child element of management>management-interfaces: "+elementLocalName);
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
    }

    protected void processElementHttpInterface(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityConfiguration legacySecurityConfiguration) throws XMLStreamException {
        System.out.println("Processing http-interface...");
        Attribute securityRealmAttr = startElement.getAttributeByName(new QName("security-realm"));
        if (securityRealmAttr != null) {
            final LegacySecuredManagementInterface<S> securedManagementInterface = new LegacySecuredManagementInterface<>("http-interface", securityRealmAttr.getValue());
            legacySecurityConfiguration.getSecuredManagementInterfaces().add(securedManagementInterface);
            System.out.println("Http Interface secured by legacy security realm "+securedManagementInterface.getSecurityRealm()+" added to legacy configuration");
        }
        skipElement(startElement, xmlEventReader);
    }

    private void processElementSecurityRealms(StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityConfiguration legacySecurityConfiguration) throws XMLStreamException {
        System.out.println("Processing security-realms...");
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals(SECURITY_REALM)) {
                    processElementSecurityRealm(element, xmlEventReader, legacySecurityConfiguration);
                } else {
                    // ignore element
                    System.out.println("Skipping unexpected child element of management>security-realms: "+elementLocalName);
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
    }

    protected void processElementSecurityRealm(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityConfiguration legacySecurityConfiguration) throws XMLStreamException {
        System.out.println("Processing security realm start elemement "+startElement.getName());
        final String securityRealmName = startElement.getAttributeByName(new QName("name")).getValue();
        final LegacySecurityRealm securityRealm = new LegacySecurityRealm(securityRealmName);
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals(SERVER_IDENTITIES)) {
                    processElementServerIdentities(element, xmlEventReader, securityRealm);
                } else if (elementLocalName.equals(AUTHENTICATION)) {
                    processElementAuthentication(element, xmlEventReader, securityRealm);
                } else if (elementLocalName.equals(AUTHORIZATION)) {
                    processElementAuthorization(element, xmlEventReader, securityRealm);
                } else {
                    // ignore element
                    System.out.println("Skipping unexpected element children of security realm: "+elementLocalName);
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
        if (legacySecurityConfiguration.getLegacySecurityRealms().putIfAbsent(securityRealm.getName(), securityRealm) != null) {
            throw new IllegalStateException("security realm "+securityRealm+" already added to the legacy security configuration");
        }
    }

    protected void processElementServerIdentities(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityRealm securityRealm) throws XMLStreamException {
        LegacySecurityRealm.ServerIdentities serverIdentities = new LegacySecurityRealm.ServerIdentities();
        System.out.println("Processing server identities for security realm "+securityRealm);
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals(SSL)) {
                    processElementServerIdentitiesSSL(element, xmlEventReader, serverIdentities);
                } else {
                    // ignore element
                    System.out.println("Skipping unexpected element children of server identities: "+elementLocalName);
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
        securityRealm.setServerIdentities(serverIdentities);
    }

    protected void processElementServerIdentitiesSSL(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityRealm.ServerIdentities serverIdentities) throws XMLStreamException {
        System.out.println("Processing server identities ssl...");
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals(KEYSTORE)) {
                    processElementServerIdentitiesSSLKeystore(element, xmlEventReader, serverIdentities);
                } else {
                    // ignore element
                    System.out.println("Skipping unexpected element children of server identities ssl: "+elementLocalName);
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
    }

    protected void processElementServerIdentitiesSSLKeystore(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityRealm.ServerIdentities serverIdentities) throws XMLStreamException {
        System.out.println("Processing server identities ssl keystore...");
        final Iterator<Attribute> iterator = startElement.getAttributes();
        while(iterator.hasNext()) {
            Attribute attribute = iterator.next();
            if (attribute.getName().getLocalPart().equals(PATH)) {
                serverIdentities.setSslKeystorePath(attribute.getValue());
            } else if (attribute.getName().getLocalPart().equals(RELATIVE_TO)) {
                serverIdentities.setSslKeystoreRelativeTo(attribute.getValue());
            } else if (attribute.getName().getLocalPart().equals(KEYSTORE_PASSWORD)) {
                serverIdentities.setSslKeystoreKeystorePassword(attribute.getValue());
            } else if (attribute.getName().getLocalPart().equals(ALIAS)) {
                serverIdentities.setSslKeystoreAlias(attribute.getValue());
            } else if (attribute.getName().getLocalPart().equals(KEY_PASSWORD)) {
                serverIdentities.setSslKeystoreKeyPassword(attribute.getValue());
            } else if (attribute.getName().getLocalPart().equals(GENERATE_SELF_SIGNED_CERTIFICATE_HOST)) {
                serverIdentities.setSslKeystoreGenerateSelfSignedCertificateHost(attribute.getValue());
            } else {
                System.out.println("Skipping unexpected attribute of server identities ssl keystore: "+attribute.getName().getLocalPart());
            }
        }
        skipElement(startElement, xmlEventReader);
    }

    protected void processElementAuthentication(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityRealm securityRealm) throws XMLStreamException {
        LegacySecurityRealm.Authentication authentication = new LegacySecurityRealm.Authentication();
        System.out.println("Processing authentication for security realm "+securityRealm);
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals(LOCAL)) {
                    processElementLocal(element, xmlEventReader, authentication);
                } else if (elementLocalName.equals(PROPERTIES)) {
                    authentication.setProperties(processElementProperties(element, xmlEventReader));
                } else {
                    // ignore element
                    System.out.println("Skipping unexpected element children of authentication: "+elementLocalName);
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
        securityRealm.setAuthentication(authentication);
    }

    protected void processElementLocal(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityRealm.Authentication authentication) throws XMLStreamException {
        System.out.println("Processing authentication local...");
        LegacySecurityRealm.Local local = new LegacySecurityRealm.Local();
        final Iterator<Attribute> iterator = startElement.getAttributes();
        while(iterator.hasNext()) {
            Attribute attribute = iterator.next();
            if (attribute.getName().getLocalPart().equals(DEFAULT_USER)) {
                local.setDefaultUser(attribute.getValue());
            } else if (attribute.getName().getLocalPart().equals(SKIP_GROUP_LOADING)) {
                local.setSkipGroupLoading(Boolean.valueOf(attribute.getValue()));
            } else if (attribute.getName().getLocalPart().equals(ALLOWED_USERS)) {
                local.setAllowedUsers(attribute.getValue());
            } else {
                System.out.println("Skipping unexpected attribute of authentication local: "+attribute.getName().getLocalPart());
            }
        }
        skipElement(startElement, xmlEventReader);
        authentication.setLocal(local);
    }

    protected LegacySecurityRealm.Properties processElementProperties(final StartElement startElement, XMLEventReader xmlEventReader) throws XMLStreamException {
        System.out.println("Processing properties...");
        LegacySecurityRealm.Properties properties = new LegacySecurityRealm.Properties();
        final Iterator<Attribute> iterator = startElement.getAttributes();
        while(iterator.hasNext()) {
            Attribute attribute = iterator.next();
            if (attribute.getName().getLocalPart().equals(PATH)) {
                properties.setPath(attribute.getValue());
            } else if (attribute.getName().getLocalPart().equals(RELATIVE_TO)) {
                properties.setRelativeTo(attribute.getValue());
            } else if (attribute.getName().getLocalPart().equals(PLAIN_TEXT)) {
                properties.setPlainText(Boolean.valueOf(attribute.getValue()));
            } else {
                System.out.println("Skipping unexpected attribute of properties: "+attribute.getName().getLocalPart());
            }
        }
        skipElement(startElement, xmlEventReader);
        return properties;
    }

    protected void processElementAuthorization(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityRealm securityRealm) throws XMLStreamException {
        System.out.println("Processing authorization for security realm "+securityRealm);
        LegacySecurityRealm.Authorization authorization = new LegacySecurityRealm.Authorization();
        Attribute mapGroupsToRolesAttr = startElement.getAttributeByName(new QName("map-groups-to-roles"));
        if (mapGroupsToRolesAttr != null) {
            authorization.setMapGroupsToRoles(Boolean.valueOf(mapGroupsToRolesAttr.getValue()));
        }
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals(PROPERTIES)) {
                    authorization.setProperties(processElementProperties(element, xmlEventReader));
                } else {
                    // ignore element
                    System.out.println("Skipping unexpected element children of authorization: "+elementLocalName);
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
        securityRealm.setAuthorization(authorization);
    }

    protected void skipElement(StartElement startElement, XMLEventReader xmlEventReader) throws XMLStreamException {
        int endElementsLeft = 1;
        do {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                endElementsLeft++;
            }
            else if(xmlEvent.isEndElement()){
                endElementsLeft--;
            }
        } while (xmlEventReader.hasNext() && endElementsLeft > 0);
    }
}