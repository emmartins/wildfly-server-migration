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

import org.jboss.migration.core.ServerMigrationFailureException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The server migration task result.
 * @author emmartins
 */
public class ServerMigrationTaskResult {

    private static final Map<String, String> NO_ATTRIBUTES = Collections.unmodifiableMap(new HashMap<String, String>());

    /**
     * A result with success status and no attributes
     */
    public static final ServerMigrationTaskResult SUCCESS = new ServerMigrationTaskResult(Status.SUCCESS, null, null);

    /**
     * A result with skipped status and no attributes
     */
    public static final ServerMigrationTaskResult SKIPPED = new ServerMigrationTaskResult(Status.SKIPPED, null, null);

    /**
     * Constructs a fail result with the specified reason, and no attributes
     * @param failReason the fail reason
     * @return a fail result with the specified reason, and no attributes
     */
    public static ServerMigrationTaskResult fail(ServerMigrationFailureException failReason) {
        return new ServerMigrationTaskResult(Status.FAIL, failReason, null);
    }

    /**
     * The possible result status.
     */
    public enum Status { FAIL, SUCCESS, SKIPPED};

    private final Status status;
    private final ServerMigrationFailureException failReason;
    private final Map<String, String> attributes;

    private ServerMigrationTaskResult(final Status status, ServerMigrationFailureException failReason, Map<String, String> attributes) {
        this.status = status;
        if (status == null) {
            throw new IllegalArgumentException("null status");
        }
        this.failReason = failReason;
        this.attributes = attributes != null ? Collections.unmodifiableMap(attributes) : NO_ATTRIBUTES;
    }

    /**
     * Retrieves the result attributes.
     * @return the result attributes
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Retrieves the result status.
     * @return the result status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Retrieves the fail reason.
     * @return the fail reason
     */
    public ServerMigrationFailureException getFailReason() {
        return failReason;
    }

    @Override
    public String toString() {
        return status.toString();
    }

    /**
     * A builder for results with attributes.
     */
    public static class Builder {
        private Status status;
        private ServerMigrationFailureException failReason;
        private Map<String, String> attributes;

        /**
         * Sets the status as success.
         * @return the builder
         */
        public Builder success() {
            status = Status.SUCCESS;
            failReason = null;
            return this;
        }

        /**
         * Sets the status as skipped.
         * @return the builder
         */
        public Builder skipped() {
            status = Status.SKIPPED;
            failReason = null;
            return this;
        }

        /**
         * Sets the status as fail, with the specified reason.
         * @return the builder
         */
        public Builder fail(ServerMigrationFailureException failReason) {
            status = Status.FAIL;
            this.failReason = failReason;
            return this;
        }

        /**
         * Adds an attribute.
         * @param name
         * @param value
         * @return
         */
        public Builder addAttribute(String name, Object value) {
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            attributes.put(name, value.toString());
            return this;
        }

        /**
         * Builds the result.
         * @return the result built
         */
        public ServerMigrationTaskResult build() {
            return new ServerMigrationTaskResult(status, failReason, attributes);
        }
    }
}
