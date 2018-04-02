package org.autorefactor.matcher;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public final class InternalMatcherUtil {

    /**
     * Implements "allOf" aka "AND" semantics.
     *
     * All given matchers must match for success.
     */
    static boolean matchAllOf(ASTNode t, BoundNodesBuilder resultBounds, List<? extends Matcher<?>> matchers) {
        return matchAllOf(t, resultBounds, matchers, Collections.emptyList());
    }

    /**
     * Implements "allOf" aka "AND" semantics.
     *
     * All given matchers must match for success.
     */
    static boolean matchAllOf(ASTNode t, BoundNodesBuilder resultBounds, List<? extends Matcher<?>> matchers,
            List<String> bindIds) {
        BoundNodesBuilder bounds = resultBounds.copy();
        // TODO: rethink if empty allOf is true or false
        boolean matched = true;
        // implements "allOf" semantics
        for (Matcher<?> m: matchers) {
            BoundNodesBuilder mBounds = bounds.copy();
            if (!m.match(t, mBounds)) {
                return false;
            }
            // TODO: make real tests and rethink this
            //bounds.addMatch(mBounds);
            bounds.set(mBounds);
            matched = true;
        }
        if (matched) {
            if (!bindIds.isEmpty()) {
                for (String id: bindIds) {
                    bounds.put(id, t);
                }
            }
            resultBounds.set(bounds);
        }
        return matched;
    }

    /**
     * Implements "allOf" aka "AND" semantics.
     *
     * All given matchers must match for success.
     * Array variant to avoid wrapping.
     */
    static boolean matchAllOf(ASTNode t, BoundNodesBuilder resultBounds, Matcher<?>[] matchers) {
        return matchAllOf(t, resultBounds, matchers, Collections.emptyList());
    }

    /**
     * Implements "allOf" aka "AND" semantics.
     *
     * All given matchers must match for success.
     * Array variant to avoid wrapping.
     */
    static boolean matchAllOf(ASTNode t, BoundNodesBuilder resultBounds, Matcher<?>[] matchers, List<String> bindIds) {
        BoundNodesBuilder bounds = resultBounds.copy();
        // implements "allOf" semantics
        // TODO: is loop inner copy needed?
        for (Matcher<?> m: matchers) {
            BoundNodesBuilder mBounds = bounds.copy();
            if (!m.match(t, mBounds)) {
                return false;
            }
            // TODO: make real tests and rethink this
            //bounds.addMatch(mBounds);
            bounds.set(mBounds);
        }
        // TODO: Abgleich von List und Array-Methode
        if (!bindIds.isEmpty()) {
            for (String id: bindIds) {
                bounds.put(id, t);
            }
        }
        resultBounds.set(bounds);
        return true;
    }

    /**
     * Implements "anyOf" aka "OR" semantics.
     *
     * The given matchers are checked in order until one matches.
     * One must match for success.
     * Array variant to avoid wrapping.
     */
    static boolean matchAnyOf(ASTNode t, BoundNodesBuilder resultBounds, List<Matcher<?>> matchers) {
        return matchAnyOf(t, resultBounds, matchers, Collections.emptyList());
    }

    /**
     * Implements "anyOf" aka "OR" semantics.
     *
     * The given matchers are checked in order until one matches.
     * One must match for success.
     * Array variant to avoid wrapping.
     */
    static boolean matchAnyOf(ASTNode t, BoundNodesBuilder resultBounds, List<Matcher<?>> matchers,
            List<String> bindIds) {
        for (Matcher<?> m: matchers) {
            BoundNodesBuilder mBounds = resultBounds.copy();
            if (m.match(t, mBounds)) {
                if (!bindIds.isEmpty()) {
                    for (String id: bindIds) {
                        mBounds.put(id, t);
                    }
                }
                resultBounds.set(mBounds);
                return true;
            }
        }
        return false;
    }

    /**
     * Implements "anyOf" aka "OR" semantics.
     *
     * The given matchers are checked in order until one matches.
     * One must match for success.
     * Array variant to avoid wrapping.
     */
    static boolean matchAnyOf(ASTNode t, BoundNodesBuilder resultBounds, Matcher<?>[] matchers,
            List<String> bindIds) {
        for (Matcher<?> m: matchers) {
            BoundNodesBuilder mBounds = resultBounds.copy();
            if (m.match(t, mBounds)) {
                if (!bindIds.isEmpty()) {
                    for (String id: bindIds) {
                        mBounds.put(id, t);
                    }
                }
                resultBounds.set(mBounds);
                return true;
            }
        }
        return false;
    }

    /**
     * Match a computed expression based on node.
     *
     * @param <T>   node type
     */
    static class ComputedExpressionMatcher<T extends ASTNode, E extends ASTNode> implements Matcher<T> {

        private final String name;
        private final Class<T> nodeClass;
        private final Function<T, E> valueSupplier;
        private final Matcher<? extends E>[] matchers;

        public ComputedExpressionMatcher(
                String name,
                Class<T> nodeClass,
                Function<T, E> valueSupplier,
                Matcher<? extends E>[] matchers) {
            this.name = name;
            this.nodeClass = nodeClass;
            this.valueSupplier = valueSupplier;
            this.matchers = matchers;
        }

        @Override
        public boolean match(ASTNode t, BoundNodesBuilder resultBounds) {
            if (nodeClass.isInstance(t)) {
                return matchAllOf(valueSupplier.apply(nodeClass.cast(t)), resultBounds, matchers);
            }
            return false;
        }

        @Override
        public String toString() {
            return "ComputedExpressionMatcher[" + name + ", " + nodeClass.getName() + "]";
        }
    }

    /**
     * Helper factory method for matching nodeClass dependent expression.
     */
    static <T extends ASTNode> Matcher<T> predicateMatcher(Class<T> nodeClass, Predicate<T> condition) {
        return new Matcher<T>() {
            @Override
            public boolean match(ASTNode t, BoundNodesBuilder resultBounds) {
                if (nodeClass.isInstance(t)) {
                    return condition.test(nodeClass.cast(t));
                }
                return false;
            }
        };
    }


    /**
     * Helper factory method for matching nodeClass dependent expression.
     * 
     * TODO: use name
     */
    static <T extends ASTNode> Matcher<T> predicateMatcher(String name, Class<T> nodeClass, Predicate<T> condition) {
        return new Matcher<T>() {
            @Override
            public boolean match(ASTNode t, BoundNodesBuilder resultBounds) {
                if (nodeClass.isInstance(t)) {
                    return condition.test(nodeClass.cast(t));
                }
                return false;
            }
        };
    }

    static String location(ASTNode node) {
        try {
            CompilationUnit cu = (CompilationUnit) node.getRoot();
            // starts with 1
            int startLine = cu.getLineNumber(node.getStartPosition());
            int startCol = cu.getColumnNumber(node.getStartPosition()) + 1;
            int endLine = cu.getLineNumber(node.getStartPosition() + node.getLength());
            int endCol = cu.getColumnNumber(node.getStartPosition() + node.getLength()) + 1;
            return ((startLine != endLine || startCol != endCol)
                            ?  ":(" + startLine + "." + startCol + "-" + endLine + "," + endCol + ")"
                            : ":(" + startLine + "." + startCol + ")");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

	static <T extends ASTNode,PT extends ASTNode>
	SubPropertyListMatcher<T,PT> propertyList(
			String name,
	        Class<T> nodeClazz,
	        Class<PT> propertyClazz,
	        Function<T,List<PT>> properties,
	        AstMatcher.Matcher<? extends PT>[] matchers) {
	    return new SubPropertyListMatcher<>(name, nodeClazz, propertyClazz, properties, matchers);
	}

    private static class SubPropertyMatcher<T extends ASTNode,PT extends ASTNode> extends InternalCommonMatcher<T> {
        private final Class<T> clazz;
        private final Function<T, PT> property;

        public SubPropertyMatcher(Class<T> clazz,
                Class<PT> pClazz,
                Function<T,PT> property) {
            super(pClazz);
            this.clazz = clazz;
            this.property = property;
        }

        public SubPropertyMatcher(Class<T> clazz,
                Class<PT> pClazz,
                Function<T,PT> property,
                Matcher<? extends PT>[] matchers) {
            super(pClazz, matchers);
            this.clazz = clazz;
            this.property = property;
        }

        @Override
        public boolean match(ASTNode t, BoundNodesBuilder resultBounds) {
            return clazz.isInstance(t) && super.match(property.apply(clazz.cast(t)), resultBounds);
        }
    }

    /*
    private static <T extends ASTNode,PT extends ASTNode>
    SubPropertyMatcher<T,PT> property(
            Class<T> nodeClazz,
            Class<PT> propertyClazz,
            Function<T, PT> property) {
        return new SubPropertyMatcher<>(nodeClazz, propertyClazz, property);
    }
    */

    static <T extends ASTNode,PT extends ASTNode>
    SubPropertyMatcher<T,PT> property(
            Class<T> nodeClazz,
            Class<PT> propertyClazz,
            Function<T, PT> property,
            Matcher<? extends PT>[] matchers) {
        return new SubPropertyMatcher<>(nodeClazz, propertyClazz, property, matchers);
    }


    static <T extends ASTNode,PT extends ASTNode>
    SubPropertyMatcher<T,PT> property(
    		String name,
            Class<T> nodeClazz,
            Class<PT> propertyClazz,
            Function<T, PT> property,
            Matcher<? extends PT>[] matchers) {
    	// TODO: pass name
        return new SubPropertyMatcher<>(nodeClazz, propertyClazz, property, matchers);
    }

    // TODO: fishy typing?
    static class SubPropertyListMatcher<T extends ASTNode,PT extends ASTNode> extends InternalCommonMatcher<T> {
        private final Class<T> clazz;
        private final Function<T, List<PT>> properties;

        public SubPropertyListMatcher(Class<T> clazz,
                Class<PT> pClazz,
                Function<T,List<PT>> properties,
                Matcher<? extends PT>[] matchers) {
            super(pClazz, matchers);
            this.clazz = clazz;
            this.properties = properties;
        }

        // TODO: use name
        public SubPropertyListMatcher(String name, Class<T> clazz,
                Class<PT> pClazz,
                Function<T,List<PT>> properties,
                Matcher<? extends PT>[] matchers) {
            super(pClazz, matchers);
            this.clazz = clazz;
            this.properties = properties;
        }

        @Override
        public boolean match(ASTNode t, BoundNodesBuilder resultBounds) {
            if (clazz.isInstance(t)) {
                for (PT p: properties.apply(clazz.cast(t))) {
                    BoundNodesBuilder innerBounds = resultBounds.copy();
                    if (super.match(p, innerBounds)) {
                        resultBounds.set(innerBounds);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    static <T extends ASTNode,PT extends ASTNode>
    SubPropertyListMatcher<T,PT> propertyList(
            Class<T> nodeClazz,
            Class<PT> propertyClazz,
            Function<T,List<PT>> properties,
            Matcher<? extends PT>[] matchers) {
        return new SubPropertyListMatcher<>(nodeClazz, propertyClazz, properties, matchers);
    }

    // TODO: fishy typing?
    private static class PropertyElementMatcher<T extends ASTNode,PT extends ASTNode> extends InternalCommonMatcher<T> {
        private final Class<T> clazz;
        private final Function<T, List<PT>> properties;
        private final int index;

        public PropertyElementMatcher(Class<T> clazz,
                Class<PT> pClazz,
                Function<T,List<PT>> properties,
                int index,
                Matcher<? extends PT>[] matchers) {
            super(pClazz, matchers);
            this.clazz = clazz;
            this.properties = properties;
            this.index = index;
        }

        @Override
        public boolean match(ASTNode t, BoundNodesBuilder resultBounds) {
            if (clazz.isInstance(t)) {
                List<PT> l = properties.apply(clazz.cast(t));
                return l != null && index < l.size()
                        && super.match(l.get(index), resultBounds);
            }
            return false;
        }
    }

    // TODO: use name
    static <T extends ASTNode,PT extends ASTNode>
    PropertyElementMatcher<T,PT> propertyListElement(
    		String name,
            Class<T> nodeClazz,
            Class<PT> propertyClazz,
            Function<T, List<PT>> properties,
            int index,
            Matcher<? extends PT>[] matchers) {
        return new PropertyElementMatcher<>(nodeClazz, propertyClazz, properties, index, matchers);
    }

	static <T extends ASTNode> AstMatcher.Matcher<T> countIs(
			Class<T> nodeClazz, 
			Function<T, List<?>> getter, 
			int count) {
		return predicateMatcher(nodeClazz, n -> InternalMatcherUtil.size(getter.apply(n)) == count);
	}

	static int size(List<?> l) {
		return l != null ? l.size() : 0;
	}

	static <O,R extends O> R tryCast(O o, Class<R> clazz) {
	    return clazz.isInstance(o) ? clazz.cast(o) : null;
	}
}
