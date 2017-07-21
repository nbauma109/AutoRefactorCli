package org.autorefactor.cli;

import com.beust.jcommander.Parameter;

/**
 * Parameters for command line interface.
 */
public class Args {
    // @Parameter(names = { "-log", "-verbose" }, description = "Level of
    // verbosity")
    // private Integer verbose = 1;

    /** fake parameter here, evaluated by shell wrapper */
    @Parameter(names = "--consolelog", description = "Show eclipse console log. Must be first parameter.",
            order = 0)
    private boolean consoleLog = false;

    @Parameter(names = "--debug", description = "Debug mode")
    private boolean debug = false;

    @Parameter(names = "--verbose", description = "Verbose mode")
    private boolean verbose = false;

    @Parameter(names = { "--help", "help" }, description = "Display usage info.", help = true)
    private boolean help;

    /**
     * Getter.
     *
     * @return property
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Getter.
     *
     * @return property
     */
    public boolean isHelp() {
        return help;
    }
}
