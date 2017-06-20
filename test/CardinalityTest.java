import org.apache.jena.graph.Node;
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
        System.out.println(RDFGraph.size() + " statements loaded.");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void cardinality() throws Exception {

        Query q = QueryFactory.read("./athlete.rq");

        int ref_card = 0, ref_total = 0;
        ResultSet rs = RDFGraph.execSelect(q);
        while (rs.hasNext()) {
            rs.next();
            ref_card++;
        }

        rs = RDFGraph.execSelect(QueryFactory.read("./athlete_all_var.rq"));
        while (rs.hasNext()) {
            rs.next();
            ref_total++;
        }

        QueryGraph qg = new QueryGraph(q);

        Node v = qg.getConcreteNodes().iterator().next();

        ELT elt = qg.asELT(v);

        int card = Cardinality.cardinality(v, elt), total = Cardinality.cardinality(elt);

        System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s%n", "Node", "Node_Card.", "Total_Card.", "Cache_Hit", "Cache_Miss", "Cache_Size");
        System.out.printf("%-40s %-15d %-15d %-15d %-15d %-15d%n", v.toString(RDFGraph.getPrefixMapping()), card, total, Cardinality.cacheHit, Cardinality.cacheMiss, Cardinality.cacheSize());

        assert ref_card == card;
        assert ref_total == total;

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