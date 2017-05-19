import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import utils.DoubleKeyHashMap;

import java.util.*;

/**
 * Created by xgfd on 05/05/2017.
 */
public class Cardinality {
    static DoubleKeyHashMap<Node, ArrayList<Triple>, Integer> cache = new DoubleKeyHashMap<>();

    /**
     * Calculate the number of trees defined by a collection of predicate chains that contain a vertex. This number is recursively calculate by traversing the predicate chains with memorisation to reduce time complexity. At each vertex the number of paths linked by the same predicate are summed, while the number of paths from different predicates are multiplied.
     * @param v               A vertex
     * @param predicateChains A tree pattern in the form of a collection of predicate chains
     * @return The number of trees containing the vertex
     */
    public int cardinality(Node v, List<ArrayList<Triple>> predicateChains) {
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
    public int cardinality(Node v, ArrayList<Triple> predicateChain) {
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
    private int _cardinality(Node v, ArrayList<Triple> predicateChain) {

        // end of the chain, each node contributes 1 path
        if (predicateChain.isEmpty()) {
            return 1;
        }

        Triple headPre = predicateChain.remove(0);

        Collection<Node> neighbours = getNeighbours(v, headPre);

        if (neighbours.isEmpty()) {
            return 0; // no need for this block but to make explicit what happens when the current node doesn't go further along the head predicate; for readability purpose only
        } else {
            return neighbours.stream()
                    .map(node -> cardinality(node, new ArrayList<Triple>(predicateChain)))
                    .mapToInt(Integer::intValue)
                    .sum();
        }
    }

    private Collection<Node> getNeighbours(Node v, Triple edge) {
        return RDFGraph.getNeighbours(v, edge);
    }
}

