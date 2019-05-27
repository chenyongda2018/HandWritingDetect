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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import example.chen.com.detecthandwriting.util.DetectUtil;
import example.chen.com.detecthandwriting.util.ImageUtil;
import example.chen.com.detecthandwriting.util.SdCardUtils;
import me.pqpo.smartcropperlib.view.CropImageView;

import static example.chen.com.detecthandwriting.util.ImageUtil.convertGray;

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
    private int mLetterPostion;
    private Bitmap[][] mBitmaps;
    private String[] mLetters;

    TensorFlowInferenceInterface mInferenceInterface;
    private static final String input_node = "reshape_1_input";
    private static final long[] input_shape = {1, 784};
    private static final String output_node = "dense_3/Softmax";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_check);
        mInferenceInterface = new TensorFlowInferenceInterface(getApplication().getAssets(), "emnist.pb");

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
                mLetterPostion = position;
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
            mDetectHandler.post(new Runnable() {
                @Override
                public void run() {
                    mJiuGongGePv.setImageBitmap(mCropBitmap);
                    mScoreTv.setText("一共:" + mItemCount + "个" + "\n" + "正确率" + mRightCount / mItemCount);
                    mProgressDialog.dismiss();
                    mItemCount = 0;
                    mRightCount = 0;
                }
            });
        }
    };


    private void detectBitmaps() {
        mBitmaps = new Bitmap[mCellHeight][mCellWidth];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                mItemCount++;
                int xx = x * mCellWidth;
                int yy = y * mCellHeight;
                mBitmaps[x][y] = Bitmap.createBitmap(mCropBitmap, xx + mCellWidth / 7, yy + mCellHeight / 7,
                        (mCellWidth / 7) * 5, (mCellHeight / 7) * 5);
                String result = "";
                Bitmap bitmap = mBitmaps[x][y];
                result = detectText(convertGray(bitmap)).toLowerCase().trim();
                if (!mLetterSwitch.toLowerCase().equals(result)) { //如果识别错误
                    SdCardUtils.saveBitmapToSD(ImageUtil.getBinaryzationBitmap(bitmap), result, mLetterPostion);
                    drawErrorOnBitmap(mCropBitmap, xx, yy, mCellWidth, mCellHeight);
                } else if (mLetterSwitch.toLowerCase().equals(result)) {
                    mRightCount++;
                }
                Log.d("detectBitmaps", "当前选择:" + mLetterSwitch + "\t识别结果:" + result + "\t判定" + (mLetterSwitch.toLowerCase().equals(result)));

            }
        }
    }

    private String detectText(Bitmap bitmap) {

        bitmap = ImageUtil.getBinaryzationBitmap(bitmap); //二值化
        float[] pb = ImageUtil.getPixelData(bitmap);
        mInferenceInterface.feed(input_node, pb, input_shape);
        mInferenceInterface.run(new String[]{output_node});
        float[] data = new float[62];
        mInferenceInterface.fetch(output_node, data);

        return DetectUtil.extractText(data);
    }


    public void drawErrorOnBitmap(Bitmap bitmap, int left, int top, int width, int height) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawLine(left, top, left + width, top + height, mPaint);
        canvas.drawLine(left + width, top, left, top + height, mPaint);
        canvas.save();
    }
}
