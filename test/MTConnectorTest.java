import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by xgfd on 20/06/2017.
 */
public class MTConnectorTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void eval() throws Exception {
        // Typical launch on Windows:
//        String[] argv = {"-linkmode", "launch", "-linkname", "c:\\program files\\wolfram research\\mathematica\\10.0\\mathkernel"};

// Typical launch on Linux:
//        String[] argv = {"-linkmode", "launch", "-linkname", "math -mathlink"};

// Typical launch on Mac OS X:
        String[] argv = {"-linkmode", "launch", "-linkname", "\"/Applications/Mathematica.app/Contents/MacOS/MathKernel\" -mathlink"};

        MTConnector.eval(argv);
    }

}