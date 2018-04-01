package org.autorefactor.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/** Parameters for application of refactorings with delta debugging support. */
@Parameters(commandDescription = "Apply refactorings with delta debug support.")
public class ApplyDDArgs extends CommonResolveArgs {

    /** Enable delta debugging. */
    @Parameter(names = "--delta-debug", description = "Enable delta debugging.")
    private boolean deltaDebug;

    @Parameter(names = "--dd-test-exception-re", description = "Java regular expression for expected exception.")
    private String deltaDebugTestExceptionPattern;

    @Parameter(names = "--dd-before-test-code-re", description = "Java regular expression for code before refactoring.")
    private String deltaDebugBeforeTestCodePattern;

    @Parameter(names = "--dd-before-test-code", description = "Executable to test code before refactoring.")
    private String deltaDebugBeforeTestCodeCommand;

    @Parameter(names = "--dd-test-code-re", description = "Java regular expression to detect expected code after refactoring.")
    private String deltaDebugTestCodePattern;

    @Parameter(names = "--dd-test-code", description = "Command to detect expected code after refactoring.")
    private String deltaDebugTestCodeCommand;

    @Parameter(names = "--source-level", description = "Java source release to support. (e.g. 1.7)")
    private String sourceLevel;

    @Parameter(names = "--refactorings",
            description = "Comma separated list of refactorings (e.g. UseDiamondOperatorRefactoring).",
            required = true)
    private List<String> refactorings = new ArrayList<String>();

    @Parameter(names = "--exclude-refactorings",
            description = "Comma separated list of refactorings to exclude (e.g. UseDiamondOperatorRefactoring).")
    private List<String> excludedRefactorings = new ArrayList<String>();

    /**
     * Is delta debugging enabled?
     *
     * @return delta debugging enabled?
     */
    public boolean isDeltaDebug() {
        return deltaDebug;
    }

    /**
     * Getter.
     *
     * @return expected exception pattern or null.
     */
    public Pattern getDeltaDebugTestExceptionPattern() {
        return deltaDebugTestExceptionPattern != null ? Pattern.compile(deltaDebugTestExceptionPattern) : null;
    }

    /**
     * Getter.
     *
     * @return expected code pattern or null.
     */
    public Pattern getDeltaDebugBeforeTestCodePattern() {
        return deltaDebugBeforeTestCodePattern != null ? Pattern.compile(deltaDebugBeforeTestCodePattern, Pattern.DOTALL | Pattern.MULTILINE) : null;
     //   return deltaDebugBeforeTestCodePattern != null ? Pattern.compile(deltaDebugBeforeTestCodePattern, Pattern.MULTILINE) : null;
    }

    /**
     * Getter.
     *
     * @return command path or null.
     */
    public String getDeltaDebugBeforeTestCodeCommand() {
        return deltaDebugBeforeTestCodeCommand;
    }

    /**
     * Getter.
     *
     * @return expected code pattern or null.
     */
    public Pattern getDeltaDebugTestCodePattern() {
        return deltaDebugTestCodePattern != null ? Pattern.compile(deltaDebugTestCodePattern, Pattern.DOTALL | Pattern.MULTILINE) : null;
        //return deltaDebugTestCodePattern != null ? Pattern.compile(deltaDebugTestCodePattern, Pattern.MULTILINE) : null;
    }

    /**
     * Getter.
     *
     * @return command path or null.
     */
    public String getDeltaDebugTestCodeCommand() {
        return deltaDebugTestCodeCommand;
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
     * The refactorings to exclude from apply.
     *
     * @return The refactorings to exclude from apply.
     */
    public List<String> getExcludedRefactorings() {
        return excludedRefactorings;
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
