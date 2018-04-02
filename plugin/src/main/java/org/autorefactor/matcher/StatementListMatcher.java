package org.autorefactor.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.autorefactor.refactoring.ASTHelper;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Statements with stripped blocks handles as a virtual list.
 */
public class StatementListMatcher
    implements Matcher<Statement> {
	// TODO: class smells fishy?

    private final List<Matcher<Statement>> matchers = new ArrayList<>();

    StatementListMatcher() {}
    
    private void add(Matcher<Statement> matcher) {
    	matchers.add(matcher);
    }

    public StatementListMatcher isNotEmpty() {
        add(new Matcher<Statement>() {

            @Override
            public boolean match(ASTNode t, BoundNodesBuilder bounds) {
                return t instanceof Statement && !ASTHelper.asList((Statement) t).isEmpty();
            }

        });
        return this;
    }

    /**
     * Returns true if any of the given statement *list* matchers match.
     */
    public StatementListMatcher anyOf(StatementListMatcher... matchers) {
        add(new Matcher<Statement>() {

            @Override
            public boolean match(ASTNode t, BoundNodesBuilder bounds) {
                return t instanceof Statement && InternalMatcherUtil.matchAnyOf(t, bounds, matchers, Collections.emptyList());
            }

        });
        return this;
    }

    /**
     * Returns true if the given matcher does not match any of the statements of list.
     */
	public StatementListMatcher unless(Matcher<? extends Statement> matcher) {
    	// TODO: optimize to avoid multiple asList calls
        add(new Matcher<Statement>() {

            @Override
            public boolean match(ASTNode t, BoundNodesBuilder bounds) {
            	if (t instanceof Statement) {
            		List<Statement> stmts = ASTHelper.asList((Statement) t);
            		for (Statement stmt: stmts) {
            			if (matcher.match(stmt, bounds)) {
            				return false;
            			}
            		}
            	}
            	return true;
            }
        });
        return this;
	}

    @Override
    public boolean match(ASTNode t, BoundNodesBuilder bounds) {
        return t instanceof Statement && InternalMatcherUtil.matchAllOf(t, bounds, matchers, Collections.emptyList());
    }

    public StatementListMatcher hasIndex(int index, Matcher<? extends Statement> m) {
    	// TODO: optimize to avoid multiple asList calls by using a statementlist matcher? 
        add(new Matcher<Statement>() {

            @Override
            public boolean match(ASTNode t, BoundNodesBuilder bounds) {
                List<Statement> stmts = t instanceof Statement ? ASTHelper.asList((Statement) t) : Collections.emptyList();
                return index < stmts.size() && m.match(stmts.get(index), bounds);
            }

        });
        return this;
    }

    public StatementListMatcher hasSize(int size) {
        add(new Matcher<Statement>() {

            @Override
            public boolean match(ASTNode t, BoundNodesBuilder bounds) {
                // TODO: memoize asList
                List<Statement> stmts = t instanceof Statement ? ASTHelper.asList((Statement) t) : Collections.emptyList();
                return size == stmts.size();
            }

        });
        return this;
    }

    @SafeVarargs
    public final StatementListMatcher lastIndex(Matcher<? extends Statement>... innerMatchers) {
        final Function<Statement, Statement> valueSupplier = stmt -> {
                        List<Statement> stmts = ASTHelper.asList(stmt);
                        return !stmts.isEmpty() ? stmts.get(stmts.size() - 1) : null;
                    };
        this.matchers.add(AstMatcher.internalAllOf("lastIndex", Statement.class, valueSupplier, innerMatchers));
        return this;
    }

    /*
    public StatementListMatcher allOf(StatementListMatcher... matchers) {
        this.add(CollectionContainsRefactoring.allOf(matchers));
        return this;
    }
    */
}