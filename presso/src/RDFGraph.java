import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.util.FileManager;

import java.io.InputStream;
import java.util.Collection;

/**
 * Created by xgfd on 15/05/2017.
 */
public class RDFGraph {
    // create an empty model
    static Model model = ModelFactory.createDefaultModel();

    public RDFGraph(String inputFileName) {

    }

    static public Model readFromFile(String inputFileName) {
        // use the FileManager to find the input file
        InputStream in = FileManager.get().open(inputFileName);
        if (in == null)

        {
            throw new IllegalArgumentException("File: " + inputFileName + " not found");
        }

// read the RDF/XML file
        return model.read(in, null);
    }

    static public Collection<Node> getNeighbours(Node v, Property pre) {
        return null;

    }


}
