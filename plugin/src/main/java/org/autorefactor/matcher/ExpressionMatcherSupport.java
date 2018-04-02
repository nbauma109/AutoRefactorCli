package org.autorefactor.matcher;

import java.util.function.Function;

import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.autorefactor.matcher.TypeMatcher.TypeBindingMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Common base class for expression matchers that common methods.
 *
 * @param <T>	matchable AST node types
 * @param <M>   the matcher for fluent interfaces
 */
class ExpressionMatcherSupport<T extends Expression, M extends ExpressionMatcherSupport<T,M>>
    extends CommonMatcher<T, M> {

	@SafeVarargs
    ExpressionMatcherSupport(Class<T> clazz, Matcher<? extends Expression>... matchers) {
        super(clazz, matchers);
    }

    @SuppressWarnings("unchecked")
    public final M isInstanceOf(String className) {
        add(AstMatcher.isInstanceOf(className));
        return (M) this;
    }

    @SuppressWarnings("unchecked")
	public final M isEqualTo(Function<BoundNodesBuilder, Expression> otherExpr) {
        add(new Matcher<Expression>() {

            @Override
            public boolean match(ASTNode t, BoundNodesBuilder bounds) {
                return t instanceof Expression && t.equals(otherExpr.apply(bounds));
            }
        });
        return (M)this;
    }

    // TODO: make more generic and simplify
    // TODO: check doc of method resolveConstantExpressionValue what else can happen here 
    @SuppressWarnings("unchecked")
	public final <C,V> M hasConstantExpressionValue( 
    		Class<C> constantClass, Function<C,V> accessor, V value) { 
    	addPredicate(n -> { 
    		final Object o = n.resolveConstantExpressionValue(); 
    		final V v = constantClass.isInstance(o) ? accessor.apply(constantClass.cast(o)) : null; 
    		return v != null && v.equals(value); 
    	}); 
    	return (M) this; 
    } 

    @SuppressWarnings("unchecked")
	public final M hasTypeBinding(TypeBindingMatcher m) { 
    	addPredicate( 
    			n -> { 
    				final ITypeBinding tb = n.resolveTypeBinding(); 
    				return tb != null && m.matches(tb); 
    			}); 
    	return (M) this; 
    } 
}