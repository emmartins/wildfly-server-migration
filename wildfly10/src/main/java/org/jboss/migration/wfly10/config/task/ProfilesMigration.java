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

package org.jboss.migration.wfly10.config.task;

import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.wfly10.config.management.ProfilesManagement;

/**
 * @author emmartins
 */
public class ProfilesMigration<S> extends ResourcesMigration<S, ProfilesManagement> {

    public static final String PROFILES = "profiles";

    protected ProfilesMigration(Builder<S> builder) {
        super(builder);
    }

    public interface SubtaskFactory<S> extends ResourcesMigration.SubtaskFactory<S, ProfilesManagement> {
    }

    public static class Builder<S> extends ResourcesMigration.Builder<Builder<S>, S, ProfilesManagement> {

        public Builder() {
            super(PROFILES);
            eventListener(new ResourcesMigration.EventListener() {
                @Override
                public void started(ServerMigrationTaskContext context) {
                    context.getLogger().infof("Profiles migration starting...");
                }
                @Override
                public void done(ServerMigrationTaskContext context) {
                    context.getLogger().infof("Profiles migration done.");
                }
            });
        }

        @Override
        public ProfilesMigration<S> build() {
            return new ProfilesMigration(this);
        }

        public Builder<S> profileMigration(ProfileMigration<S> profileMigration) {
            return addSubtaskFactory(profileMigration);
        }
    }

    public static <S> ProfilesMigration<S> from(ProfileMigration<S> profileMigration) {
        return new Builder<S>().profileMigration(profileMigration).build();
    }
}
