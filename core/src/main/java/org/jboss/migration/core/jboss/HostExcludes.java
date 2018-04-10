/*
 * Copyright 2018 Red Hat, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author emmartins
 */
public class HostExcludes {

    private final List<HostExclude> hostExcludes;

    protected HostExcludes(Builder builder) {
        this.hostExcludes = Collections.unmodifiableList(builder.hostExcludes);
    }

    public List<HostExclude> getHostExcludes() {
        return hostExcludes;
    }

    @Override
    public String toString() {
        return getHostExcludes().toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     *
     */
    public static class Builder {

        private final List<HostExclude> hostExcludes;

        protected Builder() {
            hostExcludes = new ArrayList<>();
        }

        public Builder hostExclude(HostExclude hostExclude) {
            this.hostExcludes.add(hostExclude);
            return this;
        }

        public Builder hostExclude(HostExclude.Builder hostExcludeBuilder) {
            return hostExclude(hostExcludeBuilder.build());
        }

        public HostExcludes build() {
            return new HostExcludes(this);
        }
    }

    /**
     *
     */
    public static class ApiVersion {

        private final String majorVersion;
        private final String minorVersion;

        protected ApiVersion(String majorVersion, String minorVersion) {
            this.majorVersion = Objects.requireNonNull(majorVersion);
            this.minorVersion = Objects.requireNonNull(minorVersion);
        }

        public String getMajorVersion() {
            return majorVersion;
        }

        public String getMinorVersion() {
            return minorVersion;
        }

        @Override
        public String toString() {
            return getMajorVersion()+"."+getMinorVersion();
        }
    }

    /**
     *
     */
    public static class Release {

        private final String id;

        protected Release(String id) {
            this.id = Objects.requireNonNull(id);
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return getId();
        }
    }

    /**
     *
     */
    public static class ExcludedExtension {

        private final String module;

        protected ExcludedExtension(String module) {
            this.module = Objects.requireNonNull(module);
        }

        public String getModule() {
            return module;
        }

        @Override
        public String toString() {
            return getModule();
        }
    }
}
