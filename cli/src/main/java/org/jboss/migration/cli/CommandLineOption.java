/*
 * Copyright 2018 Red Hat, Inc.
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

import org.jboss.cli.commonscli.Option;

/**
 * @author emartins
 */
public interface CommandLineOption {
    Option ENVIRONMENT = Option.builder("e")
            .longOpt(CommandLineConstants.ENVIRONMENT.getArgument())
            .argName("environment file")
            .desc(CommandLineConstants.ENVIRONMENT.getDescription())
            .hasArg(true)
            .build();

    Option HELP = Option.builder("h")
            .longOpt(CommandLineConstants.HELP.getArgument())
            .argName("help")
            .desc(CommandLineConstants.HELP.getDescription())
            .hasArg(false)
            .build();

    Option INTERACTIVE = Option.builder("i")
            .longOpt(CommandLineConstants.INTERACTIVE.getArgument())
            .argName("true/false")
            .desc(CommandLineConstants.INTERACTIVE.getDescription())
            .hasArg(true)
            .build();

    Option NON_INTERACTIVE = Option.builder("n")
            .longOpt(CommandLineConstants.NON_INTERACTIVE.getArgument())
            .desc(CommandLineConstants.NON_INTERACTIVE.getDescription())
            .hasArg(false)
            .build();

    Option SOURCE = Option.builder("s")
            .longOpt(CommandLineConstants.SOURCE.getArgument())
            .argName("source")
            .desc(CommandLineConstants.SOURCE.getDescription())
            .hasArg(true)
            .build();

    Option TARGET = Option.builder("t")
            .longOpt(CommandLineConstants.TARGET.getArgument())
            .argName("target")
            .desc(CommandLineConstants.TARGET.getDescription())
            .hasArg(true)
            .build();
}
