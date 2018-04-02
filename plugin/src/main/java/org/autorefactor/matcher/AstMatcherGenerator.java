package org.autorefactor.matcher;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IDocElement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

/**
 * Generates type safe matcher classes from AST node classes.
 * 
 * @author cal
 */
public class AstMatcherGenerator {

	private static final String MATCHERS_CLASS_NAME = "Matchers";

	public static void main(String[] args) throws Exception {
		List<Class<?>> topAstNodes = new ArrayList<>();
		for (int nodeType = 0; nodeType <= ASTNode.TYPE_METHOD_REFERENCE; nodeType++) {
			Class<?> nodeClass = null;
			
			try {
				nodeClass = ASTNode.nodeClassForType(nodeType);
				nodeClass.getMethod("propertyDescriptors", Integer.TYPE);
				topAstNodes.add(nodeClass);
			} catch (IllegalArgumentException ignore) {
				
			}
		}
		Collections.sort(topAstNodes, (a,b) -> a.getSimpleName().compareTo(b.getSimpleName()));
		
		final File pluginSrcDir = new File(AstMatcherGenerator.class.getResource(".").getFile().replaceFirst("/plugin/target/.*", "/plugin"));

		File matchersFile = new File(pluginSrcDir, "src/main/java/org/autorefactor/matcher/" + MATCHERS_CLASS_NAME + ".java");
		// TODO: no need to generate to repository code because result is copy & pasted.
		File factoriesFile = new File(pluginSrcDir, "src/main/java/org/autorefactor/matcher/MatcherFactories.java");
		
		List<Class<?>> astNodes;
		try (PrintWriter w = new PrintWriter(new FileWriter(matchersFile))) {
			astNodes = generateMatchers(w, topAstNodes);
		}
		System.out.println("wrote: " + matchersFile);

		StringBuilder factories = new StringBuilder();;
		for (Class<?> n: astNodes) {
			String nodeClassname = n.getSimpleName();
			String methodName = nodeClassname.equals("ASTNode") ? "node" : decapitalize(nodeClassname);
			String s = "\n" +
					t("    /**\n") +
					f("     * Create a matcher matching AST node %s.\n", nodeClassname) +
					t("     *\n") +
					f("     * {@link %s}\n", n.getName()) +
					t("     */\n") +
					f("    public static %s.%sMatcher %s() {\n", 
							MATCHERS_CLASS_NAME, nodeClassname, methodName) +
					f("        return new %s.%sMatcher();\n", MATCHERS_CLASS_NAME, nodeClassname) +
					t("    }\n");
			factories.append(s);
		}
		try (PrintWriter w = new PrintWriter(new FileWriter(factoriesFile))) {
			w.write("package org.autorefactor.matcher;\n" + 
					"\n" +
					"class MatcherFactories {\n" +
					factories +
					"}\n");
		}
		System.out.println("wrote: " + factoriesFile);
	}

	/**
	 * Returns list of all AST nodes with generated matchers.
	 */
	private static List<Class<?>> generateMatchers(PrintWriter w, List<Class<?>> topAstNodes)
			throws IllegalAccessException, InvocationTargetException {
		w.write("package org.autorefactor.matcher;\n" +
				"\n" +
				"import static org.autorefactor.matcher.InternalMatcherUtil.tryCast;\n" +
				"\n" +
				//"import java.util.function.Function;\n" + 
				//"\n" +
				"import org.autorefactor.matcher.AstMatcher.Matcher;\n" +
				//"import org.autorefactor.matcher.TypeMatcher.TypeBindingMatcher;\n" + 
				"import org.autorefactor.refactoring.ASTHelper;\n" + 
				""
				);
		Set<Class<?>> imports = new HashSet<>();
		for (Class<?> n: topAstNodes) {
			imports.add(n);
			propertyDescriptors(n).stream()
			.filter(c -> c instanceof ChildPropertyDescriptor)
			.forEach(c -> imports.add(((ChildPropertyDescriptor)c).getChildType()));
			propertyDescriptors(n).stream()
			.filter(c -> c instanceof ChildListPropertyDescriptor)
			.forEach(c -> imports.add(((ChildListPropertyDescriptor)c).getElementType()));
		}
		imports.add(ITypeBinding.class);
		imports.add(IVariableBinding.class);
		imports.add(IMethodBinding.class);
		imports.add(AnnotatableType.class);
		imports.add(Statement.class);
		// not used yet
		imports.remove(IDocElement.class);
		imports.remove(IExtendedModifier.class);

		List<String> importNames = imports.stream().map(c -> c.getName()).collect(Collectors.toList());
		Collections.sort(importNames);
		importNames.stream().forEach(n -> w.write(String.format("import %s;\n", n)));
		
		List<Class<?>> astNodes = imports.stream().filter(c -> ASTNode.class.isAssignableFrom(c)).collect(Collectors.toList());
		
		w.write("\n" +
		        "/**\n" +
				" * Eclipse JDT dom ast node specific matcher classes.\n" +
		        " *\n" +
		        " * This is file is generated by AstMatcherGenerator.\n" + 
				" */\n" +
				f("public final class %s {\n", MATCHERS_CLASS_NAME) +
				"\n" +
				f("    private %s() {}\n", MATCHERS_CLASS_NAME) +
				""
				);
		for(Class<?> nodeClass: astNodes) {
			List<StructuralPropertyDescriptor> pds = propertyDescriptors(nodeClass);
			generate(w, nodeClass, pds);
		}
		w.write("}\n");
		return astNodes;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	private static List<StructuralPropertyDescriptor> propertyDescriptors(Class<?> nodeClass)
			throws IllegalAccessException, InvocationTargetException {
		try {
			Method m = nodeClass.getMethod("propertyDescriptors", Integer.TYPE);
			return (List<StructuralPropertyDescriptor>)m.invoke(null, AST.JLS8);
		} catch (NoSuchMethodException ignore) {
			return Collections.emptyList();
		}
	}
	
	@SuppressWarnings("unused")
	private static void print(Class<?> clazz, List<StructuralPropertyDescriptor> propertyDescriptors) {
		System.out.println(clazz.getName());
		for (StructuralPropertyDescriptor pd: propertyDescriptors) {
			System.out.println("    pd: " + pd);
			if (pd instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor cpd = (ChildPropertyDescriptor) pd;
				System.out.println("        child " + cpd.getChildType());
			} else if (pd instanceof ChildListPropertyDescriptor) {
				ChildListPropertyDescriptor cpd = (ChildListPropertyDescriptor) pd;
					System.out.println("        child list of " + cpd.getElementType());
			} else if (pd instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor spd = (SimplePropertyDescriptor) pd;
				System.out.println("        value " + spd.getValueType());
			} else {
				System.out.println("        class " + pd.getClass().getName());
			}
		}
	}

	private static void generate(PrintWriter w, Class<?> clazz, List<StructuralPropertyDescriptor> pds) {
		final String nodeClassName = clazz.getSimpleName();
		final String matcherClassName = nodeClassName + "Matcher";
		final String superClassName = 
				Statement.class.isAssignableFrom(clazz)
				? "StatementMatcherSupport"
						: Expression.class.isAssignableFrom(clazz)
						? "ExpressionMatcherSupport"
								: "CommonMatcher";

		// class head
		w.write("\n" + 
				"    /**\n" +
				f("     * Matcher builder for AST node %s.\n", nodeClassName) +
				"     *\n" +
				t("     * This code is generated.\n") +
				f("     * For details see {@link %s}.\n", MATCHERS_CLASS_NAME) +
				"     */\n" + 
				f("    public static final class %s\n", matcherClassName) +
				f("        extends %s<%s, %s> {\n", superClassName, nodeClassName, matcherClassName)
				);
		// constructor
		w.write("\n" +
				f("        %s() {\n", matcherClassName) +
				f("            super(%s.class);\n", nodeClassName) +
				"        }\n"
				);

		for (StructuralPropertyDescriptor pd: pds) {
			if (pd instanceof ChildPropertyDescriptor) {
				generateChildProperty(w, nodeClassName, matcherClassName, (ChildPropertyDescriptor) pd);
			} else if (pd instanceof ChildListPropertyDescriptor) {
				generateChildListProperty(w, nodeClassName, matcherClassName, (ChildListPropertyDescriptor) pd);
			} else if (pd instanceof SimplePropertyDescriptor) {
				generateSimpleProperty(w, nodeClassName, matcherClassName, (SimplePropertyDescriptor) pd);
			} else {
				System.out.println("        class " + pd.getClass().getName());
			}
		}
		
		// TODO: review (good name, place, content?)
		// TODO: check all bindings!
		if (nodeClassName.equals("SimpleName")) {
			w.write(f("\n" + 
					"        public final %s hasDeclaringClass(String name) {\n" + 
					"            addPredicate(\"declaringClass\", n -> {\n" + 
					"                IVariableBinding b = tryCast(n.resolveBinding(), IVariableBinding.class);\n" + 
					"                ITypeBinding declaringClass = b != null ? b.getDeclaringClass() : null;\n" + 
					"                if (declaringClass == null) {\n" + 
					"                	IMethodBinding mb = tryCast(n.resolveBinding(), IMethodBinding.class);\n" + 
					"                    declaringClass = mb != null ? mb.getDeclaringClass() : null;\n" + 
					"                }\n" + 
					"                return name != null && declaringClass != null && name.equals(declaringClass.getQualifiedName());\n" + 
					"            });\n" + 
					"            return this;\n" + 
					"        }\n", 
					matcherClassName, nodeClassName
					));
		} else if (nodeClassName.equals("MethodDeclaration")) {
			 // TODO: check if other names may match, too.
			// TODO: cleanup simpleName reference
			w.write("\n" + 
					"		public final MethodDeclarationMatcher hasDeclaringClass(String qualifiedName) {\n" + 
					"        	return hasName(AstMatcher.simpleName().hasDeclaringClass(qualifiedName));\n" + 
					"		}\n");
		} else if (nodeClassName.equals("IfStatement")) {
			// add method aliases to improve readability
			w.write("\n" + 
			        "        @SafeVarargs\n" +
					"		 public final IfStatementMatcher hasCondition(Matcher<? extends Expression>... matchers) {\n" + 
					"            return hasExpression(matchers);\n" + 
					"		 }\n");
			w.write("\n" + 
			        "        @SafeVarargs\n" +
					"		 public final IfStatementMatcher hasThen(Matcher<? extends Statement>... matchers) {\n" + 
					"            return hasThenStatement(matchers);\n" + 
					"		 }\n");
			w.write("\n" + 
			        "        @SafeVarargs\n" +
					"		 public final IfStatementMatcher hasElse(Matcher<? extends Statement>... matchers) {\n" + 
					"            return hasElseStatement(matchers);\n" + 
					"		 }\n");
		} else if (nodeClassName.equals("Assignment")) {
			generateOperatorPredicates(w, Assignment.Operator.class, "Assignment");
		} else if (nodeClassName.equals("InfixExpression")) {
			generateOperatorPredicates(w, InfixExpression.Operator.class, "InfixExpression");
		} else if (nodeClassName.equals("PostfixExpression")) {
			generateOperatorPredicates(w, PostfixExpression.Operator.class, "PostfixExpression");
		} else if (nodeClassName.equals("PrefixExpression")) {
			generateOperatorPredicates(w, PrefixExpression.Operator.class, "PrefixExpression");
		} else if (nodeClassName.equals("PrimitiveType")) {
			generatePrimitiveTypePredicates(w, PrimitiveType.class.getFields(), PrimitiveType.Code.class, "PrimitiveType");
			w.write("\n" +
					"        public final PrimitiveTypeMatcher hasCode(PrimitiveType.Code code) {\n" + 
					"            addPredicate(n -> code != null && code.equals(n.getPrimitiveTypeCode()));\n" + 
					"            return this;\n" + 
					"        }\n"); 
		} else if (nodeClassName.equals("ForStatement")) {
			// add method aliases to improve readability
			w.write("\n" + 
			        "       @SafeVarargs\n" +
					"		public final ForStatementMatcher hasCondition(Matcher<? extends Expression>... matchers) {\n" + 
					"        	return hasExpression(matchers);\n" + 
					"		}\n");
		} else if (nodeClassName.equals("MethodInvocation")) {
			w.write("\n" + 
					"        public final MethodInvocationMatcher isMethod(String typeQualifiedName,\n" + 
					"                String methodName, String... parameterTypesQualifiedNames) {\n" + 
					"            addPredicate(n -> ASTHelper.isMethod(n, typeQualifiedName, methodName, parameterTypesQualifiedNames));\n" + 
					"            return this;\n" + 
					"        }\n");
		} else if (nodeClassName.equals("MethodDeclaration")) {
			w.write("\n" + 
					"        public final MethodDeclarationMatcher isMethod(String typeQualifiedName,\n" + 
					"                String methodName, String... parameterTypesQualifiedNames) {\n" + 
					"            addPredicate(n -> ASTHelper.isMethod(n, typeQualifiedName, methodName, parameterTypesQualifiedNames));\n" + 
					"            return this;\n" + 
					"        }\n");
		}
		if (nodeClassName.equals("BooleanLiteral")) {
			w.write("\n" + 
					"        public final BooleanLiteralMatcher isEqualTo(boolean b) {\n" + 
					"            addPredicate(n -> n.booleanValue() == b);\n" + 
					"            return this;\n" + 
					"        }\n" +
					"\n" + 
					"        public final BooleanLiteralMatcher isEqualTo(Boolean b) {\n" + 
					"            addPredicate(n -> b != null && n.booleanValue() == b);\n" + 
					"            return this;\n" + 
					"        }\n" + 
					""
					);
		}
		if (nodeClassName.equals("StringLiteral")) {
			String literalType = nodeClassName.replace("Literal",  "");
			w.write("\n" + 
					f("        public final %s isEqualTo(%s v) {\n", matcherClassName, literalType) + 
					t("            addPredicate(n -> v != null && v.equals(n.getLiteralValue()));\n") + 
					t("            return this;\n") + 
					t("        }\n")
					);
		}
		if (nodeClassName.equals("QualifiedName")) {
			w.write("\n" + 
					"        public final QualifiedNameMatcher isArrayLength() {\n" + 
					"            addPredicate(n ->\n" + 
					"                ASTHelper.isArray(n.getQualifier())\n" + 
					"                    && n.getName().getIdentifier().equals(\"length\"));\n" + 
					"            return this;\n" + 
					"        }\n");
		}
		w.write("    }\n");
		
		
		/*
		System.out.println(clazz.getName());
			System.out.println("    pd: " + pd);
		}
		*/
	}

	/**
	 * Generates a separate predicate method for each operator.
	 */
	private static void generateOperatorPredicates(PrintWriter w, Class<?> operatorClass, String nodeClassName) {
		for (Field f: operatorClass.getFields()) {
			if (!f.isAccessible()) {
				f.setAccessible(true);
			}
			try {
				Object o = f.get(null);
				if (operatorClass.isInstance(o)) {
					final String name = hackyCamelCase(f.getName());
					w.write("\n" + 
							f("		public final %sMatcher has%sOperator() {\n",
									nodeClassName, name) + 
							f("        	return hasOperator(%s.Operator.%s);\n", 
									nodeClassName, f.getName()) + 
							"		}\n");
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	/**
	 * Generates a separate predicate method for each primitive type.
	 * @param fields TODO
	 */
	private static void generatePrimitiveTypePredicates(PrintWriter w, Field[] fields, Class<?> enumAlikeClass, String nodeClassName) {
		for (Field f: fields) {
			if (!f.isAccessible()) {
				f.setAccessible(true);
			}
			try {
				Object o = f.get(null);
				if (enumAlikeClass.isInstance(o)) {
					final String name = hackyCamelCase(f.getName());
					w.write("\n" + 
							f("		public final %sMatcher is%s() {\n",
									nodeClassName, name) + 
							f("        	return hasCode(%s.%s);\n", 
									nodeClassName, f.getName()) + 
							"		}\n");
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	private static void generateChildProperty(PrintWriter w, String nodeClassName, String matcherClassName,
			ChildPropertyDescriptor cpd) {
		final String id = cpd.getId();
		final String visibleId = id.replace("returnType2", "returnType");
		final String childClassName = cpd.getChildType().getSimpleName();
		// also provide a simpler version taking a string when testing fo simple names
		if (childClassName.equals("SimpleName") || (childClassName.equals("Name") && nodeClassName.equals("SimpleType"))) {
			// TODO: provide hasIdentifier predicate
			w.write("\n" +
					f("        public final %s has%s(String name) {\n", 
							matcherClassName, capitalize(visibleId), childClassName) +
					f("            addPredicate(\"%s\", n -> { SimpleName sn = tryCast(n.get%s(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });\n", 
							id, capitalize(id)) +
					"            return this;\n" +
					"        }\n"
					);
		}
		w.write("\n" +
				"        @SafeVarargs\n" + 
				f("        public final %s has%s(Matcher<? extends %s>... matchers) {\n", 
						matcherClassName, capitalize(visibleId), childClassName) +
				f("            addProperty(\"%s\", %s.class, %s::get%s, matchers);\n", 
						id, childClassName, nodeClassName, capitalize(id)) +
				"            return this;\n" +
				"        }\n"
				);
		// test for presence of optional child.
		if (!cpd.isMandatory()) {
			// TODO: propertyIsNull predicate?
			w.write("\n" +
					f("        public final %s unlessHas%s() {\n", 
							matcherClassName, capitalize(visibleId)) +
					f("            addPredicate(\"%s\", n -> n.get%s() == null);\n", 
							id, capitalize(id)) +
					"            return this;\n" +
					"        }\n"
					);
		}
	}

	private static void generateChildListProperty(PrintWriter w, String nodeClassName, String matcherClassName,
			ChildListPropertyDescriptor cpd) {
		String id = cpd.getId();
		if (ASTNode.class.isAssignableFrom(cpd.getElementType())) {
			id = id.replace("extraDimensions2", "extraDimensions");
			String elementClassName = cpd.getElementType().getSimpleName();
			// has<single-element-name>
			final String singleElementName = depluralize(id);
			final String capDepId = capitalize(singleElementName);
			w.write("\n" +
					"        @SafeVarargs\n" + 
					f("        public final %s has%s(Matcher<? extends %s>... matchers) {\n", 
							matcherClassName, capDepId, elementClassName) +
					f("            addPropertyList(\"%s\", %s.class, %s::%s, matchers);\n", 
							id, elementClassName, nodeClassName, id) +
					"            return this;\n" +
					"        }\n"
					);
			// <single-element-name>CountIs
			w.write("\n" +
					f("        public final %s %sCountIs(int count) {\n", 
							matcherClassName, singleElementName) +
					f("            addCountIs(%s::%s, count);\n", 
							nodeClassName, id) +
					"            return this;\n" +
					"        }\n"
					);
			w.write("\n" +
					"        @SafeVarargs\n" + 
					f("        public final %s has%sAt(int index, Matcher<? extends %s>... matchers) {\n", 
							matcherClassName, capDepId, elementClassName) +
					f("            addPropertyListElement(\"%s\", %s.class, %s::%s, index, matchers);\n", 
							id, elementClassName, nodeClassName, id) +
					"            return this;\n" +
					"        }\n"
					);
		} else {
			System.out.println("TODO: support child list property " + cpd);
		}
	}

	private static void generateSimpleProperty(PrintWriter w, String nodeClassName, String matcherClassName,
			SimplePropertyDescriptor spd) {
		final String id = spd.getId();
		if (!"booleanValue".equals(id) && !"parentheses".equals(id)) {
			String valueType = spd.getValueType().getTypeName().replaceFirst(".*\\.",  "");
			switch (valueType) {
			case "boolean":
				w.write("\n" +
						f("        public final %s is%s() {\n", 
								matcherClassName, capitalize(id)) +
						f("            addPredicate(\"%s\", n -> n.is%s());\n", 
								id, capitalize(id)) +
						"            return this;\n" +
						"        }\n"
						);
				break;
			case "String":
			case "Assignment$Operator":
			case "InfixExpression$Operator":
			case "PostfixExpression$Operator":
			case "PrefixExpression$Operator":
				w.write("\n" +
						f("        public final %s has%s(%s v) {\n", 
								matcherClassName, capitalize(id), valueType.replace('$', '.')) +
						f("            addPredicate(\"%s\", n -> v != null && v.equals(n.get%s()));\n", 
								id, capitalize(id)) +
						"            return this;\n" +
						"        }\n"
						);
				break;
			default:	
				System.out.println("TODO: support value type " + spd.getValueType() + " (" + valueType + ") for " + spd);
			}
		} else {
			System.out.println("TODO: support value " + spd.getValueType() +  " for " + spd);
		}
	}

	private static String f(String format, Object... args) {
		return String.format(format, args);
	}

	// dummy to keep indent aligned
	private static String t(String text) {
		return text;
	}

	private static String depluralize(String id) {
		return id.endsWith("s") ? id.substring(0, id.length() - 1) : id;
	}

	private static String hackyCamelCase(String s) {
		return capitalize(s.toLowerCase())
				.replace("_a", "A").replace("_e", "E")
				.replace("_o", "O")
				.replace("_s", "S")
				.replace("_u", "U")
				.replace("_x", "X");
	}

	private static String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private static String decapitalize(String s) {
		return Character.toLowerCase(s.charAt(0)) + s.substring(1);
	}
}
