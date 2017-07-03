/**
 * Created by xgfd on 13/06/2017.
 */

import org.apache.commons.lang3.builder.MultilineRecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Triple;

import java.util.HashMap;
import java.util.Set;

/**
 * Class and algorithms for detecting isomorphism of edge-labeled trees.
 * A query graph (assumed to be acyclic) is represented as a edge-labeled (i.e. predicates) tree rooted at a concrete vertex,
 * which is used as an index key to retrieve cached cardinality.
 * An ELT is recursively defined as a set of ELTs descending from an anonymous node linked by labeled edges.
 */
class ELT {

    // root node
    private Node root = null;
    // child tress connected by directed edges
    private HashMap<DiPredicate, ELT> children = new HashMap<>();

    // child tress connected by outgoing edges

    public ELT(QueryGraph qg, Node root) {
        this.root = root;
        dfs(qg, root);
    }


    public Set<DiPredicate> getDescendantEdges() {
        return children.keySet();
    }

    public ELT getChild(DiPredicate dp) {
        return children.get(dp);
    }

    public boolean isEmpty() {
        return this.children.size() == 0;
    }

    /**
     * SUM(incoming.hashCode + 31 * inELT.hashCode) + SUM(outgoing.hashCode + 47 * outELT.hashCode)
     *
     * @return The hash code of the ELT
     */
    @Override
    public int hashCode() {
        int inMult = 31, outMult = 47;

        int hash = this.children.keySet().stream()
                .mapToInt(edge -> edge.hashCode() + (edge.isIncoming() ? inMult : outMult) * this.children.get(edge).hashCode())
                .sum();

        return hash;
    }


    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).toString();
    }

    private void dfs(QueryGraph qg, Node root) {
        Set<Triple> incomings = qg.getIncomming(root);
        Set<Triple> outgoings = qg.getOutgoing(root);

        incomings.forEach(triple -> {
            if (!QueryGraph.visitedEdges.contains(triple)) {
                QueryGraph.visitedEdges.add(triple);
                Node next = triple.getSubject();
                this.children.put(new DiPredicate(triple.getPredicate(), true), new ELT(qg, next));
            }
        });

        outgoings.forEach(triple -> {
            if (!QueryGraph.visitedEdges.contains(triple)) {
                QueryGraph.visitedEdges.add(triple);
                Node next = triple.getObject();
                this.children.put(new DiPredicate(triple.getPredicate(), false), new ELT(qg, next));
            }
        });
    }
}

/**
 * Directed predicate
 */

class DiPredicate extends Node_URI {
    private boolean incoming;

    protected DiPredicate(String uri, boolean incoming) {
        super(uri);
        this.incoming = incoming;
    }

    protected DiPredicate(Node n, boolean incoming) {
        super(n.getURI());
        assert n instanceof Node_URI;
        this.incoming = incoming;
    }

    boolean isIncoming() {
        return incoming;
    }

    boolean isOutgoing() {
        return !incoming;
    }

    @Override
    public String toString() {
        return (incoming ? "<-" : "->") + super.toString(RDFGraph.getPrefixMapping());
    }
}
