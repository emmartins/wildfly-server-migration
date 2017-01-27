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
package org.jboss.migration.core.task;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The server migration task id.
 * @author emmartins
 */
public class ServerMigrationTaskName implements Serializable {

    private static final Map<String, String> NO_ATTRIBUTES = Collections.unmodifiableMap(new HashMap<String, String>());

    private final String name;
    private final Map<String, String> attributes;

    private ServerMigrationTaskName(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes != null ? Collections.unmodifiableMap(attributes) : NO_ATTRIBUTES;
    }

    /**
     * Retrieves the task id name.
     * @return the task id name
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the task id attributes.
     * @return the task id attributes
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (!attributes.isEmpty()) {
            sb.append("(");
            boolean first = true;
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(entry.getKey());
                sb.append('=');
                sb.append(entry.getValue());
            }
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * The task id builder.
     */
    public static class Builder {

        private final String name;
        private Map<String, String> attributes;

        /**
         * the task id name
         * @param name
         */
        public Builder(String name) {
            this.name = name;
        }

        /**
         * the task name to build from
         * @param taskName
         */
        public Builder(ServerMigrationTaskName taskName) {
            this(taskName.name);
            if (!taskName.getAttributes().isEmpty()) {
                attributes = new HashMap<>(taskName.getAttributes());
            }
        }

        /**
         * Adds a task id attribute
         * @param name the task id attribute name
         * @param value the task id attribute value
         * @return the builder
         */
        public Builder addAttribute(String name, String value) {
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            attributes.put(name, value);
            return this;
        }

        /**
         * Builds the task id.
         * @return the task id built
         */
        public ServerMigrationTaskName build() {
            return new ServerMigrationTaskName(name, attributes);
        }
    }
}
