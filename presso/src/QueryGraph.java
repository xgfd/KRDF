import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import utils.DoubleKeyHashMap;

import java.util.*;

/**
 * Created by xgfd on 15/05/2017.
 */
public class QueryGraph {

    private final Node v;
    private HashMap<Node, Set<Triple>> incomingEdges = new HashMap<>();
    private HashMap<Node, Set<Triple>> outgoingEdges = new HashMap<>();

    public QueryGraph(String bgpString) {
        Query query = QueryFactory.create(bgpString);

        Element pattern = query.getQueryPattern();

        if (pattern instanceof ElementTriplesBlock) {
            ElementTriplesBlock bgp = (ElementTriplesBlock) pattern;
            Iterator<Triple> triples = bgp.patternElts();
            while (triples.hasNext()) {
                addEdge(triples.next());
            }
        } else {
            throw new IllegalArgumentException("Only support queries with a single BGP");
        }

    }

    public Set<Triple> getIncomming(Node v) {
        return incomingEdges.get(v);
    }

    public Set<Triple> getOutgoing(Node v) {
        return outgoingEdges.get(v);
    }

    /**
     * Represent an ACYCLIC query graph as a set of chains rooted at a node.
     * <p>
     * The {@code Set<List>} type calculates hash code in the way that hash codes of ordered items (i.e. those in the list) are multiplied,
     * and those of unordered items (i.e. chains in the set) are summed.
     *
     * @param v A node as the root of chains
     * @return A set of chains
     */
    public Set<List<Triple>> asChains(Node v) {
        Map<Node, List<Triple>> inChains = incomingEdges.get(v);
        Map<Node, List<Triple>> outChains = outgoingEdges.get(v);

        // collect chains from both incoming and outgoing predicates
        Collection<List<Triple>> chains = inChains.values();
        chains.addAll(outChains.values());

        return new HashSet(chains);
    }

    private void addEdge(Triple t) {
        Node s = t.getSubject(), p = t.getPredicate(), o = t.getObject();
        addIncoming(o, t);
        addOutgoing(s, t);
    }

    private void addIncoming(Node v, Triple t) {
        addTo(v, t, incomingEdges);
    }

    private void addOutgoing(Node v, Triple t) {
        addTo(v, t, outgoingEdges);
    }

    private void addTo(Node v, Triple t, Map<Node, Set<Triple>> map) {
        List<Triple> edges = map.get(v);

        if (edges == null) {
            edges = new HashSet<>();
        }

        edges.add(t);

        map.put(v, edges);
    }
}

/**
 * Class and algorithms for detecting isomorphism of edge-labeled trees.
 * A query graph (assumed to be acyclic) is represented as a edge-labeled (i.e. predicates) tree rooted at a concrete vertex,
 * which is used as an index key to retrieve cached cardinality.
 * An ELT is recursively defined as a set of ELTs decending from an anonymous node.
 */
private class ELT<E> {
    // child tress connected by incoming edges
    Set<Triple> incomings, outgoings;

    // child tress connected by outgoing edges

    public ELT(QueryGraph qg, Node root) {

    }

    @Override
    public int hashCode() {

    }


    private Set<ELT> dfs(QueryGraph qg, Node root) {
        Set<ELT> elts = new HashSet<>();

        this.incomings = qg.getIncomming(root);
        this.outgoings = qg.getoutgoing(root);



    }


}