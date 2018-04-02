package org.autorefactor.matcher;

import org.autorefactor.matcher.AstMatcher.Matcher;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @param <T>   node type
 */
class InternalCommonMatcher<T extends ASTNode> extends CommonMatcher<T, Matcher<T>> {

    protected InternalCommonMatcher(Class<? extends ASTNode> clazz) {
        super(clazz);
    }

    @SafeVarargs
    InternalCommonMatcher(Class<? extends ASTNode> clazz, Matcher<? extends ASTNode>... matchers) {
        super(clazz, matchers);
    }
}