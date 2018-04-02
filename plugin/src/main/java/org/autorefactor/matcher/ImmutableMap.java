package org.autorefactor.matcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ImmutableMap {
    public static <K,V> Map<K,V> of(K k, V v) {
        Map<K,V> m = new HashMap<K,V>(1, 1);
        m.put(k, v);
        return Collections.unmodifiableMap(m);
    }

    public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2) {
        Map<K,V> m = new HashMap<K,V>(2, 1);
        m.put(k1, v1);
        m.put(k2, v2);
        return Collections.unmodifiableMap(m);
    }

    public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K,V> m = new HashMap<K,V>(3, 1);
        m.put(k1, v1);
        m.put(k2, v2);
        m.put(k3, v3);
        return Collections.unmodifiableMap(m);
    }

    public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K,V> m = new HashMap<K,V>(4, 1);
        m.put(k1, v1);
        m.put(k2, v2);
        m.put(k3, v3);
        m.put(k4, v4);
        return Collections.unmodifiableMap(m);
    }

    public static <K,V> Map<K,V> copyOf(Map<K,V> m) {
        if (m.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K,V> newMap = new HashMap<>(m.size(), 1);
        newMap.putAll(m);
        return Collections.unmodifiableMap(newMap);
    }
}
