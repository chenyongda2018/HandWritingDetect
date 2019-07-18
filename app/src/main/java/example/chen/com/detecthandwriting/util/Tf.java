package example.chen.com.detecthandwriting.util;

import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * TensorFlow模型的单例类
 *
 * @author cyd
 * @date 2019/7/13 17:14
 */
public class Tf {

    public static final String INPUT_NODE = "reshape_1_input";
    public static final long[] INPUT_SHAPE = {1, 784};
    public static final String OUTPUT_NODE = "dense_3/Softmax";

    private static TensorFlowInferenceInterface mInferenceInterface;

    public static void init(Context context) {
        if (mInferenceInterface == null) {
            mInferenceInterface = new TensorFlowInferenceInterface(context.getApplicationContext().getAssets(), "emnist.pb");
        }
    }

    public static TensorFlowInferenceInterface getInstance() {
        if (mInferenceInterface != null) {
            return mInferenceInterface;
        }
        throw new RuntimeException("Tf is not init...");
    }
}
