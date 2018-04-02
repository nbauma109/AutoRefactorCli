package org.autorefactor.matcher;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: improve package/class structure
class PrivateAstMatcher {

	/**
     * Read only view on BindigMap. Helps in avoiding write access for shared maps.
     */
    static interface BoundNodesMapRead {

        boolean isEmpty();

        /** returns writable copy */
        BoundNodesMap copy();

        /** hack: throws IllegalArgumentException if more than one bounded map is registered */
        /* @Nullable */ Object getSingle(String id);

        void addAllTo(BoundNodesMap target);

        List<Map<String, Object>> snapshot();

    }
    

    /**
    * Modifiable list of binding maps.
    */
    static class BoundNodesMap implements BoundNodesMapRead {
        // TODO: optimize map memory foot print? List for small sizes? optimized map config?
        private final List<Map<String,Object>> bindings = new ArrayList<>();

        BoundNodesMap() {}

        public boolean isEmpty() {
            return bindings.isEmpty();
        }

        void addAll(BoundNodesMapRead other) {
            other.addAllTo(this);
        }

        /**
         * Add binding of "o" to "id"
         */
        void put(String id, Object o) {
            if (bindings.isEmpty()) {
                bindings.add(new HashMap<>());
            }
            for (Map<String,Object> m: bindings) {
                m.put(id, o);
            }
        }

        /**
         * Add bindings
         */
        void putAll(Map<String,Object> map) {
            if (bindings.isEmpty()) {
                bindings.add(new HashMap<>());
            }
            for (Map<String,Object> m: bindings) {
                m.putAll(map);
            }
        }

        public BoundNodesMap copy() {
            BoundNodesMap bm = new BoundNodesMap();
            bm.addAll(this);
            return bm;
        }

        // TODO: remove hack
        public Object getSingle(String id) {
            if (bindings.size() != 1) {
                throw new IllegalStateException("unexpected size: " + bindings.size()
                + "\nbindings: " + bindings);
            }
            return bindings.get(0).get(id);
        }

        @Override
        public String toString() {
            return bindings.toString();
        }

        @Override
        public void addAllTo(BoundNodesMap target) {
            if (!bindings.isEmpty()) {
                for (Map<String,Object> m: bindings) {
                    target.add(new HashMap<>(m));
                }
            }
        }

        private void add(Map<String,Object> map) {
            bindings.add(map);
        }

        @Override
        public List<Map<String, Object>> snapshot() {
            return bindings.isEmpty()
                    ? Collections.emptyList()
                    : bindings.stream().map(ImmutableMap::copyOf).collect(toList());
        }
    }
}
