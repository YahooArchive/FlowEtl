/*******************************************************
 *                                                     *
 * Copyright (C) 2011 Yahoo! Inc. All Rights Reserved. *
 *                                                     *
 *                Licensed under the New               *
 *                 BSD License. See the                *
 *              accompanying LICENSE file              *
 *              for the specific language              *
 *              governing permissions and              *
 *                limitations under the                *
 *                       License.                      *
 *******************************************************/
package com.yahoo.flowetl.commons.runner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;

import com.yahoo.flowetl.core.PipeRunner;

/**
 * This class helps create a main program that will activate a given set of
 * pipes. It helps by giving you overload points that can add on different
 * config that is needed while still allowing you to specify what options are
 * valid for your programs consumption.
 * 
 * @author Joshua Harlow
 */
public abstract class BaseRunner implements Runner
{
    // default config names
    protected static final String CONFIG_OPT = "config";
    protected static final String HELP_OPT = "help";

    // program name
    private static final String PROG_NAME = "FlowEtl";

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.commons.runner.Runner#runProgram(java.lang.String[])
     */
    public final void runProgram(String[] args) throws Exception {
        CommandLineParser parser = new PosixParser();
        Options opts = getOptions();
        CommandLine cmd = parser.parse(opts, args);
        if (helpNeeded(cmd)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(getProgramName(), opts);
            return;
        }
        runProgram(cmd);
    }

    /**
     * Gets the program name which we are running as. Can be overriden to change
     * as desired...
     */
    protected String getProgramName() {
        return PROG_NAME;
    }

    /**
     * Should return whether help is needed or whether it is not needed by
     * examining the command line options that have been parsed and determining
     * if they are sufficient.
     * 
     * @param cmd
     * 
     * @return if help is needed or not
     */
    protected boolean helpNeeded(CommandLine cmd) {
        if (cmd.hasOption(HELP_OPT)) {
            return true;
        }
        String cfgFile = cmd.getOptionValue(CONFIG_OPT);
        if (StringUtils.isEmpty(cfgFile)) {
            return true;
        }
        return false;
    }

    /**
     * The override point where you need to create the pipe runner that will
     * perform your actions, aka the pipes that this program should run. At this
     * point no help will be needed but you still have to analyze the command
     * line parsed options to determine what is needed to construct your pipes.
     * 
     * @param cmd
     *            the cmd line options
     * 
     * @throws Exception
     *             if forming a pipe runner fails
     */
    protected abstract PipeRunner formRunner(CommandLine cmd) throws Exception;

    /**
     * Runs the program by taking the given options and forming a runner and
     * adding on a simple command line listener to that pipe runner and then
     * running the pipe which was created in the currently active thread.
     * 
     * @param cmd
     *            the cmd line options (post help validation)
     * 
     * @throws Exception
     *             the exception
     */
    protected void runProgram(CommandLine cmd) throws Exception {
        // setup some default job
        PipeRunner toRun = formRunner(cmd);
        // make a cmd line output listener
        toRun.addFlowListener(new CommandLineListener());
        // do it...
        toRun.run();
    }

    /**
     * Gets the command line options that this class will be using.
     * 
     * @return the options
     */
    @SuppressWarnings("static-access")
    protected Options getOptions() {
        Options cliOpts = new Options();
        Option cfgFile = OptionBuilder.withArgName("file").hasArg(true).withDescription("the configuration file").create(CONFIG_OPT);
        cliOpts.addOption(cfgFile);
        Option helpOpt = OptionBuilder.hasArg(false).withDescription("help message").create(HELP_OPT);
        cliOpts.addOption(helpOpt);
        return cliOpts;
    }

}
