import org.junit.Test;

/**
 * Created by xgfd on 22/06/2017.
 */
public class PrestoTest {
    @Test
    public void main() throws Exception {
//        String[] args = {"-m", "test/yago_data/movie.ttl", "-q", "test/yago_queries/movie/movie1-001.rq"//, "test/movie1.rq", "test/movie1.rq", "test/movie1.rq", "test/movie1.rq", "test/movie1.rq"
        String[] args = {"-m", "test/yago_data/cs.ttl", "-f", "test/yago_queries/cs"//, "test/movie1.rq", "test/movie1.rq", "test/movie1.rq", "test/movie1.rq", "test/movie1.rq"
//                , "-v"
//                , "-V"
        };

        Presto.main(args);
    }
}