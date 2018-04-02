package org.autorefactor.matcher;

import static org.autorefactor.matcher.InternalMatcherUtil.matchAllOf;
import static org.autorefactor.matcher.InternalMatcherUtil.predicateMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.autorefactor.matcher.AstMatcher.BindableMatcher;
import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Common base class for most matchers.
 * 
 * @param <T>   node type
 * @param <M>   matcher type for fluent interface
 */
public class CommonMatcher<T extends ASTNode, M extends Matcher<T>> implements BindableMatcher<T, M> {
    private final Class<? extends ASTNode> clazz;
    private final List<Matcher<?>> matchers = new ArrayList<>();
    private List<String> bindIds = new ArrayList<>();

    CommonMatcher(Class<? extends ASTNode> clazz) {
        this.clazz = clazz;
    }

    @SafeVarargs
    CommonMatcher(Class<? extends ASTNode> clazz, Matcher<? extends ASTNode>... matchers) {
        this.clazz = clazz;
        addAll(matchers);
    }

    @SafeVarargs
	final void addAll(Matcher<? extends ASTNode>... matchers) {
		for (Matcher<?> m: matchers) {
            add(m);
        }
	}

    final void add(Matcher<?> matcher) {
        if (matcher == null) {
            throw new NullPointerException("matcher");
        }
        matchers.add(matcher);
    }

    final List<Matcher<?>> getMatchers() {
        return matchers;
    }

    @SuppressWarnings("unchecked")
    public final M is(Predicate<T> predicate) {
        add(predicateMatcher((Class<T>) clazz, predicate));
        // It is a programming error if this does not hold.
        return (M) this;
    }

    @SuppressWarnings("unchecked")
	@SafeVarargs
    public final M hasParent(Matcher<? extends ASTNode>... matchers) {
        add(AstMatcher.internalAllOf("parent", ASTNode.class, n -> n.getParent(), matchers));
        // It is a programming error if this does not hold.
        return (M) this;
    }

    @Override
    public boolean match(ASTNode t, BoundNodesBuilder resultBounds) {
        if (!clazz.isInstance(t)) {
            return false;
        }
        // TODO: replace with matcher creation style
        return matchAllOf(t, resultBounds, matchers, bindIds);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final M anyOf(Matcher<? extends T>... matchers) {
        add(AstMatcher.anyOf(matchers));
        // If this fails it's a programming error.
        return (M) this;
    }

    /**
     * Executes the matcher and executes it's bindings but does not
     * care for the result.
     * Useful for adding optional binds.
     */
    @SuppressWarnings("unchecked")
    public final M maybe(Matcher<? extends T> matcher) {
        add(AstMatcher.anyOf(matcher, AstMatcher.anything()));

        // If this fails it's a programming error.
        return (M) this;
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final M allOf(Matcher<? extends T>... matchers) {
        addAll(matchers);
        // If this fails it's a programming error.
        return (M) this;
    }

    @SuppressWarnings("unchecked")
	public final M that(Matcher<? extends T> matcher) {
        this.add(matcher);
        // If this fails it's a programming error.
        return (M) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final M bind(String id) {
        bindIds.add(id);
        // If this fails it's a programming error.
        return (M) this;
    }

    // TODO: make more generic
    @SuppressWarnings("unchecked")
	public final M log(String msg) {
    	addPredicate(n -> {System.out.println(msg); return true;});
    	return (M) this;
    }
    
    @SuppressWarnings("unchecked")
	protected final void addPredicate(Predicate<T> condition) {
        add(InternalMatcherUtil.predicateMatcher((Class<T>)clazz, condition));
    }
    
    @SuppressWarnings("unchecked")
	protected final void addPredicate(String name, Predicate<T> condition) {
        add(InternalMatcherUtil.predicateMatcher(name, (Class<T>)clazz, condition));
    }

    @SuppressWarnings("unchecked")
	protected final	<PT extends ASTNode>
	void addProperty(
    		String name,
            Class<PT> propertyClazz,
            Function<T, PT> property,
            Matcher<? extends PT>[] matchers) {
        add(InternalMatcherUtil.property(name, (Class<T>)clazz, propertyClazz, property, matchers));
    }
    
    @SuppressWarnings("unchecked")
    protected final <PT extends ASTNode>
    void addPropertyList(
    		String name,
            Class<PT> propertyClazz,
            Function<T,List<PT>> properties,
            Matcher<? extends PT>[] matchers) {
        add(InternalMatcherUtil.propertyList(name, (Class<T>)clazz, propertyClazz, properties, matchers));
    }
    
    @SuppressWarnings("unchecked")
    protected final <PT extends ASTNode>
    void addPropertyListElement(
    		String name,
            Class<PT> propertyClazz,
            Function<T,List<PT>> properties,
            int index,
            Matcher<? extends PT>[] matchers) {
        add(InternalMatcherUtil.propertyListElement(name, (Class<T>)clazz, propertyClazz, properties, index, matchers));
    }
    
	private static int size(List<?> l) {
		return l != null ? l.size() : 0;
	}

    @SuppressWarnings("unchecked")
	protected final void addCountIs(Function<T, List<?>> getter, int count) {
		add(predicateMatcher((Class<T>)clazz, n -> size(getter.apply(n)) == count));
	}
}