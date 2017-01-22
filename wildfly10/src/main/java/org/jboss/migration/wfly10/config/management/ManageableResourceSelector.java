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

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a function that selects {@link ManageableResource}s resource(s).
 * Code adapted from {@link java.util.function.Function}.
 *
 * @param <R> the type of {@link ManageableResource}s returned by the selector
 * @author emmartins
 */
@FunctionalInterface
public interface ManageableResourceSelector<R extends ManageableResource> {

    Set<R> collect(ManageableResource resource) throws IOException;

    default <I extends ManageableResource> Set<R> collect(I... resources) throws IOException {
        Set<R> result = new HashSet<>();
        for (ManageableResource resource : resources) {
            result.addAll(collect(resource));
        }
        return result;
    }

    default <I extends ManageableResource> Set<R> collect(Collection<I> resources) throws IOException {
        return collect(resources.stream().toArray(ManageableResource[]::new));
    }

    /**
     * Returns a composed selector that first applies the {@code before}
     * selector to its input, and then applies this selector to the result.
     *
     * @param before the selector to apply before this selector is applied
     * @return a composed selector that first applies the {@code before}
     * selector, and then applies this selector
     * @throws NullPointerException if before is null
     * @see #andThen(ManageableResourceSelector)
     */
    default ManageableResourceSelector<R> compose(ManageableResourceSelector<?> before) {
        Objects.requireNonNull(before);
        return resource -> collect(before.collect(resource));
    }

    /**
     * Returns a composed selector that first applies this selector to
     * its input, and then applies the {@code after} selector to the result.
     *
     * @param <R1>   the type of output of the {@code after} function, and of the
     *              composed function
     * @param after the selector to apply the result from this selector
     * @return a composed selector that first applies this selector and then
     * applies the {@code after} selector
     * @throws NullPointerException if after is null
     * @see #compose(ManageableResourceSelector)
     */
    default <R1 extends ManageableResource> ManageableResourceSelector<R1> andThen(ManageableResourceSelector<R1> after) {
        Objects.requireNonNull(after);
        return resource -> after.collect(collect(resource));
    }
}
