package org.autorefactor.matcher;

import java.util.List;
import java.util.ArrayList;

import org.autorefactor.matcher.AstMatcher.Matcher;
import org.autorefactor.util.Pair;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * Match nodes using {@link AstMatcher.Matcher} typically
 * against translation units.
 * The matching process is tightly integrated with the visiting process
 * and directly calls {@link DirectMatchCallback} that can influence
 * the visiting process.
 */
public class DirectMatchFinder {
    private final List<Pair<Matcher<?>, DirectMatchCallback>> callbacks = new ArrayList<>();

    /**
     * Register callback for given matcher.
     * Matchers will be tried in execution order.
     *
     * @returns this for fluent initialization style
     */
    public DirectMatchFinder addMatcher(Matcher<?> matcher, DirectMatchCallback callback) {
        callbacks.add(Pair.of(matcher, callback));
        return this;
    }

    /** creates direct visitor implementing the match logic */
    public ASTVisitor createVisitor() {
        return new DirectMatchVisitor(callbacks);
    }
}
