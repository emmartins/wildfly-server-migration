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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * @author Ingo Weiss
 */
public class CommandLineOptions {
    private final Options options;

    public CommandLineOptions() {
        options = new Options();

        Option opt = new Option("e", CommandLineConstants.ENVIRONMENT.getArgument(), true, CommandLineConstants.ENVIRONMENT.getDescription());
        opt.setArgName("environment file");
        options.addOption(opt);

        opt = new Option("i", CommandLineConstants.INTERACTIVE.getArgument(), true, CommandLineConstants.INTERACTIVE.getDescription());
        opt.setArgName("true/false");
        options.addOption(opt);

        opt = new Option("s", CommandLineConstants.SOURCE.getArgument(), true, CommandLineConstants.SOURCE.getDescription());
        opt.setArgName("source");
        options.addOption(opt);

        opt = new Option("t", CommandLineConstants.TARGET.getArgument(), true, CommandLineConstants.TARGET.getDescription());
        opt.setArgName("target");
        options.addOption(opt);

        opt = new Option("h", CommandLineConstants.HELP.getArgument(), false, CommandLineConstants.HELP.getDescription());
        opt.setArgName("help");
        options.addOption(opt);
    }

    public Options getOptions() {
        return options;
    }
}
