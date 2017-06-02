import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

import javax.swing.text.html.Option;
import java.util.*;

/**
 * Created by xgfd on 15/05/2017.
 */
class QueryGraph {

    private HashMap<Node, Set<Triple>> incomingEdges = new HashMap<>();
    private HashMap<Node, Set<Triple>> outgoingEdges = new HashMap<>();
    private Set<Node> concreteNodes = new HashSet<>();
    static Set<Triple> visitedEdges = new HashSet<>();

    QueryGraph(String bgpString) {
        Query query = QueryFactory.create(bgpString);

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

    Set<Triple> getIncomming(Node v) {
        return Optional.ofNullable(incomingEdges.get(v)).orElse(new HashSet<>());
    }

    Set<Triple> getOutgoing(Node v) {
        return Optional.ofNullable(outgoingEdges.get(v)).orElse(new HashSet<>());
    }

    Set<Node> getConcreteNodes() {
        return this.concreteNodes;
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
    ELT asELT(Node v) {
        visitedEdges.clear();
        return new ELT(this, v);
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

/**
 * Class and algorithms for detecting isomorphism of edge-labeled trees.
 * A query graph (assumed to be acyclic) is represented as a edge-labeled (i.e. predicates) tree rooted at a concrete vertex,
 * which is used as an index key to retrieve cached cardinality.
 * An ELT is recursively defined as a set of ELTs decending from an anonymous node linked by labeled edges.
 */
class ELT {
    // child tress connected by incoming edges
    private HashMap<Node, ELT> incomingELTs = new HashMap<>(), outgoingELTs = new HashMap<>();

    // child tress connected by outgoing edges

    ELT(QueryGraph qg, Node root) {
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
                .mapToInt(node -> node.hashCode() + 31 * this.incomingELTs.get(node).hashCode())
                .sum();

        hash += this.outgoingELTs.keySet().stream()
                .mapToInt(node -> node.hashCode() + 47 * this.outgoingELTs.get(node).hashCode())
                .sum();

        return hash;
    }


    @Override
    public String toString() {

        String str = "( " + this.hashCode() + ", ";

        String inString = this.incomingELTs.keySet().stream()
                .map(node -> this.incomingELTs.get(node).toString() + ", ")
                .reduce("", (a, b) -> a + b)
                .toString();

        String outString = this.outgoingELTs.keySet().stream()
                .map(node -> this.outgoingELTs.get(node).toString() + ", ")
                .reduce("", (a, b) -> a + b)
                .toString();

        str += inString + outString;

//        String inString = this.incomingELTs.keySet().stream()
//                .map(node -> " <-[" + node.hashCode() + "]- " + this.incomingELTs.get(node).toString() + ";")
//                .reduce("", (a, b) -> a + b)
//                .toString();
//
//        String outString = this.outgoingELTs.keySet().stream()
//                .map(node -> " -[" + node.hashCode() + "]-> " + this.outgoingELTs.get(node).toString() + ";")
//                .reduce("", (a, b) -> a + b)
//                .toString();
//
//        str += inString + outString;
//
        // remove the ; at the end
        if (str.lastIndexOf(",") == str.length() - 2) {
            str = str.substring(0, str.length() - 2);
        }

        str += " )";

        return str;
    }

    private void dfs(QueryGraph qg, Node root) {
        Set<Triple> incomings = qg.getIncomming(root);
        Set<Triple> outgoings = qg.getOutgoing(root);

        incomings.forEach(triple -> {
            if (!QueryGraph.visitedEdges.contains(triple)) {
                QueryGraph.visitedEdges.add(triple);
                Node next = triple.getSubject();
                this.incomingELTs.put(triple.getPredicate(), new ELT(qg, next));
            }
        });

        outgoings.forEach(triple -> {
            if (!QueryGraph.visitedEdges.contains(triple)) {
                QueryGraph.visitedEdges.add(triple);
                Node next = triple.getObject();
                this.outgoingELTs.put(triple.getPredicate(), new ELT(qg, next));
            }
        });
    }
}