import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.util.FileManager;

import java.io.InputStream;

/**
 * Created by xgfd on 15/05/2017.
 */
public class RDFGraph {
    // create an empty model
    static Model model = ModelFactory.createDefaultModel();

    static public Model readRDF(String inputFileName) {
        // clear cache since new triples may invalidate the cache
        Cardinality.clearCache();

        return model.read(inputFileName, null);
    }

    static public ResultSet getNeighbours(Triple t) {
        assert !t.isConcrete(); // should contain one (and only one) variable

        ElementTriplesBlock bgp = new ElementTriplesBlock(); // Make a BGP
        bgp.addTriple(t);
        Query q = QueryFactory.make();
        q.setQueryPattern(bgp);
        q.setQuerySelectType();
        q.setResultVars();

        return QueryExecutionFactory.create(q, model).execSelect();
    }

    static public long size() {
        return model.size();
    }
}
