/**
 * Created by xgfd on 20/06/2017.
 */

import com.wolfram.jlink.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MTConnector {

    public static void eval(String[] argv) {

        KernelLink ml = null;

        try {
            ml = MathLinkFactory.createKernelLink(argv);
        } catch (MathLinkException e) {
            System.out.println("Fatal error opening link: " + e.getMessage());
            return;
        }

        try {
            ml.connect(5000); // Wait at most 5 seconds
        } catch (MathLinkException e) {
            // If the timeout expires, a MathLinkException will be thrown.
            System.out.println("Failure to connect link: " + e.getMessage());
            ml.close();
            return; // Or whatever is appropriate.
        }

        try {
            // Get rid of the initial InputNamePacket the kernel will send
            // when it is launched.
            ml.discardAnswer();

            Path packagePath = Paths.get("../mathematica/distDef.m").toAbsolutePath();

            System.out.println(packagePath);

            ml.evaluate("<<" + packagePath.toString());
            ml.discardAnswer();

            ml.evaluate("2+2");
            ml.waitForAnswer();

            int result = ml.getInteger();
            System.out.println("2 + 2 = " + result);

            // Here's how to send the same input, but not as a string:
            ml.putFunction("EvaluatePacket", 1);
            ml.putFunction("Plus", 2);
            ml.put(3);
            ml.put(3);
            ml.endPacket();
            ml.waitForAnswer();
            result = ml.getInteger();
            System.out.println("3 + 3 = " + result);

            // If you want the result back as a string, use evaluateToInputForm
            // or evaluateToOutputForm. The second arg for either is the
            // requested page width for formatting the string. Pass 0 for
            // PageWidth->Infinity. These methods get the result in one
            // step--no need to call waitForAnswer.
            String strResult = ml.evaluateToOutputForm("4+4", 0);
            System.out.println("4 + 4 = " + strResult);

        } catch (MathLinkException e) {
            System.out.println("MathLinkException occurred: " + e.getMessage());
        } finally {
            ml.close();
        }
    }
}
