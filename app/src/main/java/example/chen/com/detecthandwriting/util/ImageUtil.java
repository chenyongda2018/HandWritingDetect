package example.chen.com.detecthandwriting.util;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


/**
 * 照片处理的工具类,用于获取图片路径
 *
 * @author cyd
 */
public class ImageUtil {

    /**
     * 存放拍摄图片的文件夹
     */
    private static final String FILES_NAME = "/MyPhoto";
    /**
     * 获取的时间格式
     */
    public static final String TIME_STYLE = "yyyyMMddHHmmss";
    /**
     * 图片种类
     */
    public static final String IMAGE_TYPE = ".png";

    private ImageUtil() {
    }


    @TargetApi(19)
    public static String getImagePathOnData(Intent data, Activity activity) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(activity, uri)) {
            //如果是document类型的uri，则通过document id 处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                //解析出数字格式的id
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, activity.getContentResolver());
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse
                        ("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null, activity.getContentResolver());
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的uri，则用普通方式处理
            imagePath = getImagePath(uri, null, activity.getContentResolver());
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的uri,直接获取图片路径
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    public static String getImagePath(Uri uri, String selection, ContentResolver contentResolver) {
        String path = null;
        //通过uri 和 selection 获取真实的图片路径
        Cursor cursor = contentResolver.query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images
                        .Media.DATA));

            }
            cursor.close();
        }
        return path;
    }

    public static Bitmap bitmap2Gray(Bitmap bmSrc) {
        //得到图片的长和宽
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();

        //创建目标图像
        Bitmap grayBitmap = null;
        grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        //创建画布
        Canvas c = new Canvas(grayBitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(colorMatrixColorFilter);
        c.drawBitmap(bmSrc, 0, 0, paint);
        return grayBitmap;

    }


    /**
     * 获取手机可存储路径
     *
     * @param context 上下文
     * @return 手机可存储路径
     */
    private static String getPhoneRootPath(Context context) {
        // 是否有SD卡
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || !Environment.isExternalStorageRemovable()) {
            // 获取SD卡根目录
            return context.getExternalCacheDir().getPath();
        } else {
            // 获取apk包下的缓存路径
            return context.getCacheDir().getPath();
        }
    }


    /**
     * 使用当前系统时间作为上传图片的名称
     *
     * @return 存储的根路径+图片名称
     */
    public static String getPhotoFileName(Context context) {
        File file = new File(getPhoneRootPath(context) + FILES_NAME);
        // 判断文件是否已经存在，不存在则创建
        if (!file.exists()) {
            file.mkdirs();
        }
        // 设置图片文件名称
        SimpleDateFormat format = new SimpleDateFormat(TIME_STYLE, Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        String time = format.format(date);
        String photoName = "/" + time + IMAGE_TYPE;
        return file + photoName;
    }


    /**
     * 保存Bitmap图片在SD卡中
     * 如果没有SD卡则存在手机中
     *
     * @param mbitmap 需要保存的Bitmap图片
     * @return 保存成功时返回图片的路径，失败时返回null
     */
    public static String savePhotoToSD(Bitmap mbitmap, Context context) {
        FileOutputStream outStream = null;
        String fileName = getPhotoFileName(context);
        try {
            outStream = new FileOutputStream(fileName);
            // 把数据写入文件，100表示不压缩
            mbitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (outStream != null) {
                    // 记得要关闭流！
                    outStream.close();
                }
                if (mbitmap != null) {
                    mbitmap.recycle();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 把原图按1/10的比例压缩
     *
     * @param path 原图的路径
     * @return 压缩后的图片
     */
    public static Bitmap getCompressPhoto(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        // 图片的大小设置为原来的十分之一
        options.inSampleSize = 4;
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        options = null;
        return bmp;

    }

    /**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


    /**
     * 旋转图片
     *
     * @param angle  被旋转角度
     * @param bitmap 图片对象
     * @return 旋转后的图片
     */

    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        Bitmap returnBm = null;
        //根据旋转角度，生成旋转矩阵.
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        try {
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        if (returnBm == null) {
            returnBm = bitmap;
        }

        if (bitmap != returnBm) {
            bitmap.recycle();
        }

        return returnBm;
    }

    /**
     * 处理旋转后的图片
     *
     * @param originalPath 原图路径
     * @param context      上下文
     * @return 返回修复完毕后的图片路径
     */
    public static String amendRotatePhoto(String originalPath, Context context) {

        // 取得图片旋转角度
        int angle = readPictureDegree(originalPath);

        // 把原图压缩后得到Bitmap对象
        Bitmap bmp = getCompressPhoto(originalPath);

        // 修复图片被旋转的角度
        Bitmap bitmap = rotaingImageView(angle, bmp);

        // 保存修复后的图片并返回保存后的图片路径
        return savePhotoToSD(bitmap, context);
    }

    /**
     * 该函数实现对图像进行二值化处理
     *
     * @param graymap
     * @return
     */
    public static Bitmap getGray2Binary(Bitmap graymap) {
        //得到图形的宽度和长度
        int width = graymap.getWidth();
        int height = graymap.getHeight();
        //创建二值化图像
        Bitmap binarymap = null;
        binarymap = graymap.copy(Bitmap.Config.ARGB_8888, true);
        //依次循环，对图像的像素进行处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //得到当前像素的值
                int col = binarymap.getPixel(i, j);
                //得到alpha通道的值
                int alpha = col & 0xFF000000;
                //得到图像的像素RGB的值
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                //对图像进行二值化处理
                if (gray <= 95) {
                    gray = 0;
                } else {
                    gray = 255;
                }
                // 新的ARGB
                int newColor = alpha | (gray << 16) | (gray << 8) | gray;
                //设置新图像的当前像素值
                binarymap.setPixel(i, j, newColor);
            }
        }
        return binarymap;
    }


    public static Bitmap adaptiveThreshold(Bitmap grayBitmap) {
        Mat src = new Mat();
        Utils.bitmapToMat(grayBitmap, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);//灰度化

        Mat dst = new Mat();
        Imgproc.adaptiveThreshold(src, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, -2);

        Bitmap thredsBitmap = Bitmap.createBitmap(grayBitmap);
        Utils.matToBitmap(dst, thredsBitmap);
        return thredsBitmap;
    }


    public static Bitmap getGrayByOpenCV(Bitmap bitmap) {
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);//灰度化
        Utils.matToBitmap(src, bitmap);
        return bitmap;
    }

    /**
     * 灰度化处理
     *
     * @param bitmap3
     * @return
     */
    public static Bitmap convert2Gray(Bitmap bitmap3) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);

        Paint paint = new Paint();
        paint.setColorFilter(filter);
        Bitmap result = Bitmap.createBitmap(bitmap3.getWidth(), bitmap3.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        canvas.drawBitmap(bitmap3, 0, 0, paint);
        return result;
    }


    /**
     * 将图片归一化处理
     */
    public static float[] bitmapToFloatArray(Bitmap bitmap, float rx, float ry) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        float scaleWidth = rx / width;
        float scaleHeight = ry / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        height = bitmap.getHeight();
        width = bitmap.getWidth();
        float[] result = new float[width * height];
        int k = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int argb = bitmap.getPixel(col, row);
                int r = Color.red(argb);
                int g = Color.green(argb);
                int b = Color.blue(argb);

                assert (r == g && g == b);
                result[k++] = r / 255.0f;

            }
        }
        return result;
    }

    /**
     * Get 28x28 pixel data for tensorflow input.
     */
    public static float[] getPixelData(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        float scaleWidth = 28.0f / width;
        float scaleHeight = 28.0f / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        width = bitmap.getWidth();
        height = bitmap.getHeight();


        // Get 28x28 pixel data from bitmap
        int[] pixels = new int[width * height];


        int[] iarray = new int[784];
        bitmap.getPixels(iarray, 0, width, 0, 0, width, height);
        float[] farray = new float[784];
        for (int i = 0; i < 784; i++) {
            if (((float) iarray[i] / -16777216.0f > 0.001f)) {
                farray[i] = ((float) iarray[i]) / -16777216.0f;
            } else {
                farray[i] = 0.0f;
            }

        }
        String me = "";
        for (int i = 0; i < 784; i++) {

            me = me + farray[i] + ',';
        }
        Log.d("im", me);
        return farray;
    }


    ///**
    // * 对图片进行二值化处理
    // *
    // * @param bm 原始图片
    // * @return 二值化处理后的图片
    // */
    //
    //public static Bitmap getBinaryzationBitmap(Bitmap bm) {
    //    Bitmap bitmap = null;
    //    // 获取图片的宽和高
    //    int width = bm.getWidth();
    //    int height = bm.getHeight();
    //    // 创建二值化图像
    //    bitmap = bm.copy(Bitmap.Config.ARGB_8888, true);
    //    // 遍历原始图像像素,并进行二值化处理
    //    for (int i = 0; i < width; i++) {
    //        for (int j = 0; j < height; j++) {
    //            // 得到当前的像素值
    //            int pixel = bitmap.getPixel(i, j);
    //            // 得到Alpha通道的值
    //            int alpha = pixel & 0xFF000000;
    //            // 得到Red的值
    //            int red = (pixel & 0x00FF0000) >> 16;
    //            // 得到Green的值
    //            int green = (pixel & 0x0000FF00) >> 8;
    //            // 得到Blue的值
    //            int blue = pixel & 0x000000FF;
    //            // 通过加权平均算法,计算出最佳像素值
    //            int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
    //            // 对图像设置黑白图
    //            if (gray <= 95) {
    //                gray = 0;
    //            } else {
    //                gray = 255;
    //            }
    //            // 得到新的像素值
    //            int newPiexl = alpha | (gray << 16) | (gray << 8) | gray;
    //            // 赋予新图像的像素
    //            bitmap.setPixel(i, j, newPiexl);
    //        }
    //    }
    //    return bitmap;
    //}
    public static Bitmap getBinaryzationBitmap(Bitmap bm) {

        Bitmap bitmap = null;
        // 获取图片的宽和高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 创建二值化图像
        bitmap = bm.copy(Bitmap.Config.ARGB_8888, true);
        //获取图像中出现最多的像素值
        int[] pixels = new int[226];
        int threshold = 95;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // 得到当前的像素值
                int pixel = bitmap.getPixel(i, j);
                // 得到Alpha通道的值
                int alpha = pixel & 0xFF000000;
                // 得到Red的值
                int red = (pixel & 0x00FF0000) >> 16;
                // 得到Green的值
                int green = (pixel & 0x0000FF00) >> 8;
                // 得到Blue的值
                int blue = pixel & 0x000000FF;
                // 通过加权平均算法,计算出最佳像素值
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                pixels[gray]++;
            }
        }
        int max = pixels[0];
        for (int i = 1; i <= 225; i++) {
            if (max < pixels[i]) {
                max = pixels[i];
                threshold = i - 20;
            }
        }
        System.out.println(threshold);
        // 遍历原始图像像素,并进行二值化处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // 得到当前的像素值
                int pixel = bitmap.getPixel(i, j);
                // 得到Alpha通道的值
                int alpha = pixel & 0xFF000000;
                // 得到Red的值
                int red = (pixel & 0x00FF0000) >> 16;
                // 得到Green的值
                int green = (pixel & 0x0000FF00) >> 8;
                // 得到Blue的值
                int blue = pixel & 0x000000FF;
                // 通过加权平均算法,计算出最佳像素值
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                // 对图像设置黑白图
                if (gray <= threshold) {
                    gray = 0;
                } else {
                    gray = 255;
                }
                // 得到新的像素值
                int newPiexl = alpha | (gray << 16) | (gray << 8) | gray;
                // 赋予新图像的像素
                bitmap.setPixel(i, j, newPiexl);
            }
        }
        return bitmap;
    }


    public static void binaryMatZation(Mat mat) {
        int BLACK = 0;
        int WHITE = 255;
        int ucThre = 0, ucThre_new = 127;
        int nBack_count, nData_count;
        int nBack_sum, nData_sum;
        int nValue;
        int i, j;

        int width = mat.width(), height = mat.height();
        //寻找最佳的阙值
        while (ucThre != ucThre_new) {
            nBack_sum = nData_sum = 0;
            nBack_count = nData_count = 0;

            for (j = 0; j < height; ++j) {
                for (i = 0; i < width; i++) {
                    nValue = (int) mat.get(j, i)[0];

                    if (nValue > ucThre_new) {
                        nBack_sum += nValue;
                        nBack_count++;
                    } else {
                        nData_sum += nValue;
                        nData_count++;
                    }
                }
            }

            nBack_sum = nBack_sum / nBack_count;
            nData_sum = nData_sum / nData_count;
            ucThre = ucThre_new;
            ucThre_new = (nBack_sum + nData_sum) / 2;
        }

        //二值化处理
        int nBlack = 0;
        int nWhite = 0;
        for (j = 0; j < height; ++j) {
            for (i = 0; i < width; ++i) {
                nValue = (int) mat.get(j, i)[0];
                if (nValue > ucThre_new) {
                    mat.put(j, i, WHITE);
                    nWhite++;
                } else {
                    mat.put(j, i, BLACK);
                    nBlack++;
                }
            }
        }

        // 确保白底黑字
        if (nBlack > nWhite) {
            for (j = 0; j < height; ++j) {
                for (i = 0; i < width; ++i) {
                    nValue = (int) (mat.get(j, i)[0]);
                    if (nValue == 0) {
                        mat.put(j, i, WHITE);
                    } else {
                        mat.put(j, i, BLACK);
                    }
                }
            }
        }
    }


    /**
     * 将一张bitmap 分成若干张小图
     *
     * @author cyd
     * @date 2019/7/13 20:24
     */
    public static LinkedList<Mat> getRectangleMats(Bitmap bitmap) {
        Mat source_image = new Mat();
        Utils.bitmapToMat(bitmap, source_image);

        //灰度处理
        Mat gray_image = new Mat(source_image.height(), source_image.width(), CvType.CV_8UC1);
        Imgproc.cvtColor(source_image, gray_image, Imgproc.COLOR_RGB2GRAY);

        //二值化
        Mat thresh_image = new Mat(source_image.height(), source_image.width(), CvType.CV_8UC1);
        // C 负数，取反色，超过阈值的为黑色，其他为白色
        Imgproc.adaptiveThreshold(gray_image, thresh_image, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, -2);
        //this.saveImage("out-table/1-thresh.png", thresh_image);

        //克隆一个 Mat，用于提取水平线
        Mat horizontal_image = thresh_image.clone();

        //克隆一个 Mat，用于提取垂直线
        Mat vertical_image = thresh_image.clone();

        /**
         * 求水平线
         * 1. 根据页面的列数（可以理解为宽度），将页面化成若干的扫描区域
         * 2. 根据扫描区域的宽度，创建一根水平线
         * 3. 通过腐蚀、膨胀，将满足条件的区域，用水平线勾画出来
         *
         * scale 越大，识别的线越多，因为，越大，页面划定的区域越小，在腐蚀后，多行文字会形成一个块，那么就会有一条线
         * 在识别表格时，我们可以理解线是从页面左边 到 页面右边的，那么划定的区域越小，满足的条件越少，线条也更准确
         */
        int scale = 10;
        int horizontalsize = horizontal_image.cols() / scale;
        // 为了获取横向的表格线，设置腐蚀和膨胀的操作区域为一个比较大的横向直条
        Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontalsize, 1));
        // 先腐蚀再膨胀 new Point(-1, -1) 以中心原点开始
        // iterations 最后一个参数，迭代次数，越多，线越多。在页面清晰的情况下1次即可。
        Imgproc.erode(horizontal_image, horizontal_image, horizontalStructure, new Point(-1, -1), 1);
        Imgproc.dilate(horizontal_image, horizontal_image, horizontalStructure, new Point(-1, -1), 1);
//        this.saveImage("out-table/2-horizontal.png", horizontal_image);

        // 求垂直线
        scale = 30;
        int verticalsize = vertical_image.rows() / scale;
        Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, verticalsize));
        Imgproc.erode(vertical_image, vertical_image, verticalStructure, new Point(-1, -1), 1);
        Imgproc.dilate(vertical_image, vertical_image, verticalStructure, new Point(-1, -1), 1);
//        this.saveImage("out-table/3-vertical.png", vertical_image);

        /**
         * 合并线条
         * 将垂直线，水平线合并为一张图
         */
        Mat mask_image = new Mat();
        Core.add(horizontal_image, vertical_image, mask_image);
//        this.saveImage("out-table/4-mask.png", mask_image);

        /**
         * 通过 bitwise_and 定位横线、垂直线交汇的点
         */
        Mat points_image = new Mat();
        Core.bitwise_and(horizontal_image, vertical_image, points_image);
//        saveImage("out-table/5-points.png", points_image);

        /**
         * 通过 findContours 找轮廓
         *
         * 第一个参数，是输入图像，图像的格式是8位单通道的图像，并且被解析为二值图像（即图中的所有非零像素之间都是相等的）。
         * 第二个参数，是一个 MatOfPoint 数组，在多数实际的操作中即是STL vectors的STL vector，这里将使用找到的轮廓的列表进行填充（即，这将是一个contours的vector,其中contours[i]表示一个特定的轮廓，这样，contours[i][j]将表示contour[i]的一个特定的端点）。
         * 第三个参数，hierarchy，这个参数可以指定，也可以不指定。如果指定的话，输出hierarchy，将会描述输出轮廓树的结构信息。0号元素表示下一个轮廓（同一层级）；1号元素表示前一个轮廓（同一层级）；2号元素表示第一个子轮廓（下一层级）；3号元素表示父轮廓（上一层级）
         * 第四个参数，轮廓的模式，将会告诉OpenCV你想用何种方式来对轮廓进行提取，有四个可选的值：
         *      CV_RETR_EXTERNAL （0）：表示只提取最外面的轮廓；
         *      CV_RETR_LIST （1）：表示提取所有轮廓并将其放入列表；
         *      CV_RETR_CCOMP （2）:表示提取所有轮廓并将组织成一个两层结构，其中顶层轮廓是外部轮廓，第二层轮廓是“洞”的轮廓；
         *      CV_RETR_TREE （3）：表示提取所有轮廓并组织成轮廓嵌套的完整层级结构。
         * 第五个参数，见识方法，即轮廓如何呈现的方法，有三种可选的方法：
         *      CV_CHAIN_APPROX_NONE （1）：将轮廓中的所有点的编码转换成点；
         *      CV_CHAIN_APPROX_SIMPLE （2）：压缩水平、垂直和对角直线段，仅保留它们的端点；
         *      CV_CHAIN_APPROX_TC89_L1  （3）or CV_CHAIN_APPROX_TC89_KCOS（4）：应用Teh-Chin链近似算法中的一种风格
         * 第六个参数，偏移，可选，如果是定，那么返回的轮廓中的所有点均作指定量的偏移
         */
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask_image, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));


        List<MatOfPoint> contours_poly = contours;
        Rect[] boundRect = new Rect[contours.size()];

        LinkedList<Mat> tables = new LinkedList<Mat>();

        //循环所有找到的轮廓-点
        for (int i = 0; i < contours.size(); i++) {

            MatOfPoint point = contours.get(i);
            MatOfPoint contours_poly_point = contours_poly.get(i);

            /*
             * 获取区域的面积
             * 第一个参数，InputArray contour：输入的点，一般是图像的轮廓点
             * 第二个参数，bool oriented = false:表示某一个方向上轮廓的的面积值，顺时针或者逆时针，一般选择默认false
             */
            double area = Imgproc.contourArea(contours.get(i));
            //如果小于某个值就忽略，代表是杂线不是表格
            Log.d("CheckActivity", "area of mat : " + area);
            if (area < 5) {
                continue;
            }

            /*
             * approxPolyDP 函数用来逼近区域成为一个形状，true值表示产生的区域为闭合区域。比如一个带点幅度的曲线，变成折线
             *
             * MatOfPoint2f curve：像素点的数组数据。
             * MatOfPoint2f approxCurve：输出像素点转换后数组数据。
             * double epsilon：判断点到相对应的line segment 的距离的阈值。（距离大于此阈值则舍弃，小于此阈值则保留，epsilon越小，折线的形状越“接近”曲线。）
             * bool closed：曲线是否闭合的标志位。
             */
            Imgproc.approxPolyDP(new MatOfPoint2f(point.toArray()), new MatOfPoint2f(contours_poly_point.toArray()), 3, true);

            //为将这片区域转化为矩形，此矩形包含输入的形状
            boundRect[i] = Imgproc.boundingRect(contours_poly.get(i));

            // 找到交汇处的的表区域对象
            Mat table_image = points_image.submat(boundRect[i]);

            List<MatOfPoint> table_contours = new ArrayList<MatOfPoint>();
            Mat joint_mat = new Mat();
            Imgproc.findContours(table_image, table_contours, joint_mat, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
            //从表格的特性看，如果这片区域的点数小于4，那就代表没有一个完整的表格，忽略掉
            if (table_contours.size() < 4) {
                continue;
            }
            //保存图片
            Mat m = source_image.submat(boundRect[i]).clone();
            Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);//灰度化
            tables.addFirst(m);
        }

        return tables;


    }

    /**
     * @author cyd
     * @date 2019/7/13 22:19
     */
    public static LinkedList<Bitmap> getRectangleBitmaps(Bitmap bitmap) {
        LinkedList<Mat> mats = getRectangleMats(bitmap);

        LinkedList<Bitmap> bitmaps = new LinkedList<>();

        for (int i = 0; i < mats.size(); i++) {
            Bitmap b = Bitmap.createBitmap(mats.get(i).cols(), mats.get(i).rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mats.get(i), b);
            bitmaps.addLast(b);
        }

        return bitmaps;
    }


}

