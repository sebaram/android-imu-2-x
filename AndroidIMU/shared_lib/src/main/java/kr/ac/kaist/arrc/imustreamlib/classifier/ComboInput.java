package kr.ac.kaist.arrc.imustreamlib.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ComboInput {
    private static final double[] MID = new double[]{227, 227};

    public static double distanceMid(double x, double y) {
        return Math.sqrt(Math.pow(x - MID[0], 2) + Math.pow(y - MID[1], 2));
    }

    public static double directionFirstLast(double x0, double y0, double x1, double y1) {
        return Math.atan2(y1 - y0, x1 - x0);
    }


    public static double[] genFeatures(ArrayList<Float> xValues, ArrayList<Float> yValues, ArrayList<Long> timeValues) {
        double[] result = new double[19];


        /** Using Touch Down/Up point
         *
         */
        double firstX = xValues.get(0);
        double firstY = yValues.get(0);
        double lastX = xValues.get(xValues.size() - 1);
        double lastY = yValues.get(yValues.size() - 1);

        double firstDistance = distanceMid(firstX, firstY);
        double lastDistance = distanceMid(lastX, lastY);

        result[0] = firstX;
        result[1] = firstY;
        result[2] = firstDistance;

        result[3] = lastX;
        result[4] = lastY;
        result[5] = lastDistance;

        result[6] = directionFirstLast(firstX, firstY, lastX, lastY);
        result[7] = (double) timeValues.get(timeValues.size() - 1) - timeValues.get(0);



        /** Using Touch move points
         *
         */
        ArrayList<Float> moveXValues = new ArrayList<>();
        ArrayList<Float> moveYValues = new ArrayList<>();
        ArrayList<Long> moveTimeValues = new ArrayList<>();

        for(int i = 0; i < xValues.size(); i++) {
            // remove first and last point: it is not a moving point
            if(i==0 || i==xValues.size()-1) continue;

            moveXValues.add(xValues.get(i));
            moveYValues.add(yValues.get(i));
            moveTimeValues.add(timeValues.get(i));
        }

        result[8] = average(moveXValues);

        ArrayList<Float> dxValues = new ArrayList<>();
        ArrayList<Float> dyValues = new ArrayList<>();
        ArrayList<Float> dx2y2Values = new ArrayList<>();
        ArrayList<Long> dtValues = new ArrayList<>();
        for(int i = 1; i < moveXValues.size(); i++) {
            dxValues.add(moveXValues.get(i) - moveXValues.get(i - 1));
            dyValues.add(moveYValues.get(i) - moveYValues.get(i - 1));
            dx2y2Values.add( (float) Math.sqrt(Math.pow(moveXValues.get(i) - moveXValues.get(i - 1), 2) + Math.pow(moveYValues.get(i) - moveYValues.get(i - 1), 2)));
            dtValues.add(moveTimeValues.get(i) - moveTimeValues.get(i - 1));
        }

        float avg_dx = (float) average(dxValues);
        float avg_dy = (float) average(dyValues);
        float avg_dt = (float) averageLong(dtValues);
        float std_dx = (float) standardDeviation(dxValues);
        float std_dy = (float) standardDeviation(dyValues);

        result[9] = avg_dx/ avg_dt;
        result[10] = avg_dy/ avg_dt;
        result[11] = average(dx2y2Values) / avg_dt;

        result[12] = std_dx/ avg_dt;
        result[13] = std_dy/ avg_dt;
        result[14] = (float) standardDeviation(dx2y2Values) / avg_dt;



        ArrayList<Float> ddxValues = new ArrayList<>();
        ArrayList<Float> ddyValues = new ArrayList<>();
        ArrayList<Float> ddy2dy2Values = new ArrayList<>();
        for(int i = 1; i < dxValues.size(); i++) {
            ddxValues.add(dxValues.get(i) - dxValues.get(i - 1));
            ddyValues.add(dyValues.get(i) - dyValues.get(i - 1));
            ddy2dy2Values.add( (float) Math.sqrt(Math.pow(dxValues.get(i) - dxValues.get(i - 1), 2) + Math.pow(dyValues.get(i) - dyValues.get(i - 1), 2)));
        }

        result[15] = average(ddxValues) / avg_dt;
        result[16] = average(ddyValues) / avg_dt;
        result[17] = average(ddy2dy2Values) / avg_dt;


        return result;
    }

    public static double average(ArrayList<Float> values) {
        double sum = 0;
        for(double val : values) {
            sum += val;
        }
        return sum / values.size();
    }

    public static double averageLong(ArrayList<Long> values) {
        double sum = 0;
        for(long val : values) {
            sum += val;
        }
        return sum / values.size();
    }

    public static double standardDeviation(ArrayList<Float> values) {
        double avg = average(values);
        double sum = 0;
        for(double val : values) {
            sum += Math.pow(val - avg, 2);
        }
        return Math.sqrt(sum / (values.size() - 1));
    }
}
