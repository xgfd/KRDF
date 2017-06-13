import com.sun.istack.internal.NotNull;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;

import java.util.*;

/**
 * Created by xgfd on 15/05/2017.
 */
class QueryGraph {

    private HashMap<Node, Set<Triple>> incomingEdges = new HashMap<>();
    private HashMap<Node, Set<Triple>> outgoingEdges = new HashMap<>();
    private PrefixMapping pm;
    private Set<Node> concreteNodes = new HashSet<>();

    static Set<Triple> visitedEdges = new HashSet<>();

    QueryGraph(Query query) {
        init(query);
    }

    @NotNull
    Set<Triple> getIncomming(Node v) {
        return incomingEdges.getOrDefault(v, new HashSet<>());
    }

    @NotNull
    Set<Triple> getOutgoing(Node v) {
        return outgoingEdges.getOrDefault(v, new HashSet<>());
    }

    Set<Node> getConcreteNodes() {
        return this.concreteNodes;
    }

    /**
     * Represent an ACYCLIC query graph as a set of chains rooted at a node.
     * The {@code Set<List>} type calculates hash code in the way that hash codes of ordered items (i.e. those in the list) are multiplied,
     * and those of unordered items (i.e. chains in the set) are summed.
     *
     * @param v A node as the root of chains
     * @return A set of chains
     */
    public ELT asELT(Node v) {
        visitedEdges.clear();
        return new ELT(this, v);
    }

    public PrefixMapping getPrefixMapping() {
        return pm;
    }

    private void init(Query query) {
        this.pm = query.getPrefixMapping();

        Element pattern = query.getQueryPattern();

        if (pattern instanceof ElementGroup) {
            List<Element> elements = ((ElementGroup) pattern).getElements();

            ElementPathBlock bgp = null;
            for (Element e : elements) {
                int bgpCount = 0;
                if (e instanceof ElementPathBlock) {
                    bgp = (ElementPathBlock) e;
                    bgpCount++;
                    if (bgpCount > 1) {
                        throw new IllegalArgumentException("Only support queries with a single BGP");
                    }
                }
            }

            if (bgp != null) {
                Iterator<TriplePath> triples = bgp.patternElts();
                while (triples.hasNext()) {
                    addEdge(triples.next().asTriple());
                }
            } else {
                throw new IllegalArgumentException("No BGP found");
            }
        }
    }

    private void addEdge(Triple t) {
        Node s = t.getSubject(), o = t.getObject();

        if (s.isConcrete()) {
            this.concreteNodes.add(s);
        }

        if (o.isConcrete()) {
            this.concreteNodes.add(o);
        }

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
        Set<Triple> edges = map.get(v);

        if (edges == null) {
            edges = new HashSet<>();
        }

        edges.add(t);

        map.put(v, edges);
    }
}

