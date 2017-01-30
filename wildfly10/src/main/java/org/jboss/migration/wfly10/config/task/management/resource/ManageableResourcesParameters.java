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

package org.jboss.migration.wfly10.config.task.management.resource;

import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.management.ServerConfigurationBuildParameters;

import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.toSet;

/**
 * @author emmartins
 */
public interface ManageableResourcesParameters {

    interface Single<S, R extends ManageableResource> extends ServerConfigurationBuildParameters<S> {

        R getResource();

        static <S, T extends ManageableResource, R extends ManageableResource> Mapper<? extends Multiple<S, T>, Single<S, R>> mapperFrom(ManageableResourceSelector<R> selector) {
            return (Mapper<Multiple<S, T>, Single<S, R>>) tParams -> {
                final S rSource = tParams.getSource();
                final ManageableServerConfiguration rServerConfiguration = tParams.getServerConfiguration();
                final Collection<R> rCollection = selector.fromResources(tParams.getResources());
                return rCollection.stream()
                        .map(rResource -> new Single<S, R>() {
                            @Override
                            public R getResource() {
                                return rResource;
                            }

                            @Override
                            public S getSource() {
                                return rSource;
                            }

                            @Override
                            public ManageableServerConfiguration getServerConfiguration() {
                                return rServerConfiguration;
                            }
                        })
                        .collect(toSet());
            };
        }
    }

    interface Multiple<S, R extends ManageableResource> extends ServerConfigurationBuildParameters<S> {

        Collection<? extends R> getResources();

        static <S, T extends ManageableResource, R extends ManageableResource> Mapper<Multiple<S, T>, Multiple<S, R>> mapperFrom(ManageableResourceSelector<R> rSelector) {
            return tParams -> {
                final S rSource = tParams.getSource();
                final ManageableServerConfiguration rServerConfiguration = tParams.getServerConfiguration();
                final Collection<R> rCollection = rSelector.fromResources(tParams.getResources());
                return Collections.singleton(new Multiple<S, R>() {
                    @Override
                    public S getSource() {
                        return rSource;
                    }

                    @Override
                    public ManageableServerConfiguration getServerConfiguration() {
                        return rServerConfiguration;
                    }

                    @Override
                    public Collection<? extends R> getResources() {
                        return rCollection;
                    }
                });
            };
        }
    }
}
