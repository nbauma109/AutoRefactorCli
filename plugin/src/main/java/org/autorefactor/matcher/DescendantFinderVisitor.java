package org.autorefactor.matcher;

import static org.autorefactor.refactoring.ASTHelper.DO_NOT_VISIT_SUBTREE;
import static org.autorefactor.refactoring.ASTHelper.VISIT_SUBTREE;

import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.autorefactor.refactoring.FinderVisitor;
import org.eclipse.jdt.core.dom.ASTNode;

class DescendantFinderVisitor extends FinderVisitor<Boolean> {
    private final Matcher<?> matcher;
	private final BoundNodesBuilder resultBounds;

    public DescendantFinderVisitor(Matcher<?> matcher, BoundNodesBuilder bounds) {
        this.matcher = matcher;
		this.resultBounds = bounds;
    }

    @Override
    public boolean preVisit2(ASTNode n) {
    	// TODO: optimize! avoid calling for every node if matcher is specific for some node!
    	BoundNodesBuilder bounds = resultBounds.copy();
        if (matcher.match(n, resultBounds)) {
        	resultBounds.set(bounds);
            setResult(true);
            return DO_NOT_VISIT_SUBTREE;
        }
        return VISIT_SUBTREE;
    }
}