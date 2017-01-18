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

package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.migration.wfly10.config.management.ManageableResources;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author emmartins
 */
public class ManageableResourcesFinder {

    interface TypeFinder<T extends ManageableResources> {
        Class<T> getType();
        List<T> findResources();
    }

    private final Map<Class, TypeFinder> typeFinderMap;

    public ManageableResourcesFinder(List<TypeFinder> typeFinders) {
        typeFinderMap = new HashMap<>();
        for (TypeFinder typeFinder : typeFinders) {
            typeFinderMap.put(typeFinder.getType(), typeFinder);
        }
    }

    public <T extends ManageableResources> List<T> findResources(Class<T> type) {
        final TypeFinder<T> typeFinder = typeFinderMap.get(type);
        return typeFinder != null ? typeFinder.findResources() : Collections.emptyList();
    }
}
