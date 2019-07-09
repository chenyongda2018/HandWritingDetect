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

import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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


    /**
     * 对图片进行二值化处理
     *
     * @param bm 原始图片
     * @return 二值化处理后的图片
     */

    public static Bitmap getBinaryzationBitmap(Bitmap bm) {
        Bitmap bitmap = null;
        // 获取图片的宽和高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 创建二值化图像
        bitmap = bm.copy(Bitmap.Config.ARGB_8888, true);
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
                if (gray <= 95) {
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
//    public static Bitmap getBinaryzationBitmap(Bitmap bm) {
//
//        Bitmap bitmap = null;
//        // 获取图片的宽和高
//        int width = bm.getWidth();
//        int height = bm.getHeight();
//        // 创建二值化图像
//        bitmap = bm.copy(Bitmap.Config.ARGB_8888 , true);
//        //获取图像中出现最多的像素值
//        int[] pixels = new int[226];
//        int threshold = 95;
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                // 得到当前的像素值
//                int pixel = bitmap.getPixel(i, j);
//                // 得到Alpha通道的值
//                int alpha = pixel & 0xFF000000;
//                // 得到Red的值
//                int red = (pixel & 0x00FF0000) >> 16;
//                // 得到Green的值
//                int green = (pixel & 0x0000FF00) >> 8;
//                // 得到Blue的值
//                int blue = pixel & 0x000000FF;
//                // 通过加权平均算法,计算出最佳像素值
//                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
//                    pixels[gray]++;
//            }
//        }
//        int max = pixels[0];
//        for (int i = 1; i <= 225; i++) {
//            if (max < pixels[i]) {
//                max = pixels[i];
//                threshold = i-20;
//            }
//        }
//        System.out.println(threshold);
//        // 遍历原始图像像素,并进行二值化处理
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                // 得到当前的像素值
//                int pixel = bitmap.getPixel(i, j);
//                // 得到Alpha通道的值
//                int alpha = pixel & 0xFF000000;
//                // 得到Red的值
//                int red = (pixel & 0x00FF0000) >> 16;
//                // 得到Green的值
//                int green = (pixel & 0x0000FF00) >> 8;
//                // 得到Blue的值
//                int blue = pixel & 0x000000FF;
//                // 通过加权平均算法,计算出最佳像素值
//                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
//                // 对图像设置黑白图
//                if (gray <= threshold) {
//                    gray = 0;
//                } else {
//                    gray = 255;
//                }
//                // 得到新的像素值
//                int newPiexl = alpha | (gray << 16) | (gray << 8) | gray;
//                // 赋予新图像的像素
//                bitmap.setPixel(i, j, newPiexl);
//            }
//        }
//        return bitmap;
//    }







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


}

