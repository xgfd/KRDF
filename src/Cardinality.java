import org.apache.jena.base.Sys;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import utils.DoubleKeyHashMap;

import java.util.*;

/**
 * Created by xgfd on 05/05/2017.
 */
public class Cardinality {
    static private DoubleKeyHashMap<Node, ELT, Integer> cache = new DoubleKeyHashMap<>();
    static private DoubleKeyHashMap<Node, DiPredicate, List<Node>> neighbourCache = new DoubleKeyHashMap<>(100000, 0.4f);
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
//        if (true) { // no cache available
            miss();
            cachedCard = _cardinality(v, elt);
            cache.put(v, elt, cachedCard);
        } else {
            hit();
        }

        return cachedCard;
    }

    static public int cardinality(ELT elt) {
        DiPredicate p = elt.getDescendantEdges().iterator().next();
        Triple t = Triple.create(Var.alloc("s"), p.asNode(), Var.alloc("o"));
        List<QuerySolution> rs = RDFGraph.execTriple(t);

        Set<Node> roots = new HashSet<>();
        rs.forEach(querySolution -> roots.add(p.isIncoming() ? querySolution.get("o").asNode() : querySolution.get("s").asNode()));

        int total = roots.stream()
                .mapToInt(node -> cardinality(node, elt)) // calculate cardinality for each node
                .sum();

        return total;
    }

    /**
     * Calculate the cardinality of a query
     *
     * @param qg
     * @return
     */
    //TODO add caching for query graphs
    static public int cardinality(QueryGraph qg) {
        ELT elt = qg.asELT();

        return cardinality(elt);
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

        int card = 1;
        for (DiPredicate p : adjacentEdges(elt)) {
            card *= cardinality(v, p, elt);
            if (card == 0) {
                return card;
            }
        }

        return card;
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
    static private int cardinality(Node v, DiPredicate p, ELT queryGraph) {
        List<Node> adjV = adjacentVertices(v, p);
        return adjV.stream() // get neighbours
                .mapToInt(n -> cardinality(n, descendantTree(p, queryGraph))) // map to neighbour's cardinality
                .sum();
    }

    static private Set<DiPredicate> adjacentEdges(ELT tree) {
        return tree.getDescendantEdges();
    }

    static private ELT descendantTree(DiPredicate p, ELT tree) {
        return tree.getChild(p);
    }

    /**
     * @param v A concrete node
     * @param p A predicate as an edge
     * @return Neighbours of {@code v} via {@code edge}
     */
    static private List<Node> adjacentVertices(Node v, DiPredicate p) {
        long ts = System.currentTimeMillis();
        assert v.isConcrete();
        List<Node> adjV;
        adjV = neighbourCache.get(v, p);

        if (adjV == null) {
            Triple t = p.isIncoming() ? Triple.create(Var.alloc(neighbourVar), p.asNode(), v) : Triple.create(v, p.asNode(), Var.alloc(neighbourVar));
            adjV = getNeighbourVal(RDFGraph.execTriple(t));
            neighbourCache.put(v, p, adjV);
        }

        return adjV;
    }

    static private List<Node> getNeighbourVal(List<QuerySolution> rs) {
        List<Node> adjV = new ArrayList<>();
        rs.forEach(querySolution -> adjV.add(querySolution.get(neighbourVar).asNode()));
        return adjV;
    }
}
