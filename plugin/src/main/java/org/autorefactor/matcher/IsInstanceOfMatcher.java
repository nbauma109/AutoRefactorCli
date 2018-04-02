package org.autorefactor.matcher;

import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.refactoring.ASTHelper;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

class IsInstanceOfMatcher
    extends CommonMatcher<Expression, IsInstanceOfMatcher> {

    private final String className;

    IsInstanceOfMatcher(String className) {
        super(Expression.class);
        this.className = className;
    }

    @Override
    public boolean match(ASTNode t, BoundNodesBuilder resultBounds) {
        // TODO: check if optimizable
        return t instanceof Expression && ASTHelper.instanceOf((Expression) t, className);
    }
}