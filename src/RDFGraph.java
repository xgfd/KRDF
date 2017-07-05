import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xgfd on 15/05/2017.
 */
public class RDFGraph {
    // create an empty model
    static Model model = ModelFactory.createDefaultModel();
    static String remoteSPARQL = null;
    static public boolean debug = false;

    /**
     * Initiate RDF store with local file or connect to a remote SPARQL endpoint.
     *
     * @param rdf Path to a RDF file or URL of a remote SPARQL endpoint starting with http.
     */
    static public void initRDF(String rdf) {
        // clear cache since new triples may invalidate the cache
        Cardinality.clearCache();

        if (rdf.startsWith("http")) {
            remoteSPARQL = rdf;
        } else {
            remoteSPARQL = null;
            model.read(rdf, null);
        }
    }

    static public List<QuerySolution> execTriple(Triple t) {
        if (debug) {
            System.out.println(t.toString(RDFGraph.getPrefixMapping()));
        }
        return execSelect(buildQuery(t));
    }

    static public List<QuerySolution> execSelect(Query q) {
        if (debug) {
//            System.out.println(q);
        }

        QueryExecution qex;
        if (isRemote()) {
            qex = QueryExecutionFactory.sparqlService(remoteSPARQL, q);
            qex.setTimeout(2000, 5000); // set http read timeout and connect timeout
        } else {
            qex = QueryExecutionFactory.create(q, model);
        }

        List<QuerySolution> results = materilise(qex.execSelect());
        qex.close(); // remote endpoints may limit the number of connections and cause further execution to hang
        return results;
    }

    static PrefixMapping withDefaultMappings(PrefixMapping pm) {
        return model.withDefaultMappings(pm);
    }

    static PrefixMapping withDefaultMappings(QueryGraph qg) {
        return withDefaultMappings(qg.getPrefixMapping());
    }

    static PrefixMapping getPrefixMapping() {
        return PrefixMapping.Factory.create().setNsPrefixes(model.getNsPrefixMap());
    }

    static public long size() {
        return model.size();
    }

    static public boolean isRemote() {
        return remoteSPARQL != null;
    }

    static private Query buildQuery(Triple t) {
        assert !t.isConcrete(); // should contain one (and only one) variable

        ElementTriplesBlock bgp = new ElementTriplesBlock(); // Make a BGP
        bgp.addTriple(t);
        Query q = QueryFactory.make();
        q.setQueryPattern(bgp);
        q.setQuerySelectType();
        q.setQueryResultStar(true);
        q.setResultVars();
//        System.out.println(q);
        return q;
    }

    static private List<QuerySolution> materilise(ResultSet rs) {
        List<QuerySolution> results = new ArrayList();
        rs.forEachRemaining(querySolution -> results.add(querySolution));
        return results;
    }
}
