import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import utils.DoubleKeyHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by xgfd on 15/05/2017.
 */
public class QueryGraph {

    private DoubleKeyHashMap<Node, Node, List<Triple>> incomingEdges = new DoubleKeyHashMap<>();
    private DoubleKeyHashMap<Node, Node, List<Triple>> outgoingEdges = new DoubleKeyHashMap<>();

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

    /**
     * Represent the query graph as a collection of chains rooted at a node.
     *
     * @param v A node as the root of chains
     * @return A collection of chains
     */
    public List<ArrayList<Triple>> asChains(Node v) {
        Map<Node, List<Triple>> chains = incomingEdges.get(v);
        return null;

    }

    private void addEdge(Triple t) {
        Node s = t.getSubject(), p = t.getPredicate(), o = t.getObject();
        addIncoming(o, p, t);
        addOutgoing(s, p, t);
    }

    private void addIncoming(Node v, Node p, Triple t) {
        addTo(v, p, t, incomingEdges);
    }

    private void addOutgoing(Node v, Node p, Triple t) {
        addTo(v, p, t, outgoingEdges);
    }

    private void addTo(Node v, Node p, Triple t, DoubleKeyHashMap<Node, Node, List<Triple>> map) {
        List<Triple> edges = map.get(v, p);

        if (edges == null) {
            edges = new ArrayList();
        }

        edges.add(t);

        map.put(v, p, edges);
    }
}
