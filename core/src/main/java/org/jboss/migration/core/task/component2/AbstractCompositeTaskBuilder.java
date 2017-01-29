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

import org.jboss.migration.core.task.ServerMigrationTaskResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public abstract class AbstractCompositeTaskBuilder<P extends TaskBuilder.Params, T extends AbstractCompositeTaskBuilder<P, T>> extends AbstractTaskBuilder<P, T> implements CompositeTaskBuilder<P,T> {

    private final List<RunnableFactory<? super P>> runnableFactories = new ArrayList<>();

    protected AbstractCompositeTaskBuilder() {
    }

    public AbstractCompositeTaskBuilder(AbstractCompositeTaskBuilder<P, ?> other) {
        super(other);
        this.runnableFactories.addAll(other.runnableFactories);
    }

    @Override
    public T run(RunnableFactory<? super P> runnableFactory) {
        this.runnableFactories.add(runnableFactory);
        return getThis();
    }

    @Override
    public RunnableFactory<? super P> getRunnableFactory() {
        final List<RunnableFactory<? super P>> runnableFactoriesCopy = new ArrayList<>(this.runnableFactories);
        final RunnableFactory<P> compositeRunnableFactory = params -> (name, context) -> {
            for (RunnableFactory<? super P> runnableFactory : runnableFactoriesCopy) {
                runnableFactory.newInstance(params).run(name, context);
            }
            return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
        };
        return compositeRunnableFactory;
    }
}