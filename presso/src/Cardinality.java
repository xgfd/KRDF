import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import utils.DoubleKeyHashMap;

import java.util.*;

/**
 * Created by xgfd on 05/05/2017.
 */
public class Cardinality {
    static DoubleKeyHashMap<Node, List<Triple>, Integer> cache = new DoubleKeyHashMap<>();
    static int cacheHit = 0;
    static int cacheNotHit = 0;

    static final String neighbourVar = "neighbour";

    /**
     * Calculate the number of trees defined by a collection of predicate chains that contain a vertex.
     * This number is recursively calculate by traversing the predicate chains with memorisation to reduce time complexity.
     * At each vertex the number of paths linked by the same predicate are summed, while the number of paths from different predicates are multiplied.
     *
     * @param v          A vertex in RDF
     * @param queryGraph A query graph in the form of a collection of predicate chains centered at {@code v}
     * @return The number of graphs defined by the query graph {@code queryGraph} that contain the vertex {@code v}
     * @since 1.8
     */
    static public int cardinality(Node v, Collection<List<Triple>> queryGraph) {
        //TODO
        return queryGraph.stream()
                .map(predicateChain -> cardinality(v, v, predicateChain))
                .reduce(1, (a, b) -> a * b);
    }

    /**
     * Calculate the number of paths defined by a predicate chain that go through a vertex with memorisation.
     *
     * @see #_cardinality(Node, Node, List)
     */
    static public int cardinality(Node label, Node v, List<Triple> predicateChain) {
        Integer cachedCard = cache.get(v, predicateChain);

        if (cachedCard == null) { // no cache available
            miss();
            cachedCard = _cardinality(label, v, predicateChain);
            cache.put(v, predicateChain, cachedCard);
        } else {
            hit();
        }

        return cachedCard;
    }

    static public void hit() {
        cacheHit++;
    }

    static public void miss() {
        cacheNotHit++;
    }

    static public void resetCacheStats() {
        cacheHit = 0;
        cacheNotHit = 0;
    }

    static public void clearCache() {
        cache.clear();
    }


    /**
     * Recursively calculate the number of paths defined by a given predicate chain that go through a vertex in an RDF graph. The number of paths of neighbours following the same predicate are summed.
     *
     * @param label          Corresponding node of the vertex {@code v} in the query graph from {@link #cardinality(Node, Collection)}
     * @param v              A vertex in RDF
     * @param predicateChain A predicate chain pattern that defines paths
     * @return Number of paths going through the vertex
     */
    static private int _cardinality(Node label, Node v, List<Triple> predicateChain) {

        // every node at the end of the chain contributes 1 path
        if (predicateChain.isEmpty()) {
            return 1;
        }


        int pathNum = 0;

        Triple headEdge = predicateChain.get(0);
        ResultSet neighbours = getNeighbours(label, v, headEdge);

        List<String> vars = neighbours.getResultVars();
        assert vars.get(0) == neighbourVar;

        Node neighbourLabel = otherEnd(v, headEdge);

        while (neighbours.hasNext()) {
            Node neighbour = neighbours.next().get(neighbourVar).asNode();

            /** get the number of paths from each neighbour in the RDF graph and add them to the total number of
             *  paths going through the vertex {@code v}
             */
            pathNum += cardinality(neighbourLabel, neighbour, new ArrayList<>(predicateChain.subList(1, predicateChain.size())));
        }

        return pathNum;
    }

    /**
     * @param label Corresponding node of {@code v} in {@code edge}
     * @param v     A concrete node
     * @param edge
     * @return Neighbours of {@code v} via {@code edge}
     */
    static private ResultSet getNeighbours(Node label, Node v, Triple edge) {
        assert v.isConcrete();

        Node s = edge.getSubject(), o = edge.getObject();
        assert label == s || label == o;

        Node p = edge.getPredicate();

        Triple t;
        if (label == s) {
            t = Triple.create(v, p, Var.alloc(neighbourVar));
        } else {
            t = Triple.create(Var.alloc(neighbourVar), p, v);
        }

        return RDFGraph.getNeighbours(t);
    }

    /**
     * Return the other end of an edge
     *
     * @param n    One end of an edge
     * @param edge An edge
     * @return The end of the edge that's different from {@code n}
     */
    static private Node otherEnd(Node n, Triple edge) {
        Node a = edge.getSubject(), b = edge.getObject();
        return a == n ? b : a;
    }
}

