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

/**
 * @author emmartins
 */
public class Subsystem {

    private final String name;
    private final String namespaceWithoutVersion;
    private final Extension extension;

    public Subsystem(Builder builder) {
        this.name = builder.name;
        this.namespaceWithoutVersion = builder.namespaceWithoutVersion == null ?("urn:jboss:domain:"+name) : builder.namespaceWithoutVersion;
        this.extension = builder.extension;
    }

    public Extension getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }

    public String getNamespaceWithoutVersion() {
        return namespaceWithoutVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Subsystem subsystem = (Subsystem) o;
        return name.equals(subsystem.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract static class Builder<T extends Builder<T>> {

        private String name;
        private String namespaceWithoutVersion;
        private Extension extension;

        protected abstract T getThis();

        public T extension(Extension extension) {
            this.extension = extension;
            return getThis();
        }

        public T name(String name) {
            this.name = name;
            return getThis();
        }

        public T namespaceWithoutVersion(String namespaceWithoutVersion) {
            this.namespaceWithoutVersion = namespaceWithoutVersion;
            return getThis();
        }

        public Subsystem build() {
            return new Subsystem(this);
        }
    }

    private static class DefaultBuilder extends Builder<DefaultBuilder> {
        @Override
        protected DefaultBuilder getThis() {
            return this;
        }
    }

    public static Builder builder() {
        return new DefaultBuilder();
    }
}
