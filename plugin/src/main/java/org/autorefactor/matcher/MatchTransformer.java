package org.autorefactor.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.autorefactor.matcher.AstMatcher.BoundNodes;
import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.matcher.AstMatcher.Matcher;

import org.autorefactor.util.Pair;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Match a list of (pattern, result transformer) and optionally create result R.
 */
public final class MatchTransformer<R> {
    private final List<Pair<Matcher<?>, Function<BoundNodes,Optional<R>>>> transformers = new ArrayList<>();

    public MatchTransformer<R> addTransformer(Matcher<?> matcher, Function<BoundNodes,Optional<R>> transformer) {
        transformers.add(Pair.of(matcher, transformer));
        return this;
    }

    public Optional<R> matchAndTransform(ASTNode node) {
        Optional<R> res = Optional.empty();
        for (Pair<Matcher<?>, Function<BoundNodes, Optional<R>>> p: transformers) {
            BoundNodesBuilder boundNodesBuilder = new BoundNodesBuilder();
            // TODO: review clear vs new
            //boundNodesBuilder.clear();
            Matcher<?> matcher = p.getFirst();
            if (matcher.match(node, boundNodesBuilder)) {
                Function<BoundNodes, Optional<R>> transformer = p.getSecond();
                res = transformer.apply(boundNodesBuilder.bindings());
                if (res.isPresent()) {
                    break;
                }
            }
        }
        return res;
    }
}