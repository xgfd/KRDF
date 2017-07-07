import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by xgfd on 20/06/2017.
 */
public class MTest {

    @Before
    public void setUp() throws Exception {
        M.init();
    }

    @After
    public void tearDown() throws Exception {
        M.close();
    }

    @Test
    public void mdf() throws Exception {
        int t = 10, a = 2, b = 3, r = 1;
        int[] columns = {a, b};
        System.out.println(M.mdf(t, columns, r));

        assert M.mdf2(t, a, b, r).equals(M.mdf(t, columns, r));
    }

    @Test
    public void ci90() throws Exception {
        int t = 10, a = 2, b = 3;
        int[] columns = {a, b};
        System.out.println(Arrays.toString(M.cr90(t, columns)));
    }
}