package org.autorefactor.cli.ast;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.autorefactor.matcher.AstMatcher;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Base class for groovy shell instance (currently used but not needed).
 */
public class AstMatcherBase extends Script {
	public static Matcher<?> evaluateMatchExpression(String expression) {
		GroovyShell shell = AstMatcherBase.createAstMatcherShell(AstMatcherBase.class);

		return (Matcher<?>) shell.evaluate(expression);
	}

	public static GroovyShell createAstMatcherShell(Object baseObject) {
		CompilerConfiguration configuration = new CompilerConfiguration();
		configuration.setScriptBaseClass(AstMatcherBase.class.getName());

		Map<String,Object> vars = new HashMap<String,Object>();
		// duplicate key error because of overloaded methods
		//Stream.of(AstMatcher.class.getMethods()).collect(Collectors.toMap(m -> m.getName(), m -> m));
		// not callable
		//Stream.of(AstMatcher.class.getMethods()).forEach(m -> vars.put(m.getName(), m));
		Stream.of(AstMatcher.class.getMethods()).forEach(m -> vars.put(m.getName(), new Closure<Object>(baseObject) {
			
			@Override
			public Object call() {
				try {
					return m.invoke(null);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}));

		Binding binding = new Binding(vars);

		return new GroovyShell(baseObject.getClass().getClassLoader(), binding, configuration);
	}

	@Override
	public Object run() {
		System.out.println("run called");
		return null;
	}
}