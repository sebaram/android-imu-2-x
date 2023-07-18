package kr.ac.kaist.arrc.imustreamwear;

import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import java.util.ArrayList;

import kr.ac.kaist.arrc.imustreamlib.classifier.ComboInput;
import kr.ac.kaist.arrc.imustreamlib.classifier.RunWeka;


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
    private ArrayList<Long> time;

    public String results = "";
    private Long lastTime = 0L;
    private float deltatime = 1000;

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

    public String classifySwipe(){
        String result = "";
        double[] input = ComboInput.genFeatures(xPositions, yPositions, time);
        try{
            result = RunWeka.classifyData("", input);
            if(System.currentTimeMillis()-lastTime>deltatime){
                results = result;
            }else{
                results += "\n"+result;
            }
            lastTime = System.currentTimeMillis();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG, "time,event,x,y: "+event.getEventTime() +","+event.getAction()+"," + event.getX() + "," + event.getY());
        // swipe event detection
        if(event.getPointerCount()<2){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // reset(event.getDownTime());
                    xPositions = new ArrayList<>();
                    yPositions = new ArrayList<>();
                    time = new ArrayList<>();

                    xPositions.add(event.getX());
                    yPositions.add(event.getY());
                    time.add(event.getEventTime());
                    break;
                case MotionEvent.ACTION_MOVE:
                    xPositions.add(event.getX());
                    yPositions.add(event.getY());
                    time.add(event.getEventTime());
                    break;
                case MotionEvent.ACTION_UP:
                    if (event.getPointerCount() == 1){
                        xPositions.add(event.getX());
                        yPositions.add(event.getY());
                        time.add(event.getEventTime());


                    }

                    break;
            }
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
        onTouchEvent2(event);


        return false;
    }

    public abstract void onTwoFingersDoubleTap();
    public abstract void onSwipeEvent(String direction);
    public abstract void onTouchEvent2(MotionEvent event);


}