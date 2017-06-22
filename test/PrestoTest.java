import org.junit.Test;

/**
 * Created by xgfd on 22/06/2017.
 */
public class PrestoTest {
    @Test
    public void main() throws Exception {
        String[] args = {"-m", "./athlete.ttl", "-q", "card_test.rq"};

        Presto.main(args);
    }

}