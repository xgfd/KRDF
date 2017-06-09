import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
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

        QueryGraph qg = new QueryGraph(q);

        qg.getConcreteNodes().stream()
                .forEach(v -> {
                    ELT elt = qg.asELT(v);

                    int card = Cardinality.cardinality(v, elt);

                    System.out.println("*******************" + v + "********************");
                    System.out.println(card);
                    System.out.println("hit: " + Cardinality.cacheHit + "; miss: " + Cardinality.cacheMiss);

//                    Cardinality.resetCacheStats();

                });
        System.out.println("Cache size: " + Cardinality.cacheSize());
    }
}