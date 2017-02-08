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

package org.jboss.migration.wfly10.config.management;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * @author emmartins
 */
public class ManageableResourceType {

    private final Class<? extends ManageableResource> type;
    private final Set<ManageableResourceType> childTypes;
    private volatile Set<ManageableResourceType> descendantTypes;

    protected ManageableResourceType(Class<? extends ManageableResource> type) {
        Objects.requireNonNull(type);
        this.type = type;
        this.childTypes = new HashSet<>();
    }

    protected ManageableResourceType(Class<? extends ManageableResource> type, ManageableResourceType... childTypes) {
        this(type);
        for (ManageableResourceType childType : childTypes) {
            addChildType(childType);
        }
    }

    protected void addChildType(ManageableResourceType childType) {
        this.childTypes.add(childType);
        this.descendantTypes = null;
    }

    public Class<? extends ManageableResource> getType() {
        return type;
    }

    public Set<ManageableResourceType> getChildTypes() {
        return Collections.unmodifiableSet(childTypes);
    }

    public Set<ManageableResourceType> getDescendantTypes() {
        if (descendantTypes == null) {
            descendantTypes = findDescendantTypes();
        }
        return descendantTypes;
    }

    private synchronized Set<ManageableResourceType> findDescendantTypes() {
        return childTypes.stream().flatMap(childType -> Stream.concat(Stream.of(childType), childType.getDescendantTypes().stream()))
                .collect(toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ManageableResourceType that = (ManageableResourceType) o;

        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
