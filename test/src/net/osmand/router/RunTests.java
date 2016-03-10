package net.osmand.router;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.osmand.data.LatLon;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by User on 07.03.2016.
 */
public class RunTests {
    public static void main(String[] args) {

        LatLon start = new LatLon(45.6971206184178, 35.51630312204361);
        LatLon end = new LatLon(45.6952846638807, 35.51303619146347);
        HashMap expectedResults = new HashMap<Long, String>() {
            {
                //super.put(-93995L, "+5,10");
                super.put(-96063L, "+TL, +TL, +TL, +C, TR, TR");
                super.put(-96062L, "+TL, +TL, +TL, +C, TR, TR");
                super.put(-96061L, "+TL, +TL, +TL, +C, TR, TR");
            }

        };

        TestEntry[] testEntry = {new TestEntry("Amstelveenseweg", start, end, expectedResults), new TestEntry("Amstelveenseweg2", start, end, expectedResults)};
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("json.txt")){

            String s = gson.toJson(testEntry);
            System.out.println(s);
            gson.toJson(testEntry, writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Result result = JUnitCore.runClasses(RouteResultPreparationTest.class);
        for (Failure fail : result.getFailures()) {
            System.out.println("TEST FAILED!!! " + fail.toString());
        }
        if (result.wasSuccessful()) {
            System.out.println("All tests finished successfully...");
        }

    }
}
