package org.autorefactor.matcher;

import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.eclipse.jdt.core.dom.ASTNode;

class BoundedMatcher<T extends ASTNode> implements Matcher<T> {

    private String id;
    private Matcher<?>[] matchers;

    BoundedMatcher(String id, Matcher<?>[] matchers) {
        this.id = id;
        this.matchers = matchers;
    }

    public boolean match(ASTNode t, BoundNodesBuilder resultBounds) {
        Object node = resultBounds.get(id);
        return node instanceof ASTNode && InternalMatcherUtil.matchAllOf((ASTNode) node, resultBounds, matchers);
    }
}