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

import org.jboss.migration.cli.commonscli.DefaultParser;
import org.jboss.migration.cli.commonscli.ParseException;

/**
 * @author emmartins
 */
public class CommandLineParser extends DefaultParser {

    private final boolean handleConcatenatedOptions;

    public CommandLineParser(boolean handleConcatenatedOptions) {
        this.handleConcatenatedOptions = handleConcatenatedOptions;
    }

    @Override
    protected void handleConcatenatedOptions(String token) throws ParseException {
        if (handleConcatenatedOptions) {
            super.handleConcatenatedOptions(token);
        }
    }
}
