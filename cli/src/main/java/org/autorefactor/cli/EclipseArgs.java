package org.autorefactor.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/** Eclipse info printing. */
@Parameters(commandDescription = "Print eclipse info.")
public class EclipseArgs {
    /** Allow setting of verbose parameter after command. */
    @Parameter(names = "--verbose", description = "Verbose mode", hidden = true)
    private boolean verbose;

    @Parameter(names = "--project", description = "Path to project file.", required = true)
    private String projectPath;

    /**
     * Is Verbose?
     *
     * @return is Verbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Getter.
     *
     * @return property
     */
    public String getProjectPath() {
        return projectPath;
    }
}
