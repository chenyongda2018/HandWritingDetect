package example.chen.com.detecthandwriting.util;

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
