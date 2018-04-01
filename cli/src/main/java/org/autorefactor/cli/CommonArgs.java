package org.autorefactor.cli;

import java.util.regex.Pattern;

import com.beust.jcommander.Parameter;

public class CommonArgs {

	@Parameter(names = "--path-filter", description = "Select files with path containing string.")
	private String pathFilter = "";
	@Parameter(names = "--path-re", description = "Select files with java regular expression. (e.g. '/mydir.*/MyFile')")
	private String includeRe = ".*";
	/** Duplicated here compared to args to allow setting of verbose parameter after selection of command. */
	@Parameter(names = "--verbose", description = "Verbose mode", hidden = true)
	private boolean verbose;
	@Parameter(names = "--project", description = "Path to project file.", required = true)
	private String projectPath;

	public CommonArgs() {
		super();
	}

	/**
	 * Is Verbose?
	 *
	 * @return is Verbose
	 */
	public final boolean isVerbose() {
	    return verbose;
	}

	/**
	 * Getter.
	 */
	public final String getProjectPath() {
	    return projectPath;
	}

	public final Pattern getIncludePattern() {
	    return Pattern.compile(prepareIncludePattern(includeRe));
	}

	public static String prepareIncludePattern(String includeRe) {
		if (!includeRe.startsWith(".*") && !includeRe.startsWith("^")) {
			includeRe = ".*" + includeRe;
		}
		if (!includeRe.endsWith(".*") && !includeRe.endsWith("$")) {
			includeRe = includeRe + ".*";
		}
		return includeRe;
	}

	public final String getPathFilter() {
		return pathFilter;
	}
}