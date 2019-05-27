package example.chen.com.detecthandwriting.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import example.chen.com.detecthandwriting.util.DisplayUtil;

/**
 * 拍摄Crossword时相机页面的 矩形框
 */
public class CameraTopRectView extends View {
    private int panelWidth;
    private int panelHeight;

    private int viewWidth;
    private int viewHeight;

    public int rectWidth;
    public int rectHeight;

    private int rectTop;
    private int rectLeft;
    private int rectRight;
    private int rectBottom;

    private int lineLen;
    private int lineWidht;
    private static final int LINE_WIDTH = 5;
    private static final int TOP_BAR_HEIGHT = 50;
    private static final int BOTTOM_BTN_HEIGHT = 66;

//    private static final int TOP_BAR_HEIGHT = Constant.RECT_VIEW_TOP;
//    private static final int BOTTOM_BTN_HEIGHT = Constant.RECT_VIEW_BOTTOM;

    private static final int LEFT_PADDING = 10;
    private static final int RIGHT_PADDING = 10;
    private static final String TIPS = "请将游戏区域水平放入到方框中";

    private Paint linePaint;
    private Paint wordPaint;
    private Rect rect;
    private int baseline;

    public CameraTopRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        Activity activity = (Activity) context;

        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        panelWidth = wm.getDefaultDisplay().getWidth();//拿到屏幕的宽
        panelHeight = wm.getDefaultDisplay().getHeight();//拿到屏幕的高

        //高度不需要dp转换px,不然整体相机会向上移动一小节
//        viewHeight = panelHeight - (int) DisplayUtil.dp2px(activity,TOP_BAR_HEIGHT + BOTTOM_BTN_HEIGHT);

        viewHeight = panelHeight;
        //viewHeight,界面的高,viewWidth,界面的宽
        viewWidth = panelWidth;

        /*rectWidth = panelWidth
                - UnitUtils.getInstance(activity).dip2px(
                        LEFT_PADDING + RIGHT_PADDING);*/

        rectWidth = panelWidth - (int) DisplayUtil.dp2px(activity,LEFT_PADDING + RIGHT_PADDING);

        rectHeight = (int) (rectWidth * 54 / 54);
        // 相对于此view
        rectTop = (viewHeight - rectHeight) / 2;
        rectLeft = (viewWidth - rectWidth) / 2;
        rectBottom = rectTop + rectHeight;
        rectRight = rectLeft + rectWidth;

        lineLen = panelWidth / 8;

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.rgb(0xdd, 0x42, 0x2f));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(LINE_WIDTH);// 设置线宽
        linePaint.setAlpha(255);

        wordPaint = new Paint();
        wordPaint.setAntiAlias(true);
        wordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wordPaint.setStrokeWidth(3);
        wordPaint.setTextSize(35);

        rect = new Rect(rectLeft, rectTop - 80, rectRight, rectTop - 10);
        Paint.FontMetricsInt fontMetrics = wordPaint.getFontMetricsInt();
        baseline = rect.top + (rect.bottom - rect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        wordPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        wordPaint.setColor(Color.TRANSPARENT);
        canvas.drawRect(rect, wordPaint);

        //画蒙层
        wordPaint.setColor(0xa0000000);
        rect = new Rect(0, viewHeight/2+rectHeight/2, viewWidth, viewHeight);
        canvas.drawRect(rect, wordPaint);

        rect = new Rect(0, 0, viewWidth, viewHeight/2-rectHeight/2);
        canvas.drawRect(rect, wordPaint);

        rect = new Rect(0, viewHeight/2-rectHeight/2, (viewWidth-rectWidth)/2, viewHeight/2+rectHeight/2);
        canvas.drawRect(rect, wordPaint);

        rect = new Rect(viewWidth-(viewWidth-rectWidth)/2, viewHeight/2-rectHeight/2, viewWidth, viewHeight/2+rectHeight/2);
        canvas.drawRect(rect, wordPaint);


        //重制rect  并画文字  吧文字置于rect中间
        rect = new Rect(rectLeft, rectTop - 80, rectRight, rectTop - 10);
        wordPaint.setColor(Color.WHITE);
        canvas.drawText(TIPS, rect.centerX(), baseline, wordPaint);
        canvas.drawLine(rectLeft, rectTop, rectLeft + lineLen, rectTop,
                linePaint);
        canvas.drawLine(rectRight - lineLen, rectTop, rectRight, rectTop,
                linePaint);
        canvas.drawLine(rectLeft, rectTop, rectLeft, rectTop + lineLen,
                linePaint);
        canvas.drawLine(rectRight, rectTop, rectRight, rectTop + lineLen,
                linePaint);
        canvas.drawLine(rectLeft, rectBottom, rectLeft + lineLen, rectBottom,
                linePaint);
        canvas.drawLine(rectRight - lineLen, rectBottom, rectRight, rectBottom,
                linePaint);
        canvas.drawLine(rectLeft, rectBottom - lineLen, rectLeft, rectBottom,
                linePaint);
        canvas.drawLine(rectRight, rectBottom - lineLen, rectRight, rectBottom,
                linePaint);
    }

    public int getRectLeft() {
        return rectLeft;
    }

    public int getRectTop() {
        return rectTop;
    }

    public int getRectRight() {
        return rectRight;
    }

    public int getRectBottom() {
        return rectBottom;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    private IAutoFocus mIAutoFocus;
    /** 聚焦的回调接口 */
    public interface  IAutoFocus{
        void autoFocus();
    }

    public void setIAutoFocus(IAutoFocus mIAutoFocus) {
        this.mIAutoFocus = mIAutoFocus;
    }
}
