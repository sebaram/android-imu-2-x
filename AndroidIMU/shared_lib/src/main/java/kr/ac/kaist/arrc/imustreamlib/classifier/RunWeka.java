package kr.ac.kaist.arrc.imustreamlib.classifier;


import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import kr.ac.kaist.arrc.imustreamlib.R;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.core.converters.ConverterUtils;

import static kr.ac.kaist.arrc.imustreamlib.CONSTANTS.GESTURE_CLASSES;
import static kr.ac.kaist.arrc.imustreamlib.CONSTANTS.THRE_DIST;


public class RunWeka {
    private Instances data;
    private static Instances classifyBuffer;
    private static Classifier classifier;
    private static RandomForest rf_classifier;
    private String folder_path = android.os.Environment.getExternalStorageDirectory().toString() + "/ComboInput/model/trained.model";

    private static String TAG = "ComboInput:runWeka";
    private Context ctxt;

    public RunWeka(Context ctxt) {
        this.ctxt = ctxt;
    }

    public RunWeka(String root_folder) {
        File file = new File(root_folder);

        if (file.isDirectory()) {
            folder_path = root_folder;
        } else {
            System.out.println("Type folder correctly: " + root_folder);
        }
    }


    public void loadModel() {

        InputStream model = ctxt.getResources().openRawResource(R.raw.combo_input); // getting XML

        try {
            // load model
            classifier = (Classifier) SerializationHelper.read(model);
            Log.d(TAG, "WEKA:loadData::Model LOADED");

            // create classify Buffer for runInstance
            classifyBuffer = new Instances("Classification Instances", createDataAttributes(new ArrayList<String>(GESTURE_CLASSES.values())), 1024);
            classifyBuffer.setClassIndex(classifyBuffer.numAttributes() - 1);
            Log.d(TAG, "WEKA::buffer created| atts num=" + classifyBuffer.numAttributes());


        } catch (Exception e) {
            Log.d(TAG, "WEKA:loadData::Exception occurred");
            Log.d(TAG, "WEKA:loadData" + e.getMessage());
            e.printStackTrace();
        }
    }




    private List<String> retrieveClasses(Instances instances) {
        List<String> classes = new ArrayList<String>();
        instances.setClassIndex(instances.numAttributes() - 1);
        Enumeration e = instances.classAttribute().enumerateValues();
        while (e.hasMoreElements()) {
            classes.add((String) e.nextElement());
        }
        return classes;
    }

    private ArrayList<Attribute> createDataAttributes(List<String> classes) {
        ArrayList<Attribute> atts = new ArrayList<Attribute>();

        String[] attributes = { "first_x", "first_y", "first_distance",
                                "last_x", "last_y", "last_distance",
                                "first_last_theta", "duration",

                                "move_count",

                                "move_speed_x", "move_speed_y", "move_speed",
                                "move_speed_sd_x","move_speed_sd_y", "move_speed_sd",
                                "move_accel_x", "move_accel_y", "move_accel"};

        for (int i = 0; i < attributes.length; i++) {
            atts.add(new Attribute(attributes[i]));
        }
        atts.add(new Attribute("Target", classes));

        return atts;
    }
    public void testClassify() {
        if (classifier == null) {
            Log.d(TAG, "WEKA::testClassify::classifier is null");
            return;
        }
        double[] test_input_left = {224, 293, 66.0681466366357, 229, 22, 205.009755865422, -1.55234823540374, 196, 8, 0.0289017341040462, -1.56647398843931, 84.5070473988439, 0.101975983477425, 1.05596677832457, 98.7435716137932, 0.0404624277456647, 0.0805876685934489, 56.8045002890173};
        double[] test_input_right = {178, 251, 54.561891462815, 434, 232, 207.060377667964, -0.0740829225490337, 330, 15, 0.879725085910653, -0.0652920962199313, 30.7993628865979, 0.83488561155043, 0.246301297614392, 32.8482172914135, 0.0129526830557758, 0.0111022997620936, 5.68453957176843
        };
        double[] test_input_up = {226, 278, 51.0098029794274, 234, 19, 208.117755129158, -1.53991811340672, 338, 15, 0.0263157894736842, -0.851973684210526, 41.1072782894737, 0.077471013529923, 1.11815887228245, 79.6013463469022, 0.00708502024291498, -0.0194838056680162, 24.8261822874494
        };
        double[] test_input_down = {212, 241, 20.5182845286832, 208, 449, 222.811579591367, 1.5900247258946, 432, 19, -0.0102791878172589, 0.528451776649746, 12.220683248731, 0.0938652167778226, 0.535290430944124, 13.8380855573157, -0.00532099134069876, 0.0188384592415646, 1.43063693639893
        };
        try {
            classifyData("left", test_input_left);
            classifyData("right", test_input_right);
            classifyData("up", test_input_up);
            classifyData("down", test_input_down);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String classifyData(String expected, double[] input) throws Exception {


        //Classify the new instance and get distribution
        //int classLabel = (int) classifier.classifyInstance(classifyBuffer.get(0));
//        Instance inst = new DenseInstance(0, input);
        classifyBuffer.add(new DenseInstance(0, input));

        double[] distribution = classifier.distributionForInstance(classifyBuffer.get(0));
        //Clean up buffer
        classifyBuffer.remove(0);

        //Return String containing the probability distribution of the classes
        String out = "";
        int classified = 0;
        for (int i = 0; i < distribution.length; i++) {
            out += GESTURE_CLASSES.get(i) + ":" + String.format("%.2f", distribution[i]);
            out += (i < distribution.length - 1) ? "," : "\n";
            //Update the highest distribution as the classified
            if (distribution[i] > distribution[classified])
                classified = i;
        }
        Log.d(TAG, "WEKA::classifyData::("+expected+") " + out);

        /**
         * 1. distribution check
         * **/
        if (distribution[classified] < THRE_DIST) {
//            classified = 5;
            return "null";
        }else{
            return (String) GESTURE_CLASSES.get(classified);
        }

    }

}
