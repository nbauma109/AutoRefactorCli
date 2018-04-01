package org.autorefactor.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/** Match AST by matcher. */
@Parameters(commandDescription = "Apply matcher pattern to abstract syntax tree.")
public class AstMatchArgs extends CommonResolveArgs {
	@Parameter(names = "--match", description = "Ast matcher expression.", required = true)
	private String matchPattern;
	
	public String getMatchPattern() {
		return matchPattern;
	}
}
