package org.autorefactor.matcher;

import org.autorefactor.matcher.AstMatcher.BoundNodes;

/**
 * Callback that is called on match and is directly
 * involved in the visiting process.
 */
public abstract class DirectMatchCallback {
    /**
     * Callback return to control visiting of child
     * nodes of calling visitor.
     */
    public enum Visit {
        DoNotVisitSubtree,
        VisitSubtree;

        public static Visit fromVisitorReturn(boolean visitSubtree) {
            return visitSubtree ? Visit.VisitSubtree : Visit.DoNotVisitSubtree;
        }
    }

    public abstract Visit onMatch(BoundNodes bounds);
}
