import org.junit.Test;

/**
 * Created by xgfd on 22/06/2017.
 */
public class PrestoTest {
    @Test
    public void main() throws Exception {
        String[] args = {"-m", "http://spr-001.ecs.soton.ac.uk:8890/sparql", "-q", "queries/q8.rq", "-v"};

        Presto.main(args);
    }

}