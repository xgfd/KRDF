/**
 * Created by xgfd on 20/06/2017.
 */

import com.wolfram.jlink.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Mathematica linking
 */
public class M {

    static private KernelLink ml;

    public static void init() {

        String linkname;


        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            // Typical launch on Mac OS X:
            linkname = "\"/Applications/Mathematica.app/Contents/MacOS/MathKernel\" -mathlink";
        } else if (os.contains("windows")) {
            // Typical launch on Windows:
            linkname = "c:\\program files\\wolfram research\\mathematica\\10.0\\mathkernel";
        } else if (os.contains("linux")) {
            // Typical launch on Linux:
            linkname = "math -mathlink";
        } else {
            throw new RuntimeException("OS not recognised");
        }

        init(linkname);
    }

    public static void init(String linkname) {
        String[] argv = {"-linkmode", "launch", "-linkname", linkname};

        try {
            ml = MathLinkFactory.createKernelLink(argv);
        } catch (MathLinkException e) {
            System.out.println("Fatal error opening link: " + e.getMessage());
            return;
        }

        try {
            ml.activate();
        } catch (MathLinkException e) {
            System.out.println("Failure to connect link: " + e.getMessage());
            ml.close();
            return; // Or whatever is appropriate.
        }

        try {
            // Get rid of the initial InputNamePacket the kernel will send
            // when it is launched.
            ml.discardAnswer();

            Path packagePath = Paths.get("mathematica/distDef.m").toAbsolutePath();

//            System.out.println(packagePath);

            ml.evaluate("<<" + packagePath);
            ml.discardAnswer();
        } catch (MathLinkException e) {
            System.out.println("MathLinkException occurred: " + e.getMessage());
            ml.close();
        }
    }

    static public void close() {
        if (ml != null) {
            ml.close();
        }
    }

    /**
     * Given a 2-column binary matrix with evenly distributed 1s in each column, as below
     * 1 0
     * 1 1
     * 0 1
     * 0 1
     * 0 0
     * this function is the mass density function of that a certain number of rows have all 1s.
     *
     * @param t Total number of rows
     * @param a The number of evenly distributed 1s in the 1st column
     * @param b The number of evenly distributed 1s in the 2nd column
     * @param r The number of rows having all 1s
     * @return A probability of {@code r} rows having all 1s
     */
    static public Expr mdf2(int t, int a, int b, int r) throws MathLinkException {
        ml.newPacket();
        ml.putFunction("EvaluatePacket", 1);
        ml.putFunction("mdf2", 4);
        ml.put(t);
        ml.put(a);
        ml.put(b);
        ml.put(r);
        ml.endPacket();
        ml.waitForAnswer();
        Expr result = ml.getExpr();
        assert result.rationalQ();
        return result;
    }

    /**
     * A generalisation of mdf2 to n-column matrix.
     *
     * @param t       Total number of rows
     * @param columns An array of the sum of each column
     * @param r       The number of rows having all 1s
     * @return A probability of {@code r} rows having all 1s
     * @throws MathLinkException
     */

    static public Expr mdf(int t, int[] columns, int r) throws MathLinkException {
        ml.newPacket();
        ml.putFunction("EvaluatePacket", 1);
        ml.putFunction("mdf", 3);
        ml.put(t);
        ml.put(columns);
        ml.put(r);
        ml.endPacket();
        ml.waitForAnswer();
        Expr result = ml.getExpr();
        assert result.rationalQ();
        return result;
    }

    /**
     * Calculate the 90% credible interval of the mdf defined above.
     *
     * @param t
     * @param columns
     * @return A 90% credible interval
     * @throws MathLinkException
     * @throws ExprFormatException
     * @see #mdf(int, int[], int)
     */
    static public int[] cr90(int t, int[] columns) throws MathLinkException, ExprFormatException {
        ml.newPacket();
        ml.putFunction("EvaluatePacket", 1);
        ml.putFunction("cr90", 2);
        ml.put(t);
        ml.put(columns);
        ml.endPacket();
        ml.waitForAnswer();
        Expr result = ml.getExpr();
        assert result.listQ();
        return (int[]) result.asArray(Expr.INTEGER, 1);
    }


    /**
     * Find the argument that maximise the probability w.r.t the mdf defined above.
     *
     * @param t
     * @param columns
     * @return
     * @see #mdf(int, int[], int)
     */
    static public double maxProbArg(int t, int[] columns) throws MathLinkException, ExprFormatException {
        ml.newPacket();
        ml.putFunction("EvaluatePacket", 1);
        ml.putFunction("maxProbArg", 2);
        ml.put(t);
        ml.put(columns);
        ml.endPacket();
        ml.waitForAnswer();
        Expr result = ml.getExpr();
//        assert result.();
        return result.asDouble();
    }
}
