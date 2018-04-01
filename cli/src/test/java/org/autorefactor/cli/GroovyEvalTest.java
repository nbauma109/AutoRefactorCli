package org.autorefactor.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.autorefactor.cli.ast.AstMatcherBase;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.autorefactor.matcher.Matchers.MethodInvocationMatcher;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.Test;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.Eval;

/**
 * http://groovy-lang.org/integrating.html
 */
public class GroovyEvalTest {
	@Test
	public void testEval() {
		final Map<String, String> values = new HashMap<>();
		values.put("name", "eclipse");
		values.put("lang", "Groovy");
		String groovy1 = "\"Hello $params.name from $params.lang!\"";
		assertEquals("Hello eclipse from Groovy!", Eval.me("params", values, groovy1).toString());

		String groovy2 = "x == y + z";
		assertTrue((Boolean)Eval.xyz(5, 3, 2, groovy2));
	}
	
	@Test
	public void testShell() {
		GroovyShell shell = new GroovyShell();                           
		Object result = shell.evaluate("3*5");                       
		Object result2 = shell.evaluate(new StringReader("3*5"));   
		assertEquals(result, result2);
		Script script = shell.parse("3*5");                          
		assertTrue(script instanceof groovy.lang.Script);
		assertEquals(script.run(), 15);      
	}
	
	public static class MyBase extends Script {
		protected String s1 = "My.s1"; 
		
		@Override
		public Object run() {
			System.out.println("run called");
			return null;
		}

		@Override
		public Object getProperty(String name) {
			System.out.println("getProperty(" + name + ")");
			Object res = super.getProperty(name);
			System.out.println("getProperty(" + name + ") = " + res);
			return res;
		}
	}

	@Test
	public void testShellWithCustomBaseClass() {
		CompilerConfiguration configuration = new CompilerConfiguration();
		configuration.setScriptBaseClass(MyBase.class.getName());

		Binding binding = new Binding();

		GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), binding, configuration);
		assertEquals(15, shell.evaluate("3*5"));                       
		assertEquals("My.s1", shell.evaluate("s1"));
	}

	// TODO: clang query 
	// https://eli.thegreenplace.net/2014/07/29/ast-matchers-and-clang-refactoring-tools
	// https://reviews.llvm.org/D2098
	// TODO: query replace tool: https://reviews.llvm.org/D29614
	@SuppressWarnings("serial")
	@Test
	public void testCreationOfMatcher() {
		final Matcher<?> matcher = (Matcher<?>) AstMatcherBase.evaluateMatchExpression("methodInvocation().hasName(\"testEval\")");
		assertEquals(MethodInvocationMatcher.class, matcher.getClass());                       
	}
}
