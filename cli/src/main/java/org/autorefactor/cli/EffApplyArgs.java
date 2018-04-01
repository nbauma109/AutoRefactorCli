package org.autorefactor.cli;

import java.util.regex.Pattern;

/**
 * Effective refactor application arguments.
 *
 * TODO: cleanup
 */
public class EffApplyArgs {
    final SourceLevel sourceLevel;
    final Pattern sourceFileName;
    /* @Nullable */ final Pattern deltaDebugTestExceptionPattern;
    /* @Nullable */ final Pattern deltaDebugBeforeTestCodePattern;
    /* @Nullable */ final Pattern deltaDebugTestCodePattern;
    /* @Nullable */ final String deltaDebugBeforeTestCodeCommand;
    /* @Nullable */ final String deltaDebugTestCodeCommand;
    final boolean verbose;
    final boolean deltaDebug;

    public EffApplyArgs(SourceLevel sourceLevel, Pattern compile, boolean verboseApply) {
        super();
        this.sourceLevel = sourceLevel;
        this.sourceFileName = compile;
        this.deltaDebugTestExceptionPattern = null;
        this.deltaDebugBeforeTestCodePattern = null;
        this.deltaDebugTestCodePattern = null;
        this.deltaDebugBeforeTestCodeCommand = null;
        this.deltaDebugTestCodeCommand = null;
        this.verbose = verboseApply;
        this.deltaDebug = false;
    }

    public EffApplyArgs(SourceLevel sourceLevel, Pattern compile, Pattern deltaDebugTestExceptionPattern,
            Pattern deltaDebugBeforeTestCodePattern,
            String deltaDebugBeforeTestCodeCommand, 
            Pattern deltaDebugTestCodePattern,
            String deltaDebugTestCodeCommand, 
            boolean verboseApply, boolean deltaDebug) {
        super();
        this.sourceLevel = sourceLevel;
        this.sourceFileName = compile;
        this.deltaDebugTestExceptionPattern = deltaDebugTestExceptionPattern;
        this.deltaDebugBeforeTestCodePattern = deltaDebugBeforeTestCodePattern;
        this.deltaDebugTestCodePattern = deltaDebugTestCodePattern;
        this.deltaDebugBeforeTestCodeCommand = deltaDebugBeforeTestCodeCommand;
        this.deltaDebugTestCodeCommand = deltaDebugTestCodeCommand;
        this.verbose = verboseApply;
        this.deltaDebug = deltaDebug;
    }
}
