package example.chen.com.detecthandwriting.util;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DetectUtil {

    public static String extractText(float[] result) {
        String[] ans = {
                "0",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "A",
                "B",
                "C",
                "D",
                "E",
                "F",
                "G",
                "H",
                "I",
                "J",
                "K",
                "L",
                "M",
                "N",
                "O",
                "P",
                "Q",
                "R",
                "S",
                "T",
                "U",
                "V",
                "W",
                "X",
                "Y",
                "Z",
                "a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h",
                "i",
                "j",
                "k",
                "l",
                "m",
                "n",
                "o",
                "p",
                "q",
                "r",
                "s",
                "t",
                "u",
                "v",
                "w",
                "x",
                "y",
                "z"
        };


        int mi = 0;
        float max = 0;
        for (int i = 0; i < 62; i++) {
            if (result[i] > max) {
                max = result[i];
                mi = i;
            }
            String mes = "Probability of " + i + ": " + result[i];
//            mLogger.d("mess" + mes);
        }

        if (max > 0.50f) {
            String resd = ans[mi];
            String con = String.format("%.1f", max * 100);
            String dt = "Detected = " + resd + " (" + con + "%)";

            return resd;
        } else {
            String resd = ans[mi];
            String con = String.format("%.1f", max * 100);
            String dt = "Maybe: " + resd + " (" + con + "%)";

            return resd;
        }
    }







}
