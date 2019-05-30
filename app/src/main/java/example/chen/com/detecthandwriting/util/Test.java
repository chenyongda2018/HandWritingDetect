package example.chen.com.detecthandwriting.util;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Test {

    static {
        if(!OpenCVLoader.initDebug()){
            System.out.println("OpenCV not loaded");
        } else {
            System.out.println("OpenCV loaded");
        }
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("mat = " + mat.dump());
    }




}
