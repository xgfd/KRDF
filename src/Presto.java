import com.wolfram.jlink.ExprFormatException;
import com.wolfram.jlink.MathLinkException;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.jetbrains.annotations.Nullable;

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
        parseParams(args);

        List<String> filePaths = params.getOrDefault("q", new ArrayList<>()),
                folderPaths = params.getOrDefault("f", new ArrayList<>()),
                model = params.getOrDefault("m", new ArrayList<>());

        verbose = params.get("v") != null;

        if (verbose) {
            RDFGraph.debug = true;
        }

        if (filePaths.size() == 0 && folderPaths.size() == 0 && model.size() == 0) {
            System.out.printf("%s%n%s%n%s%n%s", "Presto -m RDF_file or a SPARQL endpoint [-q query_file query_file... | -f folder folder...]", "-m: path to an RDF file", "-q: paths to query files", "-f: paths to folders of query files (.rq)");
            return;
        }

        if (model.size() == 0) {
            System.out.println("Missing RDF. Use -m for an RDF file or a SPARQL endpoint URL.");
            return;
        }

        if (filePaths.size() == 0 && folderPaths.size() == 0) {
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


        System.out.printf("%-40s %-15s %-15s %-15s %-15s%n", "Query", "CI90", "Cache_Hit", "Cache_Miss", "Cache_Size");

        filePaths.stream()
                .map(pathStr -> Paths.get(pathStr))
                .map(path -> collectOutput(path)) // file path to query
                .forEach(System.out::println);

        for (String folderPath : folderPaths) {
            try {
                Files.walk(Paths.get(folderPath))
                        .filter(Files::isRegularFile)
                        .map(path -> collectOutput(path))
                        .forEach(System.out::println);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static private List<String> collectOutput(Path path) {
        List<String> output = new ArrayList<>();
        Query q = QueryFactory.read(path.toString());
        int[] interval = esti(q);
        output.add(path.getFileName().toString()); // query file name
        output.add(Arrays.toString(interval)); // query ci90
        output.add("" + Cardinality.cacheHit); // cache hit
        output.add("" + Cardinality.cacheMiss); // cache miss
        output.add("" + Cardinality.cacheSize()); // cache size
        return output;
    }

    /**
     * Calculate the 90% credible interval of the cardinality of a query
     *
     * @param q
     * @return Cardinality credible interval
     */
    @Nullable
    static private int[] esti(Query q) {

        if (verbose) {
            System.out.println("Processing query:");
            System.out.println(q);
        }

        QueryGraph qg = new QueryGraph(q);
        Set<Node> concreteNodes = qg.getConcreteNodes();

        // calculate cardinality of every concrete node
        int[] cardinalities = concreteNodes.stream()
                .mapToInt(v -> {
                    ELT elt = qg.asELT(v);
                    int card = Cardinality.cardinality(v, elt);
                    return card;
                })
                .toArray();

        // calculate the total cardinality
        int total = Cardinality.cardinality(qg);

        int[] interval = null;
        try {
            // calculate 90% credible interval
            interval = M.ci90(total, cardinalities);
        } catch (MathLinkException e) {
            System.out.println(e.getMessage());
        } catch (ExprFormatException e) {
            System.out.println(e.getMessage());
        }
        return interval;
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
