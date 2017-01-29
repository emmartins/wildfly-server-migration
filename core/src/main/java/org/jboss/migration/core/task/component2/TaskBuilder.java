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

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.TaskContext;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author emmartins
 */
public interface TaskBuilder<P extends TaskBuilder.Params, T extends TaskBuilder<P, T>> {

    default T name(ServerMigrationTaskName name) {
        return name(factory -> name);
    }

    T name(NameFactory<? super P> nameFactory);

    default <Q extends Params> T name(ParamsConverter<P, Q> paramsConverter, NameFactory<? super Q> qNameFactory) {
        final NameFactory<P> pNameFactory = params -> qNameFactory.newInstance(paramsConverter.apply(params));
        return name(pNameFactory);
    }

    T skipPolicy(SkipPolicy<? super P> skipPolicy);

    default <Q extends Params> T skipPolicy(ParamsConverter<P, Q> paramsConverter, SkipPolicy<? super Q> q) {
        final SkipPolicy<P> p = (params, name, context) -> q.isSkipped(paramsConverter.apply(params), name, context);
        return skipPolicy(p);
    }

    T beforeRun(BeforeRun<? super P> beforeRun);

    default <Q extends Params> T beforeRun(ParamsConverter<P, Q> paramsConverter, BeforeRun<? super Q> q) {
        final BeforeRun<P> p = (params, name, context) -> q.beforeRun(paramsConverter.apply(params), name, context);
        return beforeRun(p);
    }

    default T run(TaskRunnable runnable) {
        final RunnableFactory<P> runnableFactory = params -> runnable;
        return run(runnableFactory);
    }

    T run(RunnableFactory<? super P> runnableFactory);

    default <Q extends Params> T run(ParamsConverter<P, Q> paramsConverter, RunnableFactory<? super Q> q) {
        final RunnableFactory<P> p = params -> q.newInstance(paramsConverter.apply(params));
        return run(p);
    }

    T afterRun(AfterRun<? super P> afterRun);

    default <Q extends Params> T afterRun(ParamsConverter<P, Q> paramsConverter, AfterRun<? super Q> q) {
        final AfterRun<P> p = (params, name, context) -> q.afterRun(paramsConverter.apply(params), name, context);
        return afterRun(p);
    }

    T clone();

    <Q extends P> ServerMigrationTask build(Q params) throws Exception;

    /**
     *
     */
    interface Params {
        Params NONE = new Params() {};
    }

    /**
     * Adapted from {@link Function}, to have {@link Function#apply(Object)} throwing exceptions.
     * @param <T>
     * @param <R>
     */
    @FunctionalInterface
    interface ParamsConverter<T, R extends Params> {

        /**
         * @see Function#apply(Object)
         */
        R apply(T t) throws Exception;

        /**
         * @see Function#compose(Function)
         */
        default <V extends Params> ParamsConverter<V, R> compose(ParamsConverter<? super V, ? extends T> before) {
            Objects.requireNonNull(before);
            return (V v) -> apply(before.apply(v));
        }

        /**
         * @see Function#andThen(Function)
         */
        default <V extends Params> ParamsConverter<T, V> andThen(ParamsConverter<? super R, ? extends V> after) {
            Objects.requireNonNull(after);
            return (T t) -> after.apply(apply(t));
        }

        /**
         * @see Function#identity()
         */
        static <T extends Params> ParamsConverter<T, T> identity() {
            return t -> t;
        }
    }

    /**
     *
     * @param <P>
     */
    @FunctionalInterface
    interface NameFactory<P extends Params> {
        ServerMigrationTaskName newInstance(P params) throws Exception;
    }

    /**
     *
     */
    @FunctionalInterface
    interface SkipPolicy<P extends Params> {
        boolean isSkipped(P params, ServerMigrationTaskName taskName, TaskContext context) throws Exception;
    }

    /**
     *
     * @param <P>
     */
    @FunctionalInterface
    interface BeforeRun<P extends Params> {
        void beforeRun(P params, ServerMigrationTaskName taskName, TaskContext context) throws Exception;
    }

    /**
     *
     * @param <P>
     */
    @FunctionalInterface
    interface RunnableFactory<P extends Params> {
        TaskRunnable newInstance(P params) throws Exception;
    }

    /**
     *
     * @param <P>
     */
    @FunctionalInterface
    interface AfterRun<P extends Params> {
        void afterRun(P params, ServerMigrationTaskName taskName, TaskContext context) throws Exception;
    }
}
