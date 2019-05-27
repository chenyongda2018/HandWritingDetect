package example.chen.com.detecthandwriting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import example.chen.com.detecthandwriting.view.CameraSurfaceView;
import example.chen.com.detecthandwriting.view.CameraTopRectView;

/**
 * 拍照打分的相机页面
 */
public class MCameraActivity extends AppCompatActivity implements View.OnClickListener, CameraTopRectView.IAutoFocus {


    private Button mTakePhotoBtn; //拍照按钮
    private CameraSurfaceView mCameraSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_view);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.cameraSurfaceView);
        mTakePhotoBtn = (Button) findViewById(R.id.takePic);

        mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraSurfaceView.takePicture();
                mCameraSurfaceView.setOnPathChangedListener(new CameraSurfaceView.OnPathChangedListener() {
                    @Override
                    public void onValueChange(String path) {
                        Intent intent = new Intent(MCameraActivity.this,CheckActivity.class);
                        intent.putExtra("image_path", path);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.takePic:
                mCameraSurfaceView.takePicture();
                break;
            default:
                break;
        }
    }


    @Override
    public void autoFocus() {
        mCameraSurfaceView.setAutoFocus();
    }
}