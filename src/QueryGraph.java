import com.sun.istack.internal.NotNull;
import org.apache.jena.graph.Node;
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
    private Set<Node> varNodes = new HashSet<>();

    static Set<Triple> visitedEdges = new HashSet<>();

    QueryGraph(Query query) {
        init(query);
    }

    QueryGraph(ElementPathBlock bgp) {
        init(bgp);
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
     * Represent an acyclic query graph as a rooted tree.
     * The {@code Set<List>} type calculates hash code in the way that hash codes of ordered sub-trees (i.e. those in the list) are multiplied,
     * and those of unordered items (i.e. those in the set) are summed.
     *
     * @param v A node as the root of chains
     * @return A set of chains
     */
    public ELT asELT(Node v) {
        visitedEdges.clear();
        return new ELT(this, v);
    }

    /**
     * Select an arbitrary node (concrete or variable) and represent the query graph as a tree rooted at that node.
     *
     * @return
     * @see #asELT(Node)
     */
    public ELT asELT() {
        visitedEdges.clear();
        Node v = this.concreteNodes.size() > 0 ? this.concreteNodes.iterator().next() : this.varNodes.iterator().next();
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
                        throw new IllegalArgumentException("Only support yago_queries with a single BGP");
                    }
                }
            }

            if (bgp != null) {
                init(bgp);
            } else {
                throw new IllegalArgumentException("No BGP found");
            }
        }
    }

    private void init(ElementPathBlock bgp) {
        Iterator<TriplePath> triples = bgp.patternElts();
        while (triples.hasNext()) {
            addEdge(triples.next().asTriple());
        }
    }

    private void addEdge(Triple t) {
        Node s = t.getSubject(), o = t.getObject();

        if (s.isConcrete()) {
            this.concreteNodes.add(s);
        } else {
            this.varNodes.add(s);
        }

        if (o.isConcrete()) {
            this.concreteNodes.add(o);
        } else {
            this.varNodes.add(o);
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

