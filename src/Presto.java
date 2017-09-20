import com.wolfram.jlink.ExprFormatException;
import com.wolfram.jlink.MathLinkException;
import org.apache.jena.ext.com.google.common.primitives.Doubles;
import org.apache.jena.ext.com.google.common.primitives.Ints;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by xgfd on 21/06/2017.
 */
public class Presto {
    static final Map<String, List<String>> params = new HashMap<>();
    static boolean verbose = false;

    static public void main(String[] args) {
        // -m path to an RDF file
        // -q path to query file
        // -f path to query file folder
        // -v verbose output
        // -V include intermediate query output
        parseParams(args);

        List<String> queryPaths = params.getOrDefault("q", new ArrayList<>()),
                queryFolderPaths = params.getOrDefault("f", new ArrayList<>()),
                model = params.getOrDefault("m", new ArrayList<>());

        verbose = params.get("v") != null;

        if (params.get("V") != null) {
            RDFGraph.debug = true;
        }

        if (queryPaths.size() == 0 && queryFolderPaths.size() == 0 && model.size() == 0) {
            System.out.printf("%s%n%s%n%s%n%s", "Presto -m RDF_file or a SPARQL endpoint [-q query_file query_file... | -f folder folder...]", "-m: path to an RDF file", "-q: paths to query files", "-f: paths to folders of query files (.rq)");
            return;
        }

        if (model.size() == 0) {
            System.out.println("Missing RDF. Use -m for an RDF file or a SPARQL endpoint URL.");
            return;
        }

        if (queryPaths.size() == 0 && queryFolderPaths.size() == 0) {
            System.out.println("Missing query file(s). Use -q for query files separated by spaces or -f for folders of query files.");
            return;
        }

        if (verbose) {
            System.out.println("Initialising RDF model...");
        }
        RDFGraph.initRDF(model.get(0));

        if (verbose) {
            System.out.println("Initialising Mathematica...");
        }
        M.init();

        System.out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n", "Query", "CI90", "Cardinality", "Cache_Hit", "Cache_Miss", "Cache_Size", "Card_per_Node", "Card_Cal (ms)", "Prob_Cal (ms)");

        queryPaths.stream()
                .map(pathStr -> Paths.get(pathStr))
                .map(path -> collectOutput(path)) // file path to query
                .forEach(System.out::println);

        for (String queryFolder : queryFolderPaths) {
            try {
                Files.walk(Paths.get(queryFolder)) // list files
                        .filter(p -> p.getFileName().toString().endsWith(".rq")) // get all .rq files
                        .map(path -> collectOutput(path)) // estimate queries and collect stats
                        .forEach(System.out::println);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static private List<String> collectOutput(Path path) {
        List<String> output = new ArrayList<>();
        Query q = QueryFactory.read(path.toString());

        List internal = esti(q);
        int true_card = RDFGraph.execSelect(q).size();
        output.add(path.getFileName().toString()); // query file name
        output.add(internal.get(0).toString()); // most likely card.
        output.add("" + true_card); // true cardinality
        output.add("" + Cardinality.cacheHit); // cache hit
        output.add("" + Cardinality.cacheMiss); // cache miss
        output.add("" + Cardinality.cacheSize()); // cache size
        output.add(internal.get(1).toString()); // card. per node
        output.add(internal.get(2).toString()); // card. calculation time
        output.add(internal.get(3).toString()); // probability calculation time
        return output;
    }

    /**
     * Calculate the 90% credible interval of the cardinality of a query
     *
     * @param q A query
     * @return A list of internal stats, i.e., [double most_likely_card, Map<Node, Integer> nodeCard, long card_time, long prob_time]
     */
    static private List esti(Query q) {

        if (verbose) {
            System.out.println("Processing query:");
            System.out.println(q);
        }

        List interStats = new ArrayList();
        Map<Node, Integer> nodeCard = new HashMap<>();
        long card_time, prob_time = 0;
        QueryGraph qg = new QueryGraph(q);
        Set<Node> concreteNodes = qg.getConcreteNodes();

        // calculate cardinality of every concrete node
        card_time = System.currentTimeMillis();
        int[] cardinalities = concreteNodes.stream()
                .mapToInt(v -> {
                    ELT elt = qg.asELT(v);
                    int card = Cardinality.cardinality(v, elt);
                    nodeCard.put(v, card);
                    return card;
                })
                .toArray();

        // calculate the total cardinality
        int total = Cardinality.cardinality(qg);

        card_time = System.currentTimeMillis() - card_time;

//        double[] interval = null;
        double card = -1;

        switch (concreteNodes.size()) {
            case 0:
                card = total;
                break;
            case 1:
                card = cardinalities[0];
                break;
            default: // more than 1 bound obj/sub
                try {
                    // calculate 90% credible interval
//                    interval = M.cr90(total, cardinalities);

                    prob_time = System.currentTimeMillis();
                    card = M.maxProbArg(total, cardinalities);
                    prob_time = System.currentTimeMillis() - prob_time;

//                    interval = new double[]{card};
                } catch (MathLinkException e) {
                    System.out.println(e.getMessage());
                } catch (ExprFormatException e) {
                    System.out.println(e.getMessage());
                }
                break;
        }

        interStats.add(card);
        interStats.add(nodeCard);
        interStats.add(card_time);
        interStats.add(prob_time);

        return interStats;
    }

    static private void parseParams(String[] args) {
        List<String> options = null;
        for (int i = 0; i < args.length; i++) {
            final String a = args[i];

            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    System.err.println("Error at argument " + a);
                    return;
                }

                options = new ArrayList<>();
                params.put(a.substring(1), options);
            } else if (options != null) {
                options.add(a);
            } else {
                System.err.println("Illegal parameter usage");
                return;
            }
        }
    }
}
