package org.autorefactor.cli;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/** Parameters for application of refactorings. */
@Parameters(commandDescription = "Apply refactorings.")
public class ApplyArgs {
    /** Allow late setting of verbose parameter. */
    @Parameter(names = "--verbose", description = "Verbose mode")
    private boolean verbose;

    @Parameter(names = "--classpath-variable", description = "Provide classpath variable. (e.g. LIBS_DIR=/some/dir)",
            arity = 1)
    private List<String> classPathVariables = new ArrayList<String>();

    @Parameter(names = "--project", description = "Path to project file.", required = true)
    private String projectPath;

    @Parameter(names = "--source-level", description = "Java source release to support. (e.g. 1.7")
    private String sourceLevel;

    @Parameter(names = "--source", description = "Source directories to use. (e.g. src/main/java).")
    private List<String> sources = new ArrayList<String>();

    @Parameter(names = "--refactorings",
            description = "Comma separated list of refactorings (e.g. UseDiamondOperatorRefactoring).",
            required = true)
    private List<String> refactorings = new ArrayList<String>();

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
    public List<String> getClassPathVariables() {
        return classPathVariables;
    }

    /**
     * Getter.
     *
     * @return property
     */
    public String getProjectPath() {
        return projectPath;
    }

    /**
     * Getter.
     *
     * @return property
     */
    public List<String> getSources() {
        return sources;
    }

    /**
     * The refactorings to apply.
     *
     * @return The refactorings to apply.
     */
    public List<String> getRefactorings() {
        return refactorings;
    }

    /**
     * The java source level to support.
     *
     * @return java source level to support
     */
    public String getSourceLevel() {
        return sourceLevel;
    }
}
