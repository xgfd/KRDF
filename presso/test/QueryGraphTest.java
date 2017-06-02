import org.apache.jena.graph.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;


/**
 * Created by xgfd on 31/05/2017.
 */
public class QueryGraphTest {
    QueryGraph qg;

    @Before
    public void setUp() throws Exception {

        String q = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX dbp:<http://dbpedia.org/ontology/>\n" +
                "SELECT *\n" +
                "WHERE {\n" +
                "        ?subOfAthlete rdfs:subClassOf dbp:Athlete .\n" +
                "        ?subOfAthlete rdfs:label ?athleteGroup .\n" +
                "        ?athlete a ?subOfAthlete .\n" +
                "        ?athlete dbp:birthDate ?birth .\n" +
                "        ?athlete dbp:deathDate ?death .\n" +
                "} ";

        qg = new QueryGraph(q);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void asELT() throws Exception {
        Set<Node> concreteNodes = qg.getConcreteNodes();

        concreteNodes.stream().forEach(node -> {
            System.out.println("************* " + node.toString() + " ***************");
            ELT elt = qg.asELT(node);
            System.out.println(elt.toString());
        });
    }

}