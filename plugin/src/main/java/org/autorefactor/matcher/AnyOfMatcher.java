package org.autorefactor.matcher;

import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.eclipse.jdt.core.dom.ASTNode;

class AnyOfMatcher<T extends ASTNode>
    extends CommonMatcher<T, AnyOfMatcher<T>> {

    AnyOfMatcher(Matcher<?>... matchers) {
        super(ASTNode.class, matchers);
    }

    @Override
    public boolean match(ASTNode t, BoundNodesBuilder resultBounds) {
        return InternalMatcherUtil.matchAnyOf(t, resultBounds, getMatchers());
    }
}