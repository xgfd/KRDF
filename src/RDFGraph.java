import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

import static org.apache.jena.vocabulary.OWLResults.system;


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

    static public ResultSet execTriple(Triple t) {
        if (debug) {
            System.out.println(t);
        }
        return execSelect(buildQuery(t));
    }

    static public ResultSet execSelect(Query q) {
        if (isRemote()) {
            return QueryExecutionFactory.sparqlService(remoteSPARQL, q).execSelect();
        } else {
            return QueryExecutionFactory.create(q, model).execSelect();
        }
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
}
