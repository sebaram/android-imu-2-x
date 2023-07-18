package kr.ac.kaist.arrc.imustreamlib;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by arrc on 4/10/2018.
 */

public class CONSTANTS {

    // force devices to connect to this wifi network
    // should put Phone(leg)'s hotspot ssid and passwd
    // ***IMPORTANT***CANNOT CHANGE ON APP
    public static boolean FORCE_WIFI = false;
    public static String SSID = "ssid";
    public static String PASSWD = "passwd";



    // target IP address
    // usually 192.168.43.1 is gateway(phone) IP for android hotspot
    // can change by sending broadcast message
    public static String IP_ADDRESS = "192.168.0.8";

    // each devices' IP address
    // DON'T NEED TO CHANGE (initialized by input packet)
    public static String GLASS_IP = "192.168.43.106";
    public static String WEAR_IP = "192.168.43.191";
    public static String PHONE_IP = "this";

    public static int DEVICE_ID = 0; //0:leg, 1:hand, 2:head

    public static int SS_W_SIZE = 1000;

    // port for sensor data stream
//    public static int PORT = 12562;
    public static int PORT = 12563;

    // port for Glass noti sharing
    public static int DATA_PORT = 11563;
//    public static int DATA_BYTE_SIZE = 48;
    public static int DATA_BYTE_SIZE = 48;

    /**
     * Android sensor manager registered Interval Value (ms)
     **/

    //sensor delay for SensorManager in mssec
    public static int SENSOR_DELAY = 18;


    /**
     * Resample Interval Value (ms)
     * - Sensor manager doesn't give correct interval
     **/
//    public static final int SENDING_INTERVAL = 50; //50Hz
    public static int SENDING_INTERVAL = 10;
//    public static int SENDING_INTERVAL = SENSOR_DELAY - 1;    //100Hz
    public static int SENDING_INTERVAL_HALF = SENDING_INTERVAL / 2;    // for thread sleep in UDP socket part

    // variable to share with sending part
    // Gyro    Acc,   Rotvec  Millis  ID  MAG
    // 4,4,4, 4,4,4, 4,4,4,4,   8,    4, 4,4,4
    public static int BYTE_SIZE = 64;
    public static int QUEUE_SIZE = 100;

    public static void setSensorDelay(int delay) {
        SENSOR_DELAY = delay;
        SENDING_INTERVAL = SENSOR_DELAY *2;
        SENDING_INTERVAL_HALF = SENDING_INTERVAL / 2;
    }


    // error notification
    // ERROR_NOTI_TIME: alert error when connection failed more than this time(msec)
    // ERROR_NOTI_REPEAT: repeat alert in this period
    public static int ERROR_NOTI_TIME = 10000;
    public static int ERROR_NOTI_REPEAT = 2000;



    public static Map GESTURE_CLASSES = new HashMap(){{
        put(0,"left");
        put(1,"down");
        put(2,"up");
        put(3,"right");

        put(4,"null");

    }};
    public static double THRE_DIST = 0.8;

}

