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
package org.jboss.migration.cli;

import org.jboss.migration.cli.commonscli.Option;
import org.jboss.migration.cli.commonscli.Options;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ingo Weiss
 * @author emmartins
 */
public class CommandLineOptions {

    private final Options nonDeprecatedOptions;
    private final Options deprecatedOptions;
    private final Options allOptions;

    protected CommandLineOptions(Builder builder) {
        this.allOptions = new Options();
        this.nonDeprecatedOptions = new Options();
        for (Option option : builder.nonDeprecatedOptions) {
            nonDeprecatedOptions.addOption(option);
            allOptions.addOption(option);
        }
        this.deprecatedOptions = new Options();
        for (Option option : builder.deprecatedOptions) {
            deprecatedOptions.addOption(option);
            allOptions.addOption(option);
        }
    }

    public Options getAllOptions() {
        return allOptions;
    }

    public Options getDeprecatedOptions() {
        return deprecatedOptions;
    }

    public Options getNonDeprecatedOptions() {
        return nonDeprecatedOptions;
    }

    public static Builder builder() {
        return new Builder();
    }

    protected static class Builder {

        private final List<Option> nonDeprecatedOptions;
        private final List<Option> deprecatedOptions;

        protected Builder() {
            this.nonDeprecatedOptions = new ArrayList<>();
            this.deprecatedOptions = new ArrayList<>();
        }

        public Builder deprecatedOption(Option option) {
            deprecatedOptions.add(option);
            return this;
        }

        public Builder nonDeprecatedOption(Option option) {
            nonDeprecatedOptions.add(option);
            return this;
        }

        public CommandLineOptions build() {
            return new CommandLineOptions(this);
        }
    }
}