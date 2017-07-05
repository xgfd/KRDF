import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
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
        Query q = QueryFactory.read("yago_queries/q8.rq");
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

    @Test
    public void asELTAllVar() throws Exception {
        ELT elt = qg.asELT();
        System.out.println(elt.toString());
    }

}