package org.autorefactor.matcher;

import java.util.Map;
import java.util.Optional;

import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.matcher.AstMatcher.BoundNodes;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.eclipse.jdt.core.dom.ASTNode;

public class MatchFinder {
    static Optional<BoundNodes> EMPTY = Optional.of(BoundNodes.EMPTY);

    public static Optional<BoundNodes> match(ASTNode node, Matcher<?> matcher) {
        BoundNodesBuilder bindings = new BoundNodesBuilder();
        boolean matched = matcher.match(node, bindings);
        return matched ? optionalOf(bindings) : Optional.empty();
    }

    public static Optional<BoundNodes> match(ASTNode node, Matcher<?> matcher, Map<String, Object> initialBindings) {
        BoundNodesBuilder bindings = new BoundNodesBuilder();
        bindings.putAll(initialBindings);
        boolean matched = matcher.match(node, bindings);
        return matched ? optionalOf(bindings) : Optional.empty();
    }

    public static boolean matches(ASTNode node, Matcher<?> matcher) {
        return matcher.match(node, new AstMatcher.BoundNodesBuilder());
    }

    static Optional<BoundNodes> optionalOf(BoundNodesBuilder bindings) {
        return bindings.isEmpty() ? EMPTY : Optional.of(bindings.bindings());
    }
}
