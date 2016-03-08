package net.osmand.router;

import net.osmand.binary.BinaryMapIndexReader;
import net.osmand.data.LatLon;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by yurkiss on 04.03.16.
 */

@RunWith(Parameterized.class)
public class RouteResultPreparationTest {

    private static RoutePlannerFrontEnd fe;
    private static RoutingContext ctx;

    private String testName;
    private LatLon startPoint;
    private LatLon endPoint;
    private Map<Long, String> expectedResults;

    public RouteResultPreparationTest(String testName, LatLon startPoint, LatLon endPoint, Map<Long, String> expectedResults) {
        this.testName = testName;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.expectedResults = expectedResults;
    }

    @BeforeClass
    public static void setUp() throws Exception {
        String fileName = "../Turn_lanes_test.obf";
        String fileName2 = "../World_basemap_2.obf";

        File fl = new File(fileName);
        File fl2 = new File(fileName2);

        RandomAccessFile raf = null;
        RandomAccessFile raf2 = null;
        try {
            raf = new RandomAccessFile(fl, "r");
            raf2 = new RandomAccessFile(fl2, "r");
            fe = new RoutePlannerFrontEnd(false);
            RoutingConfiguration.Builder builder = RoutingConfiguration.getDefault();
            LinkedHashMap<String, String> params = new LinkedHashMap<>();
            params.put("car", "true");
            params.put("short_way", "true");
            RoutingConfiguration config = builder.build("car", RoutingConfiguration.DEFAULT_MEMORY_LIMIT * 3, params);
            BinaryMapIndexReader[] binaryMapIndexReaders = {new BinaryMapIndexReader(raf2, fl2), new BinaryMapIndexReader(raf, fl)};
            ctx = fe.buildRoutingContext(config, null, binaryMapIndexReaders,
                    RoutePlannerFrontEnd.RouteCalculationMode.NORMAL);
            ctx.leftSideNavigation = false;
            RouteResultPreparation.PRINT_TO_CONSOLE_ROUTE_INFORMATION_TO_TEST = true;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {

        Object start = new LatLon(45.6971206184178, 35.51630312204361);
        Object end = new LatLon(45.6952846638807, 35.51303619146347);
        Object expectedResults = new HashMap<Long, String>() {
            {
                //super.put(-93995L, "+5,10");
                super.put(-96063L, "+TL, +TL, +TL, C, TR, TR");
            }

        };

        return Arrays.asList(new Object[][]{
                {"Amstelveenseweg", start, end, expectedResults},
                {"Amstelveenseweg2", start, end, expectedResults},
                {"Amstelveenseweg3", start, end, expectedResults}
        });
    }

    @Test
    public void testLanes() throws Exception {

        List<RouteSegmentResult> routeSegments = fe.searchRoute(ctx, startPoint, endPoint, null);

        int prevSegment = -1;
        for (int i = 0; i <= routeSegments.size(); i++) {
            if (i == routeSegments.size() || routeSegments.get(i).getTurnType() != null) {
                if (prevSegment >= 0) {
                    String lanes = getLanesString(routeSegments.get(prevSegment));
                    String name = routeSegments.get(prevSegment).getDescription();

                    long segmentId = routeSegments.get(prevSegment).getObject().getId();
                    String expectedResult = expectedResults.get(segmentId);
                    if (expectedResult != null) {
                        Assert.assertEquals(expectedResult, lanes);
                    } else {
                        //NOT FOUND FOR THAT SEGMENT
                    }

                    System.out.println("segmentId: " + segmentId + " description: " + name);

                }
                prevSegment = i;
            }
        }


        /*for (RouteSegmentResult s : routeSegments) {


            String name = s.getDescription();
//            if(name != null){
//                String[] split = name.split("\\[ ");
//                if(split.length > 1){
//                    split = split[1].split("\\]");
//                    lanes = split[0];
//                }
//            }

            long segmentId = s.getObject().getId();
            String expectedResult = expectedResults.get(segmentId);
            if(expectedResult != null) {
                Assert.assertEquals(expectedResult, lanes);
            }else{
                //NOT FOUND FOR THAT SEGMENT
            }


        }*/

    }


    String getLanesString(RouteSegmentResult segment) {
        String turn = segment.getTurnType().toString();
        final int[] lns = segment.getTurnType().getLanes();
        if (lns != null) {
            String s = "";
            for (int h = 0; h < lns.length; h++) {
                if (h > 0) {
                    s += ", ";
                }
                if (lns[h] % 2 == 1) {
                    s += "+";
                }
                int pt = TurnType.getPrimaryTurn(lns[h]);
                if (pt == 0) {
                    pt = 1;
                }
                s += TurnType.valueOf(pt, false).toXmlString();
                int st = TurnType.getSecondaryTurn(lns[h]);
                if (st != 0) {
                    s += ";" + TurnType.valueOf(st, false).toXmlString();
                }

            }
            s += "";
            turn += s;
            return s;
        }
        return null;
    }

    protected void addTurnInfoDescriptions(List<RouteSegmentResult> result) {
        int prevSegment = -1;
        float dist = 0;
        for (int i = 0; i <= result.size(); i++) {
            if (i == result.size() || result.get(i).getTurnType() != null) {
                if (prevSegment >= 0) {
                    String turn = result.get(prevSegment).getTurnType().toString();
                    final int[] lns = result.get(prevSegment).getTurnType().getLanes();
                    if (lns != null) {
                        String s = "[ ";
                        for (int h = 0; h < lns.length; h++) {
                            if (h > 0) {
                                s += ", ";
                            }
                            if (lns[h] % 2 == 1) {
                                s += "+";
                            }
                            int pt = TurnType.getPrimaryTurn(lns[h]);
                            if (pt == 0) {
                                pt = 1;
                            }
                            s += TurnType.valueOf(pt, false);
                            int st = TurnType.getSecondaryTurn(lns[h]);
                            if (st != 0) {
                                s += ";" + TurnType.valueOf(st, false);
                            }

                        }
                        s += "]";
                        turn += s;
                    }
                    result.get(prevSegment).setDescription(
                            turn + MessageFormat.format(" and go {0,number,#.##} meters", dist));
                    if (result.get(prevSegment).getTurnType().isSkipToSpeak()) {
                        result.get(prevSegment).setDescription("-*" + result.get(prevSegment).getDescription());
                    }
                }
                prevSegment = i;
                dist = 0;
            }
            if (i < result.size()) {
                dist += result.get(i).getDistance();
            }
        }
    }


}
