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

package org.jboss.migration.core.task.component2;

/**
 * @author emmartins
 */
public abstract class AbstractLeafTaskBuilder<P extends TaskBuilder.Params, T extends AbstractLeafTaskBuilder<P, T>> extends AbstractTaskBuilder<P, T> {

    private RunnableFactory<? super P> runnableFactory;

    protected AbstractLeafTaskBuilder() {
    }

    protected AbstractLeafTaskBuilder(AbstractLeafTaskBuilder<P, ?> other) {
        super(other);
        this.runnableFactory = other.runnableFactory;
    }

    @Override
    public T run(RunnableFactory<? super P> runnableFactory) {
        this.runnableFactory = runnableFactory;
        return getThis();
    }

    @Override
    public RunnableFactory<? super P> getRunnableFactory() {
        return runnableFactory;
    }
}
