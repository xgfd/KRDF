import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import utils.DoubleKeyHashMap;

import java.util.*;

/**
 * Created by xgfd on 05/05/2017.
 */
//TODO update cardinality calculation
public class Cardinality {
    static private DoubleKeyHashMap<Node, ELT, Integer> cache = new DoubleKeyHashMap<>();
    static int cacheHit = 0;
    static int cacheMiss = 0;

    static private final String neighbourVar = "neighbour";

    /**
     * Calculate the number of trees defined by a query graph that contain a certain vertex.
     * This number is recursively calculate by traversing the predicates of the query graph with memorisation to reduce time complexity.
     *
     * @param v   A vertex in RDF
     * @param elt A query graph represented as a tree rooted at {@code v}
     * @return The number of graphs defined by the query graph {@code elt} that contain the vertex {@code v}
     * @see #_cardinality(Node, ELT)
     * @since 1.8
     */
    static public int cardinality(Node v, ELT elt) {
        Integer cachedCard = cache.get(v, elt);

        if (cachedCard == null) { // no cache available
            miss();
            cachedCard = _cardinality(v, elt);
            cache.put(v, elt, cachedCard);
        } else {
            hit();
        }

        return cachedCard;
    }

    static public void hit() {
        cacheHit++;
    }

    static public void miss() {
        cacheMiss++;
    }

    static public int cacheSize() {
        return cache.size();
    }

    static public void resetCacheStats() {
        cacheHit = 0;
        cacheMiss = 0;
    }

    static public void clearCache() {
        cache.clear();
    }

    /**
     * Recursively calculate the number of paths defined by a given predicate chain that go through a vertex in an RDF graph.
     * At each vertex the number of paths (sub-graphs) linked by the same predicate are summed,
     * while the number of paths from different predicates are multiplied.
     *
     * @param v   A vertex in RDF
     * @param elt
     * @return Number of paths going through the vertex
     */
    static private int _cardinality(Node v, ELT elt) {

        // every node at the end of the chain contributes 1 path
        if (elt.isEmpty()) {
            return 1;
        }

        Set<Node> inEdges = adjacentEdgesIn(elt);
        int inCard = inEdges.stream()
                .mapToInt(p -> cardinalityIn(v, p, elt)) // map each edge to the cardinality;
                .reduce(1, (a, b) -> a * b); // product

        Set<Node> outEdges = adjacentEdgesOut(elt);
        int outCard = outEdges.stream()
                .mapToInt(p -> cardinalityOut(v, p, elt))
                .reduce(1, (a, b) -> a * b);

        return inCard * outCard;
    }

    /**
     * Cardinality of a node {@code v} from a specific edge {@code p}.
     * It's calculated as the sum of cardinality of its neighbours linked via {@code p}.
     *
     * @param v
     * @param p
     * @param queryGraph
     * @return
     */
    static private int cardinalityIn(Node v, Node p, ELT queryGraph) {
        return adjacentVerticesIn(v, p).stream() // get neighbours
                .mapToInt(n -> cardinality(n, descendantTreeIn(p, queryGraph))) // map to neighbour's cardinality
                .sum();
    }

    static private Set<Node> adjacentEdgesIn(ELT tree) {
        return tree.getIncomingELTs().keySet();
    }

    static private ELT descendantTreeIn(Node p, ELT tree) {
        return tree.getIncomingELTs().get(p);
    }

    /**
     * @param v A concrete node
     * @param p A predicate as an edge
     * @return Neighbours of {@code v} via {@code edge}
     */
    static private List<Node> adjacentVerticesIn(Node v, Node p) {
        assert v.isConcrete();
        assert p.isConcrete();

        Triple t = Triple.create(Var.alloc(neighbourVar), p, v);
        return solutionToNodes(RDFGraph.getNeighbours(t));
    }

    //********************* same methods as above for outgoing edges ***********************************

    static private int cardinalityOut(Node v, Node p, ELT queryGraph) {
        return adjacentVerticesOut(v, p).stream() // get neighbours
                .mapToInt(n -> cardinality(n, descendantTreeOut(p, queryGraph))) // map to neighbour's cardinality
                .sum();
    }

    static private Set<Node> adjacentEdgesOut(ELT tree) {
        return tree.getOutgoingELTs().keySet();
    }

    static private ELT descendantTreeOut(Node p, ELT tree) {
        return tree.getOutgoingELTs().get(p);
    }

    static private List<Node> adjacentVerticesOut(Node v, Node p) {
        assert v.isConcrete();
        assert p.isConcrete();

        Triple t = Triple.create(v, p, Var.alloc(neighbourVar));
        return solutionToNodes(RDFGraph.getNeighbours(t));
    }

    static private List<Node> solutionToNodes(ResultSet rs) {
        List<Node> adjV = new ArrayList<>();
        rs.forEachRemaining(querySolution -> adjV.add(querySolution.get(neighbourVar).asNode()));
        return adjV;
    }
}
