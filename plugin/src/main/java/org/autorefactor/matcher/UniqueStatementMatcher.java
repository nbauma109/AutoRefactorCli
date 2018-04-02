package org.autorefactor.matcher;

import java.util.List;

import org.autorefactor.refactoring.ASTHelper;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

class UniqueStatementMatcher
extends InternalCommonMatcher<Statement> {

    UniqueStatementMatcher(AstMatcher.Matcher<? extends Statement> m) {
        super(Statement.class, m);
    }


    private Statement uniqueStmt(List<Statement> stmts) {
        if (stmts.size() == 1) {
            return stmts.get(0);
        }
        return null;
    }

    @Override
    public boolean match(ASTNode t, AstMatcher.BoundNodesBuilder resultBounds) {
        return t instanceof Statement && super.match(uniqueStmt(ASTHelper.asList((Statement)t)), resultBounds);
    }
}