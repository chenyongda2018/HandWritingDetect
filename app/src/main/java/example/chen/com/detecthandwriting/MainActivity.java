package example.chen.com.detecthandwriting;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import example.chen.com.detecthandwriting.util.SdCardUtils;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    public static final int RC_CAMERA_AND_RECORD_AUDIO = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void requestPermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //判断有没有权限
        if (EasyPermissions.hasPermissions(this, perms)) {
            openCamera();
        } else {
            EasyPermissions.requestPermissions(this, "写上你需要用权限的理由, 是给用户看的", RC_CAMERA_AND_RECORD_AUDIO, perms);
        }
    }

    /**
     * 权限申请成功的回调
     *
     * @param requestCode 申请权限时的请求码
     * @param perms       申请成功的权限集合
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode != RC_CAMERA_AND_RECORD_AUDIO) {
            return;
        }
        for (int i = 0; i < perms.size(); i++) {
            if (perms.get(i).equals(Manifest.permission.CAMERA)) {
                Toast.makeText(this, "相机已授权", Toast.LENGTH_SHORT).show();
                openCamera();
            } else if (perms.get(i).equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "读写已授权", Toast.LENGTH_SHORT).show();
                SdCardUtils.createFileDir(SdCardUtils.FILE_DIR);
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
    }

    private void openCamera() {
        Intent intent = new Intent(this, MCameraActivity.class);
        startActivity(intent);
    }
}
