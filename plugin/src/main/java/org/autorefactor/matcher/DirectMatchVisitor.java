package org.autorefactor.matcher;

import static org.autorefactor.refactoring.ASTHelper.DO_NOT_VISIT_SUBTREE;
import static org.autorefactor.refactoring.ASTHelper.VISIT_SUBTREE;

import java.util.ArrayList;
import java.util.List;

import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.autorefactor.matcher.DirectMatchCallback.Visit;
import org.autorefactor.util.Pair;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

class DirectMatchVisitor extends ASTVisitor {
    private final List<Pair<Matcher<?>, DirectMatchCallback>> callbacks;

    DirectMatchVisitor(List<Pair<Matcher<?>, DirectMatchCallback>> callbacks) {
        this.callbacks = new ArrayList<>(callbacks);
    }

    /**
     * Return true if visit(node) should be called.
     */
    @Override
    public boolean preVisit2(ASTNode node) {
        //System.out.println("dmv: preVisit2, node=" + node.getClass().getName());
        if (callbacks.isEmpty()) {
            return false;
        }
        BoundNodesBuilder boundNodesBuilder = new BoundNodesBuilder();
        for (Pair<Matcher<?>, DirectMatchCallback> e: callbacks) {
            Matcher<?> matcher = e.getFirst();
            boundNodesBuilder.clear();
            if (matcher.match(node, boundNodesBuilder)) {
                //System.out.println("match: " + node.getClass().getName());
                boundNodesBuilder.put("root", node);
                // TODO: think about calling all matches or directing via callback
                DirectMatchCallback callback = e.getSecond();
                // TODO: fix logic about visit node/visit sub tree
                if (callback.onMatch(boundNodesBuilder.bindings()) != Visit.VisitSubtree) {
                    //System.out.println("dmv.previsit2: do not visit node");
                    return false; // do not visit node
                }
            }
        }
        return true; // visit node
    }


}
