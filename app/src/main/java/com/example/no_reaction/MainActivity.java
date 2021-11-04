package com.example.no_reaction;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;




import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.Executors;



//memo
// cameraX参考
// https://note.com/npaka/n/n4d02e2de2ac9


public class MainActivity extends AppCompatActivity {

    // 定数
    private final int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA};

    // UI
    private TextureView textureView;
    private Button captureButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //タイトル画面用のアクティビティを表示する。
        findViewById(R.id.titleImage).setVisibility(View.VISIBLE);
        findViewById(R.id.titleLabel).setVisibility(View.VISIBLE);
        findViewById(R.id.titleButton).setVisibility(View.VISIBLE);
        findViewById(R.id.titleButton).setOnClickListener(this::startAnimation);

        // UI
        this.textureView = findViewById(R.id.texture_view);
        this.captureButton = findViewById(R.id.capture_button);

        // パーミッションのチェック
        if (allPermissionsGranted()) {
            this.textureView.post(() -> startCamera());
        } else {
            ActivityCompat.requestPermissions(this,
                    REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    // パーミッション許可のリクエスト結果の取得
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "ユーザーから権限が許可されていません。",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // 全てのパーミッション許可
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // カメラの開始
    private void startCamera() {
        // プレビューの表示
        PreviewConfig pConfig = new PreviewConfig.Builder().build();
        Preview preview = new Preview(pConfig);
        preview.setOnPreviewOutputUpdateListener(
                output -> {
                    // SurfaceTextureの更新
                    ViewGroup parent = (ViewGroup)this.textureView.getParent();
                    parent.removeView(this.textureView);
                    parent.addView(this.textureView, 0);

                    // SurfaceTextureをTextureViewに指定
                    this.textureView.setSurfaceTexture(output.getSurfaceTexture());

                    // TextureViewのサイズの調整
                    int w = output.getTextureSize().getWidth();
                    int h = output.getTextureSize().getHeight();
                    int degree = output.getRotationDegrees();
                    if (degree == 90 || degree == 270) {
                        w = output.getTextureSize().getHeight();
                        h = output.getTextureSize().getWidth();
                    }
                    h = h * textureView.getWidth() / w;
                    w = textureView.getWidth();
                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(w,h);
                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                    textureView.setLayoutParams(params);;
                });

        // カメラのライフサイクルのバインド
        CameraX.bindToLifecycle(this, preview);
    }

    public void startAnimation(View v) {
        findViewById(R.id.titleButton).setVisibility(View.GONE);
        findViewById(R.id.titleLabel).setVisibility(View.GONE);
        fadeout(findViewById(R.id.titleImage));
    }

    public  void fadeout(View target){
        PropertyValuesHolder ani1 = PropertyValuesHolder.ofFloat( "scaleX", 1f, 0f );
        PropertyValuesHolder ani2 = PropertyValuesHolder.ofFloat( "alpha", 1f, 0f );
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(target,ani1,ani2);
        objectAnimator.setDuration(2000);
        objectAnimator.setRepeatCount(0);//0で1回2
        objectAnimator.start();
    }
}