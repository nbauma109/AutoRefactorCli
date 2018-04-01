package org.autorefactor.cli;

import com.beust.jcommander.Parameter;

/** Parameters for command line interface. */
public class Args {
    // @Parameter(names = { "-log", "-verbose" }, description = "Level of
    // verbosity")
    // private Integer verbose = 1;

    /** Fake parameter here, evaluated by shell wrapper. */
    @Parameter(names = "--consolelog", description = "Show eclipse console log. Must be first parameter.",
            order = 0)
    private boolean consoleLog;

    @Parameter(names = "--debug", description = "Debug mode")
    private boolean debug;

    @Parameter(names = "--verbose", description = "Verbose mode")
    private boolean verbose;

    @Parameter(names = { "--help", "help" }, description = "Display usage info.", help = true)
    private boolean help;

    @Parameter(names = { "--experimental"}, description = "Display usage info, experimental options included.", help = true)
    private boolean experimentalHelp;

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

    public boolean isExperimentalHelp() {
        return experimentalHelp;
    }
}
