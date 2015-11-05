/*
 * Copyright 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.migration.cli;

import org.wildfly.migration.cli.logger.CommandLineMigrationLogger;
import org.wildfly.security.manager.WildFlySecurityManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author emmartins
 */
public abstract class CommandLineArgumentUsage {

    private static String USAGE;
    private static final String NEW_LINE = String.format("%n");

    private static final List<List<String>> arguments = new ArrayList<List<String>>();

    protected static void addArguments( String... args){
        ArrayList<String> tempArguments = new ArrayList<String>();
        Collections.addAll(tempArguments, args);
        arguments.add(tempArguments);
    }

    protected static List<String> instructions = new ArrayList<String>();

    private static String getCommand(int i){
        // Segment Instructions
        final List<String> segmentedInstructions = new ArrayList<String>();
        segmentInstructions(instructions.get(i), segmentedInstructions);

        // Segment Arguments
        final List<String> segmentedArguments = new ArrayList<String>();
        segmentArguments(arguments.get(i), segmentedArguments, 0);

        // First line
        StringBuilder output = new StringBuilder(String.format("    %-35s %s", segmentedArguments.remove(0), segmentedInstructions.remove(0)));
        output.append(NEW_LINE);

        if( segmentedArguments.size() <= segmentedInstructions.size()){
            int count = 0;
            for( String arg : segmentedArguments){
                output.append(String.format("         %-30s %s", arg, segmentedInstructions.remove(count)));
                output.append(NEW_LINE);
                count++;
            }

            for (String instruction : segmentedInstructions) {
                output.append(String.format("%-40s%s", " ", instruction));
                output.append(NEW_LINE);
            }
        }else{
            int count = 0;
            for ( String instruction : segmentedInstructions ){
                output.append(String.format("         %-30s %s", segmentedArguments.remove(count), instruction));
                output.append(NEW_LINE);
                count++;
            }

            for( String arg : segmentedArguments ){
                output.append(String.format("         %-30s", arg));
                output.append(NEW_LINE);
            }
        }

        output.append(NEW_LINE);
        return output.toString();
    }

    private static void segmentArguments(List<String> input, List<String> output, int depth){
        int width = 30;

        if( depth == 0 ){
            width = 35;
        }

        if( input.size() > 0 ) {
            StringBuilder argumentsString = new StringBuilder();
            for( int i = 0; i < input.size(); ){
                // Trim in case an argument is too large for the width. Shouldn't happen.
                if( input.get(0).length() > width ){
                    String tooLong = input.remove(0);
                    input.add(0, tooLong.substring(0, width-5));
                }

                if( input.size() == 1 && (argumentsString.toString().length() + input.get(0).length() <= width)){
                    argumentsString.append(input.remove(0));
                }else if( argumentsString.toString().length() + input.get(0).length() + 2 <= width ){
                    argumentsString.append(input.remove(0)).append(", ");
                }else{
                   break;
                }

            }
            output.add(argumentsString.toString());
            segmentArguments(input, output, depth+1);
        }
    }

    private static void segmentInstructions(String instructions, List<String> segments) {
        if (instructions.length() <= 40) {
            segments.add(instructions);
        } else {
            String testFragment = instructions.substring(0,40);
            int lastSpace = testFragment.lastIndexOf(' ');
            if (lastSpace < 0) {
                // degenerate case; we just have to chop not at a space
                lastSpace = 39;
            }
            segments.add(instructions.substring(0, lastSpace + 1));
            segmentInstructions(instructions.substring(lastSpace + 1), segments);
        }
    }

    protected static String usage(String executableBaseName) {
        boolean isWindows = (WildFlySecurityManager.getPropertyPrivileged("os.name", null)).toLowerCase(Locale.ENGLISH).contains("windows");
        String executableName = isWindows ? executableBaseName : executableBaseName + ".sh";

        if (USAGE == null) {
            final StringBuilder sb = new StringBuilder();
            sb.append(NEW_LINE).append(CommandLineMigrationLogger.ROOT_LOGGER.argUsage(executableName)).append(NEW_LINE);

            for (int i = 0; i < arguments.size(); i++) {
                sb.append(getCommand(i)).append(NEW_LINE);
            }
            USAGE = sb.toString();
        }
        return USAGE;

    }

}
