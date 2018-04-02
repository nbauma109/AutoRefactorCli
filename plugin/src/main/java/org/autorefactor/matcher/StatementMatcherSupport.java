package org.autorefactor.matcher;

import static org.autorefactor.refactoring.ASTHelper.getNextStatement;
import static org.autorefactor.refactoring.ASTHelper.getPreviousSibling;
import static org.autorefactor.refactoring.ASTHelper.getPreviousStatement;

import java.util.function.Function;

import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Common base class for statement matchers that provides navigation support.
 *
 * @param <T>	matchable AST node types
 * @param <M>   the matcher for fluent interfaces
 */
public class StatementMatcherSupport<T extends Statement, M extends StatementMatcherSupport<T,M>>
    extends CommonMatcher<T, M> {

	@SafeVarargs
    StatementMatcherSupport(Class<T> clazz, Matcher<? extends Statement>... matchers) {
        super(clazz, matchers);
    }

    // TODO: think about naming
    @SuppressWarnings("unchecked")
	@SafeVarargs
    public final M previousStatement(Matcher<? extends Statement>... innerMatchers) {
        add(AstMatcher.internalAllOf("previousStatement", Statement.class, stmt -> getPreviousStatement(stmt), innerMatchers));
        return (M) this;
    }

    @SuppressWarnings("unchecked")
	@SafeVarargs
    public final M previousSibling(Matcher<? extends Statement>... innerMatchers) {
        add(AstMatcher.internalAllOf("previousSibling", Statement.class, stmt -> getPreviousSibling(stmt), innerMatchers));
        return (M) this;
    }

    @SuppressWarnings({ "unchecked" })
	@SafeVarargs
    public final M nextStatement(Matcher<? extends Statement>... innerMatchers) {
        add(AstMatcher.internalAllOf("nextStatement", Statement.class, stmt -> getNextStatement(stmt), innerMatchers));
        return (M) this;
    }

    @SuppressWarnings("unchecked")
	public final M isEqualTo(Function<BoundNodesBuilder, Statement> otherStmt) {
        add(new Matcher<Statement>() {

            @Override
            public boolean match(ASTNode t, BoundNodesBuilder bounds) {
                return t instanceof Statement && t.equals(otherStmt.apply(bounds));
            }
        });
        return (M)this;
    }
}