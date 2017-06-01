import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

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
 * An ELT is recursively defined as a set of ELTs decending from an anonymous node linked by labeled edges.
 */
private class ELT<E> {
    // child tress connected by incoming edges
    HashMap<Node, ELT> incomingELTs = new HashMap<>(),
            outgoingELTs = new HashMap<>();

    // child tress connected by outgoing edges

    public ELT(QueryGraph qg, Node root) {
        dfs(qg, root);
    }

    /**
     * SUM(incoming.hashCode + 31 * inELT.hashCode) + SUM(outgoing.hashCode + 47 * outELT.hashCode)
     *
     * @return The hash code of the ELT
     */
    @Override
    public int hashCode() {
        int hash = this.incomingELTs.keySet().stream()
                .mapToInt(node -> {
                    return node.hashCode() + 31 * this.incomingELTs.get(node).hashCode();
                })
                .sum();

        hash += this.outgoingELTs.keySet().stream()
                .mapToInt(node -> {
                    return node.hashCode() + 47 * this.incomingELTs.get(node).hashCode();
                })
                .sum();

        return hash;
    }

//    static String indent = "";

    @Override
    public String toString() {

        String str = "( " + this.hashCode();

        String inString = this.incomingELTs.keySet().stream()
                .map(node -> {
                    return " <- " + node.hashCode() + " - " + this.incomingELTs.get(node).toString() + ";";
                })
                .reduce((a, b) -> a.concat(b));

        String outString = this.outgoingELTs.keySet().stream()
                .map(node -> {
                    return " - " + node.hashCode() + " -> " + this.incomingELTs.get(node).toString() + ";";
                })
                .reduce((a, b) -> a.concat(b));

        str += inString + outString;

        // remove the ; at the end
        if(str.lastIndexOf(";") == str.length() -1) {
            str = str.substring(0, str.length() - 1);
        }

        str += " )";

        return str;
    }

    private Set<ELT> dfs(QueryGraph qg, Node root) {
        Set<ELT> elts = new HashSet<>();

        Set<Triple> incomings = qg.getIncomming(root);
        Set<Triple> outgoings = qg.getoutgoing(root);

        incomings.stream().forEach(triple -> {
            Node next = triple.getSubject();
            this.incomingELTs.put(triple.getPredicate(), new ELT(qg, next));
        });

        outgoings.stream().forEach(triple -> {
            Node next = triple.getObject();
            this.outgoingELTs.put(triple.getPredicate(), new ELT(qg, next));
        });
    }
}