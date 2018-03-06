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

import org.jboss.cli.commonscli.Options;

import java.util.stream.Stream;

import static org.jboss.migration.cli.CommandLineOption.*;

/**
 * @author Ingo Weiss
 */
public interface CommandLineOptions {

    Options NON_DEPRECATED = new Options()
            .addOption(ENVIRONMENT)
            .addOption(HELP)
            .addOption(NON_INTERACTIVE)
            .addOption(SOURCE)
            .addOption(TARGET);

    Options DEPRECATED = new Options()
            .addOption(INTERACTIVE);

    Options ALL = Stream.concat(NON_DEPRECATED.getOptions().stream(), DEPRECATED.getOptions().stream())
            .collect(Options::new, Options::addOption, (options1, options2) -> options2.getOptions().stream().forEach(option -> options1.addOption(option)));
}