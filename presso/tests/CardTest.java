import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

/**
 * Created by xgfd on 22/05/2017.
 */
public class CardTest {
    @Test
    public void calCard() {
        RDFGraph.readRDF("athlete.ttl");

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
}
