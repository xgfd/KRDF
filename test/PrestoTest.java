import org.junit.Test;

/**
 * Created by xgfd on 22/06/2017.
 */
public class PrestoTest {
    @Test
    public void main() throws Exception {
        String[] args = {"-m", "test/yago_data/yago.ttl", "-f", "test/yago_queries"
                , "-v"
//                , "-V"
        };

        Presto.main(args);
    }

}