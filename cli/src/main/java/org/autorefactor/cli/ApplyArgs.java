package org.autorefactor.cli;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/** Parameters for application of refactorings. */
@Parameters(commandDescription = "Apply refactorings.")
public class ApplyArgs extends CommonResolveArgs {
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
