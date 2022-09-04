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

    private static final String SECURITY_REALMS = "security-realms";
    private static final String SECURITY_REALM = "security-realm";
    private static final String HTTP_INTERFACE = "http-interface";
    private static final String REMOTE = "remote";

    private final LegacySecurityConfigurations<S> legacySecurityConfigurations;

    public ReadLegacySecurityConfigurationFromXML(LegacySecurityConfigurations<S> legacySecurityConfigurations) {
        this.legacySecurityConfigurations = legacySecurityConfigurations;
    }

    @Override
    public ServerMigrationTask getTask(final JBossServerConfiguration<S> source, final JBossServerConfiguration targetConfigurationPath) {
        return new SimpleComponentTask.Builder()
                .name(TASK_NAME)
                .skipPolicy(skipIfDefaultTaskSkipPropertyIsSet())
                .runnable(context -> {
                    context.getLogger().debugf("Searching for legacy security XML configuration, not supported by the target server...");
                    final LegacySecurityConfiguration<S> legacySecurityConfiguration = processXMLConfiguration(source, targetConfigurationPath, context);
                    ServerMigrationTaskResult taskResult = legacySecurityConfiguration != null ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
                    if (taskResult.getStatus() == ServerMigrationTaskResult.Status.SKIPPED) {
                        context.getLogger().debugf("No legacy security XML configuration found.");
                    } else {
                        legacySecurityConfigurations.securityConfigurations.putIfAbsent(legacySecurityConfiguration.sourceConfiguration, legacySecurityConfiguration);
                        context.getLogger().infof("Legacy security XML configuration found: "+legacySecurityConfiguration);
                    }
                    return taskResult;
                })
                .build();
    }

    protected LegacySecurityConfiguration<S> processXMLConfiguration(final JBossServerConfiguration<S> source, final JBossServerConfiguration targetConfigurationPath, final TaskContext context) {
        final LegacySecurityConfiguration<S> legacySecurityConfiguration = new LegacySecurityConfiguration<>();
        legacySecurityConfiguration.sourceConfiguration = source;
        legacySecurityConfiguration.targetConfiguration = targetConfigurationPath;
        // setup and run the xml processor
        final XMLFileProcessor xmlFileProcessor = (startElement, xmlEventReader) -> {
            try {
                while (xmlEventReader.hasNext()) {
                    XMLEvent xmlEvent = xmlEventReader.nextEvent();
                    if (xmlEvent.isStartElement()) {
                        final StartElement element = xmlEvent.asStartElement();
                        final String elementLocalName = element.getName().getLocalPart();
                        if (elementLocalName.equals("management")) {
                            processElementManagement(element, xmlEventReader, legacySecurityConfiguration);
                        } else {
                            // ignore element
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

    private void processElementManagement(StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityConfiguration<S> legacySecurityConfiguration) throws XMLStreamException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals(SECURITY_REALMS)) {
                    processElementSecurityRealms(element, xmlEventReader, legacySecurityConfiguration);
                } else if (elementLocalName.equals("management-interfaces")) {
                    processElementManagementInterfaces(element, xmlEventReader, legacySecurityConfiguration);
                } else {
                    // ignore element
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
    }

    private void processElementManagementInterfaces(StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityConfiguration<S> legacySecurityConfiguration) throws XMLStreamException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals(HTTP_INTERFACE)) {
                    processElementHttpInterface(element, xmlEventReader, legacySecurityConfiguration);
                } else {
                    // ignore element
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
    }

    protected void processElementHttpInterface(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityConfiguration<S> legacySecurityConfiguration) throws XMLStreamException {
        Attribute securityRealmAttr = startElement.getAttributeByName(new QName(startElement.getName().getNamespaceURI(),"security-realm"));
        if (securityRealmAttr != null) {
            final LegacySecuredManagementInterface<S> securedManagementInterface = new LegacySecuredManagementInterface<>("http-interface", securityRealmAttr.getValue());
            legacySecurityConfiguration.securedManagementInterfaces.add(securedManagementInterface);
        }
        skipElement(startElement, xmlEventReader);
    }

    private void processElementSecurityRealms(StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityConfiguration<S> legacySecurityConfiguration) throws XMLStreamException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals(SECURITY_REALM)) {
                    processElementSecurityRealm(element, xmlEventReader, legacySecurityConfiguration);
                } else {
                    // ignore element
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
    }

    protected void processElementSecurityRealm(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityConfiguration<S> legacySecurityConfiguration) throws XMLStreamException {
        final LegacySecurityRealm securityRealm = new LegacySecurityRealm();
        securityRealm.name = startElement.getAttributeByName(new QName(startElement.getName().getNamespaceURI(),"name")).getValue();
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals("server-identities")) {
                    processElementServerIdentities(element, xmlEventReader, securityRealm);
                } else if (elementLocalName.equals("authentication")) {
                    processElementAuthentication(element, xmlEventReader, securityRealm);
                } else if (elementLocalName.equals("authorization")) {
                    processElementAuthorization(element, xmlEventReader, securityRealm);
                } else {
                    // ignore element
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
        if (legacySecurityConfiguration.legacySecurityRealms.putIfAbsent(securityRealm.name, securityRealm) != null) {
            throw new IllegalStateException("security realm "+securityRealm+" already added to the legacy security configuration");
        }
    }

    protected void processElementServerIdentities(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityRealm securityRealm) throws XMLStreamException {
        LegacySecurityRealm.ServerIdentities serverIdentities = new LegacySecurityRealm.ServerIdentities();
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals("ssl")) {
                    processElementServerIdentitiesSSL(element, xmlEventReader, serverIdentities);
                } else {
                    // ignore element
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
        securityRealm.serverIdentities = serverIdentities;
    }

    protected void processElementServerIdentitiesSSL(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityRealm.ServerIdentities serverIdentities) throws XMLStreamException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals("keystore")) {
                    processElementServerIdentitiesSSLKeystore(element, xmlEventReader, serverIdentities);
                } else {
                    // ignore element
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
    }

    protected void processElementServerIdentitiesSSLKeystore(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityRealm.ServerIdentities serverIdentities) throws XMLStreamException {
        final Iterator<Attribute> iterator = startElement.getAttributes();
        while(iterator.hasNext()) {
            Attribute attribute = iterator.next();
            if (attribute.getName().getLocalPart().equals("path")) {
                serverIdentities.sslKeystorePath = attribute.getValue();
            } else if (attribute.getName().getLocalPart().equals("relative-to")) {
                serverIdentities.sslKeystoreRelativeTo = attribute.getValue();
            } else if (attribute.getName().getLocalPart().equals("keystore-password")) {
                serverIdentities.sslKeystoreKeystorePassword = attribute.getValue();
            } else if (attribute.getName().getLocalPart().equals("alias")) {
                serverIdentities.sslKeystoreAlias = attribute.getValue();
            } else if (attribute.getName().getLocalPart().equals("key-password")) {
                serverIdentities.sslKeystoreKeyPassword = attribute.getValue();
            } else if (attribute.getName().getLocalPart().equals("generate-self-signed-certificate-host")) {
                serverIdentities.sslKeystoreGenerateSelfSignedCertificateHost = attribute.getValue();
            }
        }
        skipElement(startElement, xmlEventReader);
    }

    protected void processElementAuthentication(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityRealm securityRealm) throws XMLStreamException {
        LegacySecurityRealm.Authentication authentication = new LegacySecurityRealm.Authentication();
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals("local")) {
                    processElementLocal(element, xmlEventReader, authentication);
                } else if (elementLocalName.equals("properties")) {
                    authentication.properties = processElementProperties(element, xmlEventReader);
                } else {
                    // ignore element
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
        securityRealm.authentication = authentication;
    }

    protected void processElementLocal(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityRealm.Authentication authentication) throws XMLStreamException {
        LegacySecurityRealm.Local local = new LegacySecurityRealm.Local();
        final Iterator<Attribute> iterator = startElement.getAttributes();
        while(iterator.hasNext()) {
            Attribute attribute = iterator.next();
            if (attribute.getName().getLocalPart().equals("default-user")) {
                local.defaultUser = attribute.getValue();
            } else if (attribute.getName().getLocalPart().equals("skip-group-loading")) {
                local.skipGroupLoading = Boolean.valueOf(attribute.getValue());
            } else if (attribute.getName().getLocalPart().equals("allowed-users")) {
                local.allowedUsers = attribute.getValue();
            }
        }
        skipElement(startElement, xmlEventReader);
        authentication.local = local;
    }

    protected LegacySecurityRealm.Properties processElementProperties(final StartElement startElement, XMLEventReader xmlEventReader) throws XMLStreamException {
        LegacySecurityRealm.Properties properties = new LegacySecurityRealm.Properties();
        final Iterator<Attribute> iterator = startElement.getAttributes();
        while(iterator.hasNext()) {
            Attribute attribute = iterator.next();
            if (attribute.getName().getLocalPart().equals("path")) {
                properties.path = attribute.getValue();
            } else if (attribute.getName().getLocalPart().equals("relative-to")) {
                properties.relativeTo = attribute.getValue();
            } else if (attribute.getName().getLocalPart().equals("plain-text")) {
                properties.plainText = Boolean.valueOf(attribute.getValue());
            }
        }
        skipElement(startElement, xmlEventReader);
        return properties;
    }

    protected void processElementAuthorization(final StartElement startElement, XMLEventReader xmlEventReader, LegacySecurityRealm securityRealm) throws XMLStreamException {
        LegacySecurityRealm.Authorization authorization = new LegacySecurityRealm.Authorization();
        Attribute mapGroupsToRolesAttr = startElement.getAttributeByName(new QName(startElement.getName().getNamespaceURI(),"map-groups-to-roles"));
        if (mapGroupsToRolesAttr != null) {
            authorization.mapGroupsToRoles = Boolean.valueOf(mapGroupsToRolesAttr.getValue());
        }
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement element = xmlEvent.asStartElement();
                final String elementLocalName = element.getName().getLocalPart();
                if (elementLocalName.equals("properties")) {
                    authorization.properties = processElementProperties(element, xmlEventReader);
                } else {
                    // ignore element
                    skipElement(element, xmlEventReader);
                }
            } else if (xmlEvent.isEndElement()) {
                break;
            }
        }
        securityRealm.authorization = authorization;
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