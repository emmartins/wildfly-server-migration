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

package org.jboss.migration.core.jboss;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.xml.stream.XMLStreamConstants.*;

/**
 * @author Eduardo Martins
 */
public class ModuleSpecification {

    private final ModuleIdentifier moduleIdentifier;
    private final List<Dependency> dependencies;

    protected ModuleSpecification(Builder builder) {
        this.moduleIdentifier = builder.moduleIdentifier;
        this.dependencies = Collections.unmodifiableList(builder.dependencies);
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public ModuleIdentifier getModuleIdentifier() {
        return moduleIdentifier;
    }

    public static class Parser {

        public static ModuleSpecification parse(Path inputFile) throws IOException, XMLStreamException {
            try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile.toFile()))) {
                return parse(in);
            }
        }

        public static ModuleSpecification parse(final InputStream in) throws IOException, XMLStreamException {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(in);
            reader.require(START_DOCUMENT, null, null);
            while (reader.hasNext()) {
                int type = reader.next();
                switch (type) {
                    case START_ELEMENT:
                        if (reader.getLocalName().equals("module")) {
                            return parseModule(reader);
                        }
                        else if (reader.getLocalName().equals("module-alias")) {
                            return parseModuleAlias(reader);
                        }
                        break;
                }
            }
            throw new XMLStreamException("expected XML elements not found");
        }

        private static ModuleSpecification parseModule(XMLStreamReader reader) throws XMLStreamException {
            final int count = reader.getAttributeCount();
            String name = null;
            String slot = "main";
            for (int i = 0; i < count; i++) {
                if("name".equals(reader.getAttributeName(i).getLocalPart())) {
                    name = reader.getAttributeValue(i);
                } else if("slot".equals(reader.getAttributeName(i).getLocalPart())) {
                    slot = reader.getAttributeValue(i);
                }
            }
            final Builder builder = new Builder(ModuleIdentifier.create(name, slot));
            while (reader.hasNext()) {
                int type = reader.next();
                switch (type) {
                    case START_ELEMENT:
                        if (reader.getLocalName().equals("dependencies")) {
                            parseDependencies(reader, builder);
                        }
                    case END_ELEMENT:
                        if (reader.getLocalName().equals("module")) {
                            return builder.build();
                        }
                }
            }
            throw new IllegalStateException("Unexpected end of module.xml");
        }

        private static ModuleSpecification parseModuleAlias(XMLStreamReader reader) throws XMLStreamException {
            String targetName = "";
            String targetSlot = "main";
            String name = null;
            String slot = "main";
            boolean optional = false;
            for (int i = 0 ; i < reader.getAttributeCount() ; i++) {
                String localName = reader.getAttributeLocalName(i);
                if (localName.equals("target-name")) {
                    targetName = reader.getAttributeValue(i);
                } else if (localName.equals("target-slot")) {
                    targetSlot = reader.getAttributeValue(i);
                } else if (localName.equals("name")) {
                    name = reader.getAttributeValue(i);
                } else if (localName.equals("slot")) {
                    slot = reader.getAttributeValue(i);
                }
            }
            return new Builder(ModuleIdentifier.create(name, slot)).
                    dependency(new Dependency(ModuleIdentifier.create(targetName, targetSlot), optional))
                    .build();
        }

        private static void parseDependencies(XMLStreamReader reader, Builder builder) throws XMLStreamException {
            while (reader.hasNext()) {
                int type = reader.next();
                switch (type) {
                    case START_ELEMENT:
                        if (reader.getLocalName().equals("module")) {
                            String name = "";
                            String slot = "main";
                            boolean optional = false;
                            for (int i = 0 ; i < reader.getAttributeCount() ; i++) {
                                String localName = reader.getAttributeLocalName(i);
                                if (localName.equals("name")) {
                                    name = reader.getAttributeValue(i);
                                } else if (localName.equals("slot")) {
                                    slot = reader.getAttributeValue(i);
                                } else if (localName.equals("optional")) {
                                    optional = Boolean.parseBoolean(reader.getAttributeValue(i));
                                }
                            }
                            final ModuleIdentifier dependencyId = ModuleIdentifier.create(name, slot);
                            builder.dependency(new Dependency(dependencyId, optional));
                        }
                        break;
                    case END_ELEMENT:
                        if (reader.getLocalName().equals("dependencies")) {
                            return;
                        }
                }
            }
        }
    }

    public static class Dependency {
        private final ModuleIdentifier moduleId;
        private final boolean optional;

        public Dependency(ModuleIdentifier moduleId, boolean optional) {
            this.moduleId = moduleId;
            this.optional = optional;
        }

        public ModuleIdentifier getId() {
            return moduleId;
        }

        public boolean isOptional() {
            return optional;
        }

        @Override
        public String toString() {
            return "[" + moduleId + (optional ? ",optional=true" : "") + "]";
        }
    }

    public static class Builder {

        private final ModuleIdentifier moduleIdentifier;
        private final List<Dependency> dependencies;

        public Builder(ModuleIdentifier moduleIdentifier) {
            this.moduleIdentifier = moduleIdentifier;
            this.dependencies = new ArrayList<>();
        }

        public Builder dependency(Dependency dependency) {
            dependencies.add(dependency);
            return this;
        }

        public ModuleSpecification build() {
            return new ModuleSpecification(this);
        }
    }
}
