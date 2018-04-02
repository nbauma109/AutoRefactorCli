package org.autorefactor.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class TypeMatcher {

    /** TODO: rethink if separate class is OK */
    public static class TypeBindingMatcher {
        private List<Predicate<Object>> matchers = new ArrayList<>();

        public TypeBindingMatcher isPrimitive() {
            matchers.add(new Predicate<Object>() {

                @Override
                public boolean test(Object t) {
                    return t instanceof ITypeBinding && ((ITypeBinding) t).isPrimitive();
                }
            });
            return this;
        }

        public TypeBindingMatcher hasQualifiedName(String typeName) {
            matchers.add(new Predicate<Object>() {

                @Override
                public boolean test(Object t) {
                    return t instanceof ITypeBinding && ((ITypeBinding) t).getQualifiedName().equals(typeName);
                }
            });
            return this;
        }

        public boolean matches(Object o) {
            for (Predicate<Object> m: matchers) {
                if (!m.test(o)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static TypeBindingMatcher typeBinding() {
        return new TypeBindingMatcher();
    }
}
