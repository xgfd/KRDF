import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by xgfd on 31/05/2017.
 */
public class CardinalityTest {
    @Before
    public void setUp() throws Exception {
        System.out.println("Loading RDF ...");
        RDFGraph.readRDF("./athlete.ttl");
        System.out.println("RDF loaded.");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void cardinality() throws Exception {

        Query q = QueryFactory.read("./athlete.rq");

        int ref = 0;
        ResultSet rs = RDFGraph.execSelect(q);
        while (rs.hasNext()) {
            rs.next();
            ref++;
        }
//        System.out.println(ref);

        QueryGraph qg = new QueryGraph(q);

        Node v = qg.getConcreteNodes().iterator().next();

        ELT elt = qg.asELT(v);

        int card = Cardinality.cardinality(v, elt);

        System.out.printf("%-50s %-10s %-10s %-10s %-10s%n", "Node", "Card.", "Hit", "Miss", "Cache size");
        System.out.printf("%-50s %-10d %-10d %-10d %-10d%n", v, card, Cardinality.cacheHit, Cardinality.cacheMiss, Cardinality.cacheSize());

        assert ref == card;

//        qg.getConcreteNodes().stream()
//                .forEach(v -> {
//                    ELT elt = qg.asELT(v);
//
//                    int card = Cardinality.cardinality(v, elt);
//
//                    System.out.printf("%-50s %-10d %-10d %-10d%n", v, card, Cardinality.cacheHit, Cardinality.cacheMiss);
//
////                    Cardinality.resetCacheStats();
//
//                });

    }
}