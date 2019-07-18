package example.chen.com.detecthandwriting;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.LinkedList;

import example.chen.com.detecthandwriting.util.DetectUtil;
import example.chen.com.detecthandwriting.util.ImageUtil;
import example.chen.com.detecthandwriting.util.SdCardUtils;
import example.chen.com.detecthandwriting.util.Tf;
import me.pqpo.smartcropperlib.view.CropImageView;

public class CheckActivity extends AppCompatActivity {
    public static final String TAG = "CheckActivity";
    private String mImageFilePath;
    private Handler mDetectHandler = new Handler();

    private Paint mPaint;//画笔  在图片上画出错误部分
    private AppCompatSpinner mSpinner;
    private Button mDetectBtn;
    private TextView mScoreTv;
    private CropImageView mCropImageView;
    private PhotoView mJiuGongGePv; //存放九宫格的视图View
    private Bitmap mRawBitmap;//剪裁前
    private Bitmap mCropBitmap; //剪裁后
    private ProgressDialog mProgressDialog;

    private int mCellWidth = 0;
    private int mCellHeight = 0;

    public float mItemCount = 0;
    public float mRightCount = 0;

    private String mLetterSwitch; //要识别的字母
    private int mLetterPosition;
    private Bitmap[][] mBitmaps;
    private String[] mLetters;


    private static final String input_node = "reshape_1_input";
    private static final long[] input_shape = {1, 784};
    private static final String output_node = "dense_3/Softmax";
    private static int scanTimes = 0;
    static int[] mErrorList = new int[126];

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        Tf.init(this);


        //创建文件夹
        SdCardUtils.createAppDir();
        mLetters = getResources().getStringArray(R.array.letters);
        SdCardUtils.createLetterDir(mLetters);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        initView();
        mCellWidth = mCropBitmap.getWidth() / 3;//单元格宽度
        mCellHeight = mCropBitmap.getHeight() / 3;// 单元格高度

    }

    private void initView() {
        mProgressDialog = new ProgressDialog(this);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(1.1f);
        mDetectBtn = findViewById(R.id.detect_puzzle_btn);
        mCropImageView = findViewById(R.id.puzzle_crop_image_view);
        mScoreTv = findViewById(R.id.score_tv);
        mJiuGongGePv = findViewById(R.id.puzzle_jiu_gong_ge_view);
        mSpinner = findViewById(R.id.which_detect_spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mLetterPosition = position;
                mLetterSwitch = mLetters[position]; //规定当前识别的哪种字母
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //加载并剪裁照片
        mImageFilePath = getIntent().getStringExtra("image_path"); //接收拍的的照片的文件路径
        Log.d(TAG, mImageFilePath);
        mRawBitmap = BitmapFactory.decodeFile(mImageFilePath);//剪裁前的照片
        mCropImageView.setImageToCrop(mRawBitmap);
        mCropBitmap = mCropImageView.crop();//获得剪裁后的图片
        mJiuGongGePv.setImageBitmap(mCropBitmap);//放进View容器
        mCropImageView.setVisibility(View.GONE);
        mJiuGongGePv.setVisibility(View.VISIBLE);

        mDetectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Mat dest = new Mat();
//                Utils.bitmapToMat(convert2Gray(mCropBitmap), dest); // 灰度化的bitmap 转换成 mat
//                ImageUtil.binaryMatZation(dest); //将Mat二值化
//                Utils.matToBitmap(dest,mCropBitmap );
//                Bitmap src = convert2Gray(mCropBitmap);
//                src = ImageUtil.getBinaryzationBitmap(src);
//                mJiuGongGePv.setImageBitmap(src);
                mProgressDialog.setMessage("正在检测...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                new Thread(runnable).start();
            }
        });
    }


    /**
     * 识别线程
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            detectBitmaps(); //识别bitmap
//            Bitmap src = convert2Gray(mCropBitmap);
//            src = ImageUtil.getBinaryzationBitmap(src);
//            Bitmap finalSrc = src;
            mDetectHandler.post(new Runnable() {
                @Override
                public void run() {
                    mJiuGongGePv.setImageBitmap(mCropBitmap);
//                    mJiuGongGePv.setImageBitmap(finalSrc);
                    mScoreTv.setText("一共:" + mItemCount + "个" + "\n" + "正确率" + mRightCount / mItemCount);
                    mProgressDialog.dismiss();
                    mItemCount = 0;
                    mRightCount = 0;
                }
            });
        }
    };

    /**
     * 识别一个九宫格图片
     */
    private void detectBitmaps() {

        mBitmaps = new Bitmap[mCellHeight][mCellWidth];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                mItemCount++;
                int xx = x * mCellWidth;
                int yy = y * mCellHeight;
                //if (y==2 && x==2 ) {
                //    mBitmaps[x][y] = Bitmap.createBitmap(mCropBitmap, xx + mCellWidth / 8, yy + mCellHeight / 8,
                //            (mCellWidth / 8) * 6, (mCellHeight / 8) * 6);
                //}else if (y==2) {
                //    mBitmaps[x][y] = Bitmap.createBitmap(mCropBitmap, xx + mCellWidth / 8, yy + mCellHeight / 8,
                //            (mCellWidth / 8) * 7, (mCellHeight / 8) * 6);
                //} else if (x==2) {
                //    mBitmaps[x][y] = Bitmap.createBitmap(mCropBitmap, xx + mCellWidth / 8, yy + mCellHeight / 8,
                //            (mCellWidth / 8) * 6, (mCellHeight / 8) * 7);
                //} else {
                //    mBitmaps[x][y] = Bitmap.createBitmap(mCropBitmap, xx + mCellWidth / 8, yy + mCellHeight / 8,
                //            (mCellWidth / 8) * 6, (mCellHeight / 8) * 6);
                //}
                mBitmaps[x][y] = Bitmap.createBitmap(mCropBitmap, xx + mCellWidth / 11, yy + mCellHeight / 11,
                        (mCellWidth / 11) * 9, (mCellHeight / 11) * 9);

                //mBitmaps[x][y] = Bitmap.createBitmap(mCropBitmap, xx, yy,
                //        mCellWidth, mCellHeight);

                String result = "";
                Bitmap bitmap = mBitmaps[x][y];

                //LinkedList<Bitmap> bitmaps = ImageUtil.getRectangleBitmaps(bitmap);
                //
                //Log.d(TAG, "getRectangleBitmaps size " + bitmaps.size());
                //for (int i = 0; i < bitmaps.size(); i++) {
                //    SdCardUtils.saveBitmapToSdWithPoint(bitmap, mLetterSwitch.toLowerCase(), x, y, mLetterPosition);
                //}

                bitmap = ImageUtil.convert2Gray(bitmap); //灰度化
                bitmap = ImageUtil.getBinaryzationBitmap(bitmap);//二值化
                result = detectText(bitmap);//识别结果
                String originalRes =result;
                String transResult = result.toLowerCase().trim();

                if (!mLetterSwitch.toLowerCase().equals(transResult)) { //如果识别错误
                    char c = originalRes.charAt(0);
                    if (c != ' ') {
                        mErrorList[c]++;
                    }
                    SdCardUtils.saveBitmapToSdWithPoint(bitmap, result, x, y, mLetterPosition);
                    drawErrorOnBitmap(mCropBitmap, xx, yy, mCellWidth, mCellHeight);
                } else if (mLetterSwitch.toLowerCase().equals(transResult)) {
                    mRightCount++;
                }
                Log.d("detectBitmaps", "当前选择:" + mLetterSwitch + "\t识别结果:" + result + "\t判定" + (mLetterSwitch.toLowerCase().equals(result)));
            }
        }

        scanTimes++;
        if(scanTimes % 5 == 0) {
            Log.d(TAG, "scanTimes init"+scanTimes);
            mErrorList = new int[126];
            scanTimes=1;
        }

        if(scanTimes % 4 ==0 && scanTimes > 0) {
            for (int i = 0; i < mErrorList.length; i++) {
                if (mErrorList[i] > 0) {
                    Log.d(TAG, "lettererror: " + (char) (i) + "-" + mErrorList[i] + " times");
                }
            }
        }
    }


    /**
     * 入口方法，识别一个图片的所有子图，看看其中只要有一张正确，那么就说明填写字符正确
     *
     * @param bitmaps 入口方法，识别一个图片的所有子图，看看其中只要有一张正确，那么就说明填写字符正确
     * @param result  正确结果
     * @return
     */
    private boolean detectBitmaps(LinkedList<Bitmap> bitmaps, String result) {
        int i = 0;
        while (i < bitmaps.size()) {
            Bitmap b = Bitmap.createBitmap(bitmaps.get(i));
            if (detectText(b).equals(result)) return true;
            i++;
        }
        return false;
    }


    /**
     * 识别单张图片-核心识别方法
     *
     * @param bitmap 输入的图像
     * @return 识别结果
     */
    private String detectText(Bitmap bitmap) {
        //bitmap = ImageUtil.getBinaryzationBitmap(bitmap); //二值化
        float[] pb = ImageUtil.getPixelData(bitmap);
        Tf.getInstance().feed(Tf.INPUT_NODE, pb, Tf.INPUT_SHAPE);
        Tf.getInstance().run(new String[]{Tf.OUTPUT_NODE});
        float[] data = new float[62];
        Tf.getInstance().fetch(Tf.OUTPUT_NODE, data);
        return DetectUtil.extractText(data);
    }


    /**
     * 画出检测结果出错的地方
     *
     * @param bitmap
     * @param left
     * @param top
     * @param width
     * @param height
     */
    public void drawErrorOnBitmap(Bitmap bitmap, int left, int top, int width, int height) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawLine(left, top, left + width, top + height, mPaint);
        canvas.drawLine(left + width, top, left, top + height, mPaint);
        canvas.save();
    }


}
