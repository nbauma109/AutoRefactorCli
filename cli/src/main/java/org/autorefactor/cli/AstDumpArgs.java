package org.autorefactor.cli;

import java.util.regex.Pattern;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/** Dump AST. */
@Parameters(commandDescription = "Dump abstract syntax tree.")
public class AstDumpArgs {
//    @Parameter(names = "--filename-filter", description = "Select files with path or filename containing string.")
//    private String filenameFilter;

    @Parameter(names = "--include-re", description = "Java regular expression to match input file names. (e.g. '.*/MyFile.java')")
    private String includeRe = ".*";

    /** Allow late setting of verbose parameter. */
    @Parameter(names = "--verbose", description = "Verbose mode")
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
     */
    public String getProjectPath() {
        return projectPath;
    }

    public Pattern getIncludePattern() {
        return Pattern.compile(includeRe);
    }
}
