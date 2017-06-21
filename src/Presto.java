import com.wolfram.jlink.ExprFormatException;
import com.wolfram.jlink.MathLinkException;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by xgfd on 21/06/2017.
 */
public class Presto {
    static final Map<String, List<String>> params = new HashMap<>();

    static public void main(String[] args) {
        // -q path/to/query/file
        // -f path/to/query/file/folder
        parseParams(args);

        List<String> filePaths = params.get("q"), folderPaths = params.get("f");

        if (filePaths == null && folderPaths == null) {
            System.out.println("Invalid parameters. Use -q for query files separated by spaces or -f for a folder of query files.");
        }

        if (filePaths != null) {
            int[][] estimates = (int[][]) filePaths.stream()
                    .map(path -> QueryFactory.read(path))
                    .map(query -> esti(query))
                    .toArray();

            System.out.println(Arrays.toString(estimates));
        }

        //TODO
//        if (folderPaths != null) {
//            Path folder = Paths.get(folderPaths.get(0));
//
//        }

    }

    /**
     * Calculate the 90% credible interval of the cardinality of a query
     *
     * @param q
     * @return Cardinality credible interval
     */
    @Nullable
    static private int[] esti(Query q) {

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

        try {
            // calculate 90% credible interval
            return M.ci90(total, cardinalities);
        } catch (MathLinkException e) {
            e.printStackTrace();
        } catch (ExprFormatException e) {
            e.printStackTrace();
        }

        return null;
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
