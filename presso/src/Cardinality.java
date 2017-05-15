import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;

import java.util.*;

/**
 * Created by xgfd on 05/05/2017.
 */
public class Cardinality {
    static DoubleKeyHashMap<Node, ArrayList<Property>, Integer> cache = new DoubleKeyHashMap<>();

    /**
     * Calculate the number of trees defined by a collection of predicate chains that contain a vertex. This number is recursively calculate by traversing the predicate chains with memorisation to reduce time complexity. At each vertex the number of paths linked by the same predicate are summed, while the number of paths from different predicates are multiplied.
     *
     * @param v               A vertex
     * @param predicateChains A tree pattern in the form of a collection of predicate chains
     * @return The number of trees containing the vertex
     */
    public int cardinality(Node v, List<ArrayList<Property>> predicateChains) {
        return predicateChains.stream()
                .map(predicateChain -> this.cardinality(v, predicateChain))
                .reduce(1, (a, b) -> a * b);
    }

    /**
     * Calculate the number of paths defined by a given predicate chain that go through a vertex with memorisation.
     *
     * @param v              A vertex
     * @param predicateChain A predicate chain pattern that defines paths
     * @return The number of paths going through the vertex
     * @see this#_cardinality
     * @since 1.8
     */
    public int cardinality(Node v, ArrayList<Property> predicateChain) {
        Integer cachedCard = cache.get(v, predicateChain);

        if (cachedCard == null) { // no cache available
            cachedCard = _cardinality(v, predicateChain);
            cache.put(v, predicateChain, cachedCard);
        }

        return cachedCard;
    }

    /**
     * Recursively calculate the number of paths defined by a given predicate chain that go through a vertex. The number of paths of neighbours following the same predicate are summed.
     *
     * @param v              A vertex
     * @param predicateChain A predicate chain pattern that defines paths
     * @return Number of paths going through the vertex
     * @since 1.8
     */
    private int _cardinality(Node v, ArrayList<Property> predicateChain) {

        // end of the chain, each node contributes 1 path
        if (predicateChain.isEmpty()) {
            return 1;
        }

        Property headPre = predicateChain.remove(0);

        Collection<Node> neighbours = getNeighbours(v, headPre);

        if (neighbours.isEmpty()) {
            return 0; // no need for this block but to make explicit what happens when the current node doesn't go further along the head predicate; for readability purpose only
        } else {
            return neighbours.stream()
                    .map(node -> cardinality(node, new ArrayList<Property>(predicateChain)))
                    .mapToInt(Integer::intValue)
                    .sum();
        }
    }

    private Collection<Node> getNeighbours(Node v, Property predicate) {
        return RDFGraph.getNeighbours(v, predicate);
    }
}

/**
 * Helper class for double-key hash map
 *
 * @param <K> The first key
 * @param <T> The second key
 * @param <V> Value
 */
class DoubleKeyHashMap<K, T, V> {
    private HashMap<K, Map<T, V>> outterMap = new HashMap<K, Map<T, V>>();

    HashMap<K, Map<T, V>> put(K k1, T k2, V val) {
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

    V get(K k1, T k2) {
        Map<T, V> innerMap = outterMap.get(k1);

        if (innerMap == null) {
            return null;
        } else {
            return innerMap.get(k2);
        }
    }
}