package model;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;

public class Feature {
    
    private static LocalTime startTime = LocalTime.of(9, 30); // 9:30 AM start time
    private static double lowAAAA = Double.MAX_VALUE;
    private static double highAAAA = Double.MIN_VALUE;
    private static int timeOfLowAAAA = 1;
    private static int timeOfHighAAAA = 1;
    private static double lowFeature9 = Double.MAX_VALUE;
    private static double highFeature9 = Double.MIN_VALUE;
    private static int timeOfLowFeature9 = 1;
    private static int timeOfHighFeature9 = 1;
    private static double lowEquityLast = Double.MAX_VALUE;
    private static double highEquityLast = Double.MIN_VALUE;
    private static int timeOfLowEquityLast = 1;
    private static int timeOfHighEquityLast = 1;
    private static double prevTTTT = 0;
    private static double prevCCCC = 0;
    private static int feature23LastFlag = 0; // -1 for Feature 21, 1 for Feature 22
    private static int feature30LastFlag = 0; // 1 for Feature 28, -1 for Feature 29
    private static int timeOfLastFeature31 = -1;
    private static double feature39Low = Double.MAX_VALUE;
    private static double feature39High = Double.MIN_VALUE;
    private static int feature30Time = -1;


    public static ArrayList<Double> parse(Map<String, Object> jsonData){

        //Map<String, Double> features = new HashMap<>();
        ArrayList<Double> features = new ArrayList<Double>();

        features.add(0.0); //1
        features.add(0.0); //2
        features.add(0.0); //3

        // Extract necessary fields from the JSON data
        String timeString = (String) jsonData.get("time");
        LocalTime time = LocalTime.parse(timeString);
        int feature1Time = (int) ChronoUnit.MINUTES.between(startTime, time) + 1;
        double AAAA = (double) jsonData.get("AAAA");
        double BBBB = (double) jsonData.get("BBBB");
        double CCCC = (double) jsonData.get("CCCC");
        double TTTT = (double) jsonData.get("TTTT");
        double equityLast = (double) jsonData.get("equityLast");

        // Feature 1: Minutes into the trading day
        int feature1 = feature1Time;
        // features.put("feature1", (double) feature1);
        features.add((double)feature1); //4
        features.add(0.0); //5

        // Feature 2: AAAA
        double feature2 = AAAA;
        // features.put("feature2", feature2);
        features.add(feature2); //6

        // Feature 3: Low of the day for AAAA
        if (AAAA < lowAAAA) {
            lowAAAA = AAAA;
            timeOfLowAAAA = feature1;
        }
        // features.put("feature3", lowAAAA);
        features.add((double)lowAAAA); //7


        // Feature 4: Time of the low for AAAA
        int feature4 = timeOfLowAAAA;
        //features.put("feature4", (double) feature4);
        features.add((double) feature4); //8

        // Feature 5: Minutes since low of AAAA
        int feature5 = feature1 - timeOfLowAAAA;
        features.add((double) feature5); //9

        // Feature 6: High of the day for AAAA
        if (AAAA > highAAAA) {
            highAAAA = AAAA;
            timeOfHighAAAA = feature1;
        }
        // features.put("feature6", highAAAA);
        features.add(highAAAA); //10

        // Feature 7: Time of the high for AAAA
        int feature7 = timeOfHighAAAA;
        // features.put("feature7", (double) feature7);
        features.add((double) feature7); //11

        // Feature 8: Minutes since high of AAAA
        int feature8 = feature1 - timeOfHighAAAA;
        // features.put("feature8", (double) feature8);
        features.add((double) feature8); //12

        // Feature 9: 1 / BBBB
        double feature9 = BBBB;
        // features.put("feature9", feature9);
        features.add((double) feature9);

        features.add(0.0); //14

        // Feature 10: High of Feature 9
        if (feature9 > highFeature9) {
            highFeature9 = feature9;
            timeOfHighFeature9 = feature1;
        }
        // features.put("feature10", highFeature9);
        features.add(highFeature9); //15

        // Feature 11: Time of the high for Feature 9
        int feature11 = timeOfHighFeature9;
        // features.put("feature11", (double) feature11);
        features.add((double) feature11); //16

        // Feature 12: Minutes since high of Feature 9
        int feature12 = feature1 - timeOfHighFeature9;
        // features.put("feature12", (double) feature12);
        features.add((double) feature12); //17

        // Feature 13: Low of Feature 9
        if (feature9 < lowFeature9) {
            lowFeature9 = feature9;
            timeOfLowFeature9 = feature1;
        }
        // features.put("feature13", lowFeature9);
        features.add(lowFeature9); //18

        // Feature 14: Time of the low for Feature 9
        int feature14 = timeOfLowFeature9;
        // features.put("feature14", (double) feature14);
        features.add((double) feature14); //19

        // Feature 15: Minutes since low of Feature 9
        int feature15 = feature1 - timeOfLowFeature9;
        // features.put("feature15", (double) feature15);
        features.add((double) feature15); //20

        // Feature 16: CCCC
        double feature16 = CCCC;
        // features.put("feature16", feature16);
        features.add(feature16); //21

        // Feature 17: TTTT
        double feature17 = TTTT;
        // features.put("feature17", feature17);
        features.add(feature17); //22

        // Feature 18: CCCC / TTTT
        double feature18 = CCCC / TTTT;
        // features.put("feature18", feature18);
        features.add(feature18); //23

        // Feature 19: Difference between EquityLast and the next whole number above
        double feature19 = Math.ceil(equityLast) - equityLast;
        // features.put("feature19", feature19);
        features.add(feature19); //24

        // Feature 20: Difference between EquityLast and the next whole number below
        double feature20 = equityLast - Math.floor(equityLast);
        // features.put("feature20", feature20);
        features.add(feature20); //25

        // Feature 21: CCCC crosses below TTTT
        int feature21 = (prevCCCC >= prevTTTT && CCCC < TTTT) ? 1 : 0;
        // features.put("feature21", (double) feature21);
        features.add((double) feature21); //26

        // Feature 22: CCCC crosses above TTTT
        int feature22 = (prevCCCC <= prevTTTT && CCCC > TTTT) ? 1 : 0;
        // features.put("feature22", (double) feature22);
        features.add((double) feature22); //27

        // Feature 23: Which flagged last (Feature 21 or Feature 22)
        if (feature21 == 1) feature23LastFlag = -1;
        if (feature22 == 1) feature23LastFlag = 1;
        int feature23 = feature23LastFlag;
        // features.put("feature23", (double) feature23);
        features.add((double) feature23); //28

        // Feature 24: Difference in TTTT from the previous value
        double feature24 = TTTT - prevTTTT;
        // features.put("feature24", feature24);
        features.add(feature24); //29

        // Feature 25: Difference in CCCC from the previous value
        double feature25 = CCCC - prevCCCC;
        // features.put("feature25", feature25);
        features.add(feature25); //30

        // Feature 26: AAAA / BBBB
        double feature26 = AAAA / BBBB;
        // features.put("feature26", feature26);
        features.add(feature26); //31

        // Feature 27: AAAA / BBBB crosses .5 or 2 boundaries
        int feature27 = 0;
        if (feature26 < 0.5) {
            feature27 = -1;
        } else if (feature26 > 2.0) {
            feature27 = 1;
        }
        // features.put("feature27", (double) feature27);
        features.add((double) feature27); //32

        // Feature 28: Both AAAA and 1/BBBB below CCCC
        int feature28 = (AAAA < CCCC && feature9 < CCCC) ? 1 : 0;
        // features.put("feature28", (double) feature28);
        features.add((double) feature28); //33

        // Feature 29: Both AAAA and 1/BBBB above CCCC
        int feature29 = (AAAA > CCCC && feature9 > CCCC) ? 1 : 0;
        // features.put("feature29", (double) feature29);
        features.add((double) feature29); //34

        // Feature 30: Which flagged last (Feature 28 or Feature 29)
        if (feature28 == 1) feature30LastFlag = 1;
        if (feature29 == 1) feature30LastFlag = -1;
        int feature30 = feature30LastFlag;
        // features.put("feature30", (double) feature30);
        features.add((double) feature30); //35

        // Feature 31: Time of last flag (Feature 28 or 29)
        if (feature28 == 1 || feature29 == 1) {
            feature30Time = feature1;
        }
        int feature31 = feature30Time;
        // features.put("feature31", (double) feature31);
        features.add((double) feature31); //36

        // Feature 32: Minutes since last flag (Feature 31)
        int feature32 = (feature30Time != -1) ? feature1 - feature30Time : -1;
        // features.put("feature32", (double) feature32);
        features.add((double) feature32); //37

        features.add(0.0); //38
        features.add(0.0); //39

        // Feature 33: Time of the low for EquityLast
        if (equityLast < lowEquityLast) {
            lowEquityLast = equityLast;
            timeOfLowEquityLast = feature1;
        }

        int feature33 = timeOfLowEquityLast;
        // features.put("feature33", (double) feature33);
        features.add((double) feature33); //40


        // Feature 34: Time of the high for EquityLast
        if (equityLast > highEquityLast) {
            highEquityLast = equityLast;
            timeOfHighEquityLast = feature1;
        }
        int feature34 = timeOfHighEquityLast;
        // features.put("feature34", (double) feature34);
        features.add((double) feature34); //41

        // Feature 35: Minutes since low of EquityLast
        int feature35 = feature1 - timeOfLowEquityLast;
        // features.put("feature35", (double) feature35);
        features.add((double) feature35); //42

        // Feature 36: Minutes since high of EquityLast
        int feature36 = feature1 - timeOfHighEquityLast;
        // features.put("feature36", (double) feature36);
        features.add((double) feature36); //43

        // Feature 37: Ratio of current price to low of the day for EquityLast
        double feature37 = equityLast / lowEquityLast;
        // features.put("feature37", feature37);
        features.add(feature37); //44

        // Feature 38: Ratio of current price to high of the day for EquityLast
        double feature38 = equityLast / highEquityLast;
        // features.put("feature38", feature38);
        features.add(feature38); //45

        // Feature 39: Low or high tracking for AAAA based on thresholds
        if (AAAA < 0.5) {
            feature39Low = Math.min(feature39Low, AAAA);
        } else if (AAAA > 2.0) {
            feature39High = Math.max(feature39High, AAAA);
        }
        int feature39 = (AAAA < 2 && AAAA > 0.5) ? 0 : (AAAA < 0.5 || AAAA > 2) ? (int)(AAAA / ((AAAA < 0.5) ? feature39Low : feature39High)) : -1;
        // features.put("feature39", (double) feature39);
        features.add((double) feature39); //46

        // Feature 40: Handling feature 39 boundaries
        int feature40 = (AAAA < 2 && AAAA > 0.5) ? 0 : -1;
        // features.put("feature40", (double) feature40);
        features.add((double) feature40); //47

        // Feature 41: BBBB / CCCC
        double feature41 = BBBB / CCCC;
        // features.put("feature41", feature41);
        features.add(feature41); //48

        features.add(0.0); //49
        features.add(1.0); //50

        return features;

    }
}
