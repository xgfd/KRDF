package utils;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

/**
 * Helper class for double-key hash map implemented with LFU cache
 *
 * @param <K> The first key
 * @param <T> The second key
 * @param <V> Value
 */
public class DoubleKeyHashMap<K, T, V> {
//    private HashMap<K, Map<T, V>> outterMap = new HashMap<K, Map<T, V>>();
//
//    @NotNull
//    public HashMap<K, Map<T, V>> put(K k1, T k2, V val) {
//        if (outterMap.get(k1) == null) {
//            HashMap<T, V> innerMap = new HashMap<T, V>();
//            innerMap.put(k2, val);
//            outterMap.put(k1, innerMap);
//        } else {
//            Map<T, V> innerMap = outterMap.get(k1);
//            innerMap.put(k2, val);
//        }
//
//        return outterMap;
//    }
//
//    @Nullable
//    public V get(K k1, T k2) {
//        Map<T, V> innerMap = outterMap.get(k1);
//
//        if (innerMap == null) {
//            return null;
//        } else {
//            return innerMap.get(k2);
//        }
//    }
//
//    @NotNull
//    public Map<T, V> get(K k1) {
//        return outterMap.getOrDefault(k1, new HashMap<>());
//    }
//
//    public int size() {
//        return outterMap.keySet().stream()
//                .map(k -> outterMap.get(k))
//                .mapToInt(map -> map.size())
//                .sum();
//    }
//
//    public void clear() {
//        outterMap.clear();
//    }

    private LFUCache<Key<K, T>, V> lfuCache;

    public DoubleKeyHashMap() {
        this(1000000, 0.3f);
    }

    public DoubleKeyHashMap(int maxCacheSize, float evicitionFactor) {
        this.lfuCache = new LFUCache<>(maxCacheSize, evicitionFactor);
    }

    @NotNull
    public DoubleKeyHashMap<K, T, V> put(K k1, T k2, V val) {
        lfuCache.put(new Key<>(k1, k2), val);
        return this;
    }

    @Nullable
    public V get(Object k1, Object k2) {
        return lfuCache.get(new Key<>(k1, k2));
    }

    public int size() {
        return lfuCache.size();
    }

    public void clear() {
        lfuCache.clear();
    }

}

class Key<K, T> {
    private K k1;
    private T k2;

    Key(K k1, T k2) {
        this.k1 = k1;
        this.k2 = k2;
    }

    @Override
    public int hashCode() {
        return k1.hashCode() * 31 + k2.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Key) {
            Key k = (Key) o;
            return this.k1.equals(k.k1) && this.k2.equals(k.k2);
        } else {
            return false;
        }
    }

    public K getK1() {
        return this.k1;
    }

    public T getK2() {
        return this.k2;
    }
}
