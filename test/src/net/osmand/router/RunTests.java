package net.osmand.router;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Created by User on 07.03.2016.
 */
public class RunTests {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(RouteResultPreparationTest.class);
        for (Failure fail : result.getFailures()) {
            System.out.println("TEST FAILED!!! " + fail.toString());
        }
        if (result.wasSuccessful()) {
            System.out.println("All tests finished successfully...");
        }

    }
}
