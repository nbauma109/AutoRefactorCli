package org.autorefactor.matcher;

import static org.autorefactor.matcher.InternalMatcherUtil.matchAllOf;
import static org.autorefactor.matcher.InternalMatcherUtil.matchAnyOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.autorefactor.matcher.AstMatcher.BoundNodes;
import org.autorefactor.matcher.AstMatcher.BoundNodesBuilder;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * A Matcher that accepts parameters that can be checked against node matchers.
 */
public class ParameterizedMatcher {
    private final List<String> parameterNames;
    private final List<BiFunction<BoundNodesBuilder, Map<String,ASTNode>, Boolean>> predicates = new ArrayList<>();

    ParameterizedMatcher(List<String> parameterNames) {
        this.parameterNames = ImmutableList.copyOf(parameterNames);
    }

    @SafeVarargs
    public final <T extends ASTNode> ParameterizedMatcher hasParam(String parameterName,
            Matcher<? extends T> firstMatcher,
            Matcher<? extends T>... matchers) {
        if (!parameterNames.contains(parameterName)) {
            throw new IllegalArgumentException("undeclared parameter: " + parameterName);
        }
        List<Matcher<?>> matcherList = new ArrayList<>(matchers.length + 1);
        matcherList.add(firstMatcher);
        Collections.addAll(matcherList, matchers);
        // TODO: add "build" or "finish" methods to matchers to
        // - validate parameter references
        predicates.add(new BiFunction<BoundNodesBuilder, Map<String,ASTNode>, Boolean>() {

            @Override
            public Boolean apply(BoundNodesBuilder resultBounds, Map<String, ASTNode> arguments) {
                return matchAllOf(arguments.get(parameterName), resultBounds, matcherList);
            }
        });
        return this;
    }

    @SafeVarargs
    public final <T extends ASTNode> ParameterizedMatcher hasBound(String id, Matcher<T>... matchers) {
        predicates.add(new BiFunction<BoundNodesBuilder, Map<String,ASTNode>, Boolean>() {

            @Override
            public Boolean apply(BoundNodesBuilder resultBounds, Map<String, ASTNode> arguments) {
                Object node = resultBounds.get(id);
                return (node instanceof ASTNode) && matchAllOf((ASTNode) node, resultBounds, matchers);
            }
        });
        return this;
    }

    public ParameterizedMatcher anyOf(PBoundedMatcher... matchers) {
        List<Matcher<?>> matcherList = new ArrayList<>();
        for (PBoundedMatcher m: matchers) {
            matcherList.add((Matcher<?>) m);
        }
        predicates.add(new BiFunction<BoundNodesBuilder, Map<String,ASTNode>, Boolean>() {

            @Override
            public Boolean apply(BoundNodesBuilder resultBounds, Map<String, ASTNode> arguments) {
                return matchAnyOf(null, resultBounds, matcherList);
            }
        });
        return this;
    }

    public Optional<BoundNodes> match(Map<String,ASTNode> arguments) {
        // TODO: move method/logic to MatchFinder?
        Set<String> argNames = arguments.keySet();
        if (argNames.size() != parameterNames.size() || !arguments.keySet().containsAll(parameterNames)) {
            throw new IllegalArgumentException("parameter mismatch: expected " + parameterNames + " got " + argNames);
        }
        if (predicates.isEmpty()) {
            return MatchFinder.EMPTY;
        }
        BoundNodesBuilder bindingBuilder = new BoundNodesBuilder();
        for (BiFunction<BoundNodesBuilder, Map<String, ASTNode>, Boolean> p: predicates) {
            if (!p.apply(bindingBuilder, arguments)) {
                return Optional.empty();
            }
        }
        return MatchFinder.optionalOf(bindingBuilder);
    }
}