import org.junit.Test;

/**
 * Created by xgfd on 22/06/2017.
 */
public class PrestoTest {
    @Test
    public void main() throws Exception {
        String[] args = {"-m", "test/yago_data/movie.ttl", "-q", "test/movie1.rq", "test/movie1.rq", "test/movie1.rq", "test/movie1.rq", "test/movie1.rq", "test/movie1.rq"
//                , "-v"
//                , "-V"
        };

        Presto.main(args);
    }
}