import org.apache.jena.graph.Node;

import java.lang.reflect.Array;
import java.util.Map;

/**
 * Created by xgfd on 05/05/2017.
 */
public class NodeAdjList {
    static Map<Node, DoubleCol> adjList;
}


class DoubleCol {
    private Array right, left;
}
