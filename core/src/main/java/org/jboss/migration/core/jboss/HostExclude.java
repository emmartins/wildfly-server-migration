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
public class HostExclude {

    public static Builder builder() {
        return new Builder();
    }

    private final String name;
    private final ApiVersion apiVersion;
    private final Release release;
    private final List<ExcludedExtension> excludedExtensions;

    protected HostExclude(Builder builder) {
        this.name = Objects.requireNonNull(builder.name);
        this.apiVersion = builder.apiVersion;
        this.release = builder.release;
        this.excludedExtensions = Collections.unmodifiableList(builder.excludedExtensions);
    }

    public String getName() {
        return name;
    }

    public ApiVersion getApiVersion() {
        return apiVersion;
    }

    public Release getRelease() {
        return release;
    }

    public List<ExcludedExtension> getExcludedExtensions() {
        return excludedExtensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HostExclude that = (HostExclude) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "HostExclude{ name="+name+", apiVersion="+String.valueOf(apiVersion)+", release="+String.valueOf(release)+", excluded-extensions="+excludedExtensions+" }";
    }

    /**
     *
     */
    public static class Builder {

        private String name;
        private ApiVersion apiVersion;
        private Release release;
        private final List<ExcludedExtension> excludedExtensions;

        protected Builder() {
            excludedExtensions = new ArrayList<>();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder apiVersion(String majorVersion, String minorVersion) {
            return apiVersion(majorVersion, minorVersion, null);
        }

        public Builder apiVersion(String majorVersion, String minorVersion, String microVersion) {
            this.apiVersion = new ApiVersion(majorVersion, minorVersion, microVersion);
            return this;
        }

        public Builder release(String id) {
            this.release = new Release(id);
            return this;
        }

        public Builder excludedExtension(String module) {
            this.excludedExtensions.add(new ExcludedExtension(module));
            return this;
        }

        public HostExclude build() {
            return new HostExclude(this);
        }
    }

    /**
     *
     */
    public static class ApiVersion {

        private final String majorVersion;
        private final String microVersion;
        private final String minorVersion;

        protected ApiVersion(String majorVersion, String minorVersion, String microVersion) {
            this.majorVersion = Objects.requireNonNull(majorVersion);
            this.minorVersion = Objects.requireNonNull(minorVersion);
            this.microVersion = microVersion;
        }

        public String getMajorVersion() {
            return majorVersion;
        }

        public String getMicroVersion() {
            return microVersion;
        }

        public String getMinorVersion() {
            return minorVersion;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder(getMajorVersion()).append('.').append(getMinorVersion());
            if (microVersion != null) {
                sb.append('.').append(getMicroVersion());
            }
            return sb.toString();
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
