import org.apache.jena.base.Sys;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by xgfd on 31/05/2017.
 */
public class CardinalityTest {
    @Before
    public void setUp() throws Exception {
        System.out.println("Loading RDF ...");
        RDFGraph.initRDF("./yago_data/yago.ttl");
        System.out.println(RDFGraph.size() + " statements loaded.");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void cardinality() throws Exception {

        Query q = QueryFactory.read("card_test.rq");

        int ref_card = 0, ref_total = 0;
        List<QuerySolution> rs = RDFGraph.execSelect(q);
        ref_card += rs.size();

        rs = RDFGraph.execSelect(QueryFactory.read("ref_card_test.rq"));
        ref_total += rs.size();

        QueryGraph qg = new QueryGraph(q);

        Node v = qg.getConcreteNodes().iterator().next();

        ELT elt = qg.asELT(v);

        Cardinality.adjVTime = 0;

        long ts = System.currentTimeMillis();
        int card = Cardinality.cardinality(v, elt), total = Cardinality.cardinality(elt);
        ts = System.currentTimeMillis() - ts;

        System.out.println("Total time: " + ts + "; adjV: " + Cardinality.adjVTime);

        System.out.printf("%-40s %-15s %-15s %-15s %-15s%n", "Node", "Node_Card.", "Total_Card.", "Cache_Hit", "Cache_Miss");
        System.out.printf("%-40s %-15d %-15d %-15d %-15d%n", v.toString(RDFGraph.getPrefixMapping()), card, total, Cardinality.cacheHit, Cardinality.cacheMiss);

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