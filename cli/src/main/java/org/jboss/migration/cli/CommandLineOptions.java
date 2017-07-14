package org.jboss.migration.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
//import org.jboss.migration.cli.logger.CommandLineMigrationLogger;

/**
 * @author Ingo Weiss
 */
public class CommandLineOptions {
    private final Options options;

    public CommandLineOptions() {
        options = new Options();

        Option opt = Option.builder("e").longOpt(CommandLineConstants.ENVIRONMENT.getArgument()).argName("environment file")
                .desc(CommandLineConstants.ENVIRONMENT.getDescription()).hasArg(true).build();
        options.addOption(opt);

        opt = Option.builder("i").longOpt(CommandLineConstants.INTERACTIVE.getArgument()).argName("true/false")
                .desc(CommandLineConstants.INTERACTIVE.getDescription()).hasArg(true).build();
        options.addOption(opt);

        opt = Option.builder("s").longOpt(CommandLineConstants.SOURCE.getArgument()).argName("source")
                .desc(CommandLineConstants.SOURCE.getDescription()).hasArg(true).build();
        options.addOption(opt);

        opt = Option.builder("t").longOpt(CommandLineConstants.TARGET.getArgument()).argName("target")
                .desc(CommandLineConstants.TARGET.getDescription()).hasArg(true).build();
        options.addOption(opt);

        opt = Option.builder("h").longOpt(CommandLineConstants.HELP.getArgument()).argName("help")
                .desc(CommandLineConstants.HELP.getDescription()).hasArg(false).build();
        options.addOption(opt);
    }

    public Options getOptions() {
        return options;
    }
}
