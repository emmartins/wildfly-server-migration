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
import java.util.List;
import java.util.function.Predicate;

/**
 * @author emmartins
 */
public interface ManageableNode<T extends ManageableNode> {

    Class<T> getType();
    <C extends ManageableNode> List<C> findChildren(Select<C> select) throws IOException;
    <C extends ManageableNode> List<C> findChildren(Query<C> query) throws IOException;

    interface Query<T extends ManageableNode> {
        Select<T> getChildSelector();
        Query<?> getParentsQuery();
    }

    interface Select<T extends ManageableNode> extends Predicate<T> {
        Class<T> getType();
    }
}
