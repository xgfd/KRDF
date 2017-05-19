package utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for double-key hash map
 *
 * @param <K> The first key
 * @param <T> The second key
 * @param <V> Value
 */
public class DoubleKeyHashMap<K, T, V> {
    private HashMap<K, Map<T, V>> outterMap = new HashMap<K, Map<T, V>>();

    public HashMap<K, Map<T, V>> put(K k1, T k2, V val) {
        if (outterMap.get(k1) == null) {
            HashMap<T, V> innerMap = new HashMap<T, V>();
            innerMap.put(k2, val);
            outterMap.put(k1, innerMap);
        } else {
            Map<T, V> innerMap = outterMap.get(k1);
            innerMap.put(k2, val);
        }

        return outterMap;
    }

    public V get(K k1, T k2) {
        Map<T, V> innerMap = outterMap.get(k1);

        if (innerMap == null) {
            return null;
        } else {
            return innerMap.get(k2);
        }
    }

    public Map<T, V> get(K k1) {
        Map<T, V> innerMap = outterMap.get(k1);
        return innerMap;
    }
}
