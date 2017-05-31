import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
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

        String q = "prefix dbp:<http://dbpedia.org/ontology/>\n" +
                "select *\n" +
                "where {\n" +
                "        ?subOfAthlete rdfs:subClassOf dbp:Athlete .\n" +
                "        ?subOfAthlete rdfs:label ?athleteGroup .\n" +
                "        ?athlete a ?subOfAthlete .\n" +
                "        ?athlete dbp:birthDate ?birth .\n" +
                "        ?athlete dbp:deathDate ?death .\n" +
                "} ";

        QueryGraph qg = new QueryGraph(q);

        Node v = NodeFactory.createURI("http://dbpedia.org/ontology/Athlete");

        Collection<List<Triple>> chains = qg.asChains(v);

        int card = Cardinality.cardinality(v, chains);
    }

    @Test
    public void cardinality1() throws Exception {
    }

}