package org.autorefactor.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

public class CommonResolveArgs extends CommonArgs{
	
	@DynamicParameter(names = "--classpath-variable", description = "Provide classpath variable. (e.g. LIBS_DIR=/some/dir)")
	private Map<String, String> classPathVariables = new HashMap<>();
	@Parameter(names = "--source", description = "Source directories to use. (e.g. src/main/java). Default is: 'all source dirs'.")
	private List<String> sources = new ArrayList<String>();

	public CommonResolveArgs() {
		super();
	}

	/**
	 * Getter.
	 *
	 * @return property
	 */
	public Map<String, String> getClassPathVariables() {
	    return Collections.unmodifiableMap(classPathVariables);
	}

	/**
	 * Getter.
	 *
	 * @return property
	 */
	public List<String> getSources() {
	    return sources;
	}
}