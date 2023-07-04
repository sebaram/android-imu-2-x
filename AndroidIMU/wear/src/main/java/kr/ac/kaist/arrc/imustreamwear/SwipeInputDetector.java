package kr.ac.kaist.arrc.imustreamwear;

import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import java.util.ArrayList;


public abstract class SwipeInputDetector {
    String TAG = "SwipeInputDetector";

    private static final int TIMEOUT = ViewConfiguration.getDoubleTapTimeout() + 100;
    private long mFirstDownTime = 0;
    private boolean mSeparateTouches = false;
    private byte mTwoFingerTapCount = 0;

    private static int WIDTH = 320;
    private static int HEIGHT = 320;
    private static int DISTANCE_THRE = 160-16;

    private ArrayList<Float> xPositions;
    private ArrayList<Float> yPositions;


    private void resetDoubleTap(long time) {
        mFirstDownTime = time;
        mSeparateTouches = false;
        mTwoFingerTapCount = 0;
    }
    public void updateDisplaySize(int width, int height){
        WIDTH = width;
        HEIGHT = height;

        // Assume circle display
        DISTANCE_THRE = (int)(0.9 * WIDTH/2);
        Log.d(TAG, "updateDisplaySize: " + WIDTH + ", " + HEIGHT);
    }
    private float calculateAngle(float x, float y){
        float angle = (float) Math.toDegrees(Math.atan2(y - HEIGHT/2, x - WIDTH/2));
        if(angle < 0){
            angle += 360;
        }
        return angle;
    }
    private float calculateDistance(float x, float y){
        float distance = (float) Math.sqrt(Math.pow(x - WIDTH/2, 2) + Math.pow(y - HEIGHT/2, 2));
        return distance;
    }
    private float calLineFit(){
        float sumX = 0;
        float sumY = 0;
        float sumXY = 0;
        float sumXX = 0;
        float sumYY = 0;
        for(int i = 0; i < xPositions.size(); i++){
            sumX += xPositions.get(i);
            sumY += yPositions.get(i);
            sumXY += xPositions.get(i) * yPositions.get(i);
            sumXX += xPositions.get(i) * xPositions.get(i);
            sumYY += yPositions.get(i) * yPositions.get(i);
        }
        float a = (sumY * sumXX - sumX * sumXY) / (xPositions.size() * sumXX - sumX * sumX);
        float b = (xPositions.size() * sumXY - sumX * sumY) / (xPositions.size() * sumXX - sumX * sumX);
        float r = (float) Math.sqrt((sumXY * sumXY + sumYY * sumYY) / (sumXX * sumXX + sumYY * sumYY));
        Log.d(TAG, "calLineFit: " + a + ", " + b + ", " + r);
        return r;
    }

    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "x,y: " + event.getX() + ", " + event.getY());

        // swipe event detection
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // reset(event.getDownTime());
                xPositions = new ArrayList<>();
                yPositions = new ArrayList<>();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    xPositions.add(event.getX());
                    yPositions.add(event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                if (event.getPointerCount() == 1){
                    float x = event.getX();
                    float y = event.getY();
                    float angle = calculateAngle(x, y);
                    float distance = calculateDistance(x, y);
                    Log.d(TAG, "UP angle: " + angle + ", distance: " + distance);
                    calLineFit();
                    if (distance > DISTANCE_THRE) {
                        if (angle > 45 && angle < 135) {
                            onSwipeEvent("up");
                        } else if (angle > 225 && angle < 315) {
                            onSwipeEvent("down");
                        } else if (angle > 135 && angle < 225) {
                            onSwipeEvent("left");
                        } else {
                            onSwipeEvent("right");
                        }
                    }
                }

                break;
        }



        // double touch event detection
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if(mFirstDownTime == 0 || event.getEventTime() - mFirstDownTime > TIMEOUT)
                    this.resetDoubleTap(event.getDownTime());
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if(event.getPointerCount() == 2)
                    mTwoFingerTapCount++;
                else
                    mFirstDownTime = 0;
                break;
            case MotionEvent.ACTION_UP:
                if(!mSeparateTouches)
                    mSeparateTouches = true;
                else if(mTwoFingerTapCount == 2 && event.getEventTime() - mFirstDownTime < TIMEOUT) {
                    onTwoFingersDoubleTap();
                    mFirstDownTime = 0;
                    return true;
                }
        }

        return false;
    }

    public abstract void onTwoFingersDoubleTap();
    public abstract void onSwipeEvent(String direction);


}