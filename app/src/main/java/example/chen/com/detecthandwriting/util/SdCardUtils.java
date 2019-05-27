package example.chen.com.detecthandwriting.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class SdCardUtils {
    //crossword为一个单词
    public static final String FILE_DIR = "/HandWriting";
    public static final String UPPER_LETTER_PREFIX = "/Letter_Upper_";
    public static final String LOWER_LETTER_PREFIX = "/Letter_Lower_";
    public static final String ERROR_LETTER = "/error_letter_data_set";
    public static String[] letterDirs = new String[52];

    //在sd卡下创建文件夹
    public static void createAppDir() {
        if (SdCardUtils.checkSdCard()) {
            SdCardUtils.createFileDir(SdCardUtils.FILE_DIR);
            SdCardUtils.createFileDir(SdCardUtils.FILE_DIR+ERROR_LETTER);
        }
    }

    public static boolean checkSdCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //sd卡可用
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取sd卡的文件路径
     *
     * @return
     */
    public static String getSdPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    }

    /**
     * 创建目录
     */
    public static void createFileDir(String fileDir) {
        String path = getSdPath() + fileDir;
        File filePath = new File(path);
        if (!filePath.exists()) {
            filePath.mkdirs();
            Log.d("createLetterDir", filePath.getAbsolutePath());
        }
    }

    /**
     * 按照字母创建文件夹
     */
    public static void createLetterDir(String[] letters) {

        for (int i = 0; i < letters.length; i++) {
            if (i % 2 == 0) {
                String dirName = FILE_DIR + LOWER_LETTER_PREFIX + letters[i];
                letterDirs[i] = dirName;
                createFileDir(dirName);
            } else {
                String dirName = FILE_DIR + UPPER_LETTER_PREFIX + letters[i];
                letterDirs[i] = dirName;
                createFileDir(dirName);
            }
        }
    }
    /**
     * 把Bitmap保存到本地
     * @param bitmap
     * @return
     */
    public static String saveBitmapToSD(Bitmap bitmap, String result,int position) {
        UUID id = UUID.randomUUID();
        String fileName = "";
        if(result == null || result.equals("")) {
            fileName = "unknown_"+id.toString();
        } else {
            fileName = "letter_"+result+"_"+id.toString();
        }
        File file = new File(Environment.getExternalStorageDirectory() +letterDirs[position],
                fileName+".jpg"); //放在每个字母对应的文件夹里
        return savePhotoToSD(bitmap,null , file.getAbsolutePath());
    }

    /**
     * 保存Bitmap图片在SD卡中
     * 如果没有SD卡则存在手机中
     *
     * @param mbitmap 需要保存的Bitmap图片
     * @return 保存成功时返回图片的路径，失败时返回null
     */
    public static String savePhotoToSD(Bitmap mbitmap, Context context, String fileName) {
        FileOutputStream outStream = null;
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

    public static final String DATA_SET_a_DIR = "/a";
    public static final String DATA_SET_A_DIR = "/A";

    public static final String DATA_SET_b_DIR = "/b";
    public static final String DATA_SET_B_DIR = "/B";

    public static final String DATA_SET_c_DIR = "/c";
    public static final String DATA_SET_C_DIR = "/C";

    public static final String DATA_SET_d_DIR = "/d";
    public static final String DATA_SET_D_DIR = "/D";

    public static final String DATA_SET_e_DIR = "/e";
    public static final String DATA_SET_E_DIR = "/E";

    public static final String DATA_SET_f_DIR = "/f";
    public static final String DATA_SET_F_DIR = "/F";

    public static final String DATA_SET_g_DIR = "/g";
    public static final String DATA_SET_G_DIR = "/G";

    public static final String DATA_SET_h_DIR = "/h";
    public static final String DATA_SET_H_DIR = "/H";

    public static final String DATA_SET_i_DIR = "/i";
    public static final String DATA_SET_I_DIR = "/I";

    public static final String DATA_SET_j_DIR = "/j";
    public static final String DATA_SET_J_DIR = "/J";

    public static final String DATA_SET_k_DIR = "/k";
    public static final String DATA_SET_K_DIR = "/K";

    public static final String DATA_SET_l_DIR = "/l";
    public static final String DATA_SET_L_DIR = "/L";

    public static final String DATA_SET_m_DIR = "/m";
    public static final String DATA_SET_M_DIR = "/M";

    public static final String DATA_SET_n_DIR = "/n";
    public static final String DATA_SET_N_DIR = "/N";

    public static final String DATA_SET_o_DIR = "/o";
    public static final String DATA_SET_O_DIR = "/O";

    public static final String DATA_SET_p_DIR = "/p";
    public static final String DATA_SET_P_DIR = "/P";

    public static final String DATA_SET_q_DIR = "/q";
    public static final String DATA_SET_Q_DIR = "/Q";

    public static final String DATA_SET_r_DIR = "/r";
    public static final String DATA_SET_R_DIR = "/R";

    public static final String DATA_SET_s_DIR = "/s";
    public static final String DATA_SET_S_DIR = "/S";

    public static final String DATA_SET_t_DIR = "/t";
    public static final String DATA_SET_T_DIR = "/T";

    public static final String DATA_SET_u_DIR = "/u";
    public static final String DATA_SET_U_DIR = "/U";

    public static final String DATA_SET_v_DIR = "/v";
    public static final String DATA_SET_V_DIR = "/V";

    public static final String DATA_SET_w_DIR = "/w";
    public static final String DATA_SET_W_DIR = "/W";

    public static final String DATA_SET_x_DIR = "/x";
    public static final String DATA_SET_X_DIR = "/X";

    public static final String DATA_SET_y_DIR = "/y";
    public static final String DATA_SET_Y_DIR = "/Y";

    public static final String DATA_SET_z_DIR = "/z";
    public static final String DATA_SET_Z_DIR = "/Z";


}
