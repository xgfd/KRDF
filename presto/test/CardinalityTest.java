import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by xgfd on 31/05/2017.
 */
public class CardinalityTest {
    @Before
    public void setUp() throws Exception {
        RDFGraph.readRDF("athlete.ttl");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void cardinality() throws Exception {

        Query q = QueryFactory.read("./athlete.rq");

        QueryGraph qg = new QueryGraph(q);

        Node v = NodeFactory.createURI("http://dbpedia.org/ontology/Athlete");

        ELT chains = qg.asELT(v);

        int card = Cardinality.cardinality(v, null);
    }

    @Test
    public void cardinality1() throws Exception {
    }

}