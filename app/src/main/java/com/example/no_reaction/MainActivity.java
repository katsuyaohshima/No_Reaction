package com.example.no_reaction;
import androidx.appcompat.app.AppCompatActivity;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;




import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
//import androidx.camera.core.ImageAnalysis;
//import androidx.camera.core.ImageAnalysisConfig;
//import androidx.camera.core.ImageCapture;
//import androidx.camera.core.ImageCaptureConfig;
//import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
//import androidx.camera.core.PreviewConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.solutions.facemesh.FaceMesh;
import com.google.mediapipe.solutions.facemesh.FaceMeshOptions;
import com.google.mediapipe.solutions.facemesh.FaceMeshResult;

import java.io.File;
import java.util.concurrent.ExecutionException;
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
    private PreviewView previewView;
    private Button captureButton;
    private ImageView testview;
    private TextView text;
    private TextView text2;

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
        this.previewView = findViewById(R.id.viewFinder);
        this.textureView = findViewById(R.id.texture_view);
        this.captureButton = findViewById(R.id.capture_button);
        this.testview = findViewById(R.id.testview);
        this.text = findViewById(R.id.textView);
        this.text2 = findViewById(R.id.textView2);


        captureButton.setVisibility(View.GONE);

        captureButton.setOnClickListener(this::capture);

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
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview
                PreviewView previewView = findViewById(R.id.viewFinder);
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Select back camera as a default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);
            } catch (ExecutionException | InterruptedException e) {
                //Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }, ContextCompat.getMainExecutor(this));
    }
    /*
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

     */

    Handler handler= new Handler();
    Boolean captureWkup = false;
    Bitmap nowView ;
    private FaceMesh facemesh;
    private long prevtime =0;
    int RING_MAX_INDEX = 30;
    int RING_DUMMY_INDEX = 999;
    int RING_NOW_INDEX = RING_DUMMY_INDEX;
    float RING[] = new float[RING_MAX_INDEX];
    float RING_AVE=0;
    float TH_UNADUKI_RANGE = (float) 0.1;
    float TH_UNADUKI_FRM = 3000;
    int UNADUKI_COUNT=0;


    public void capture(View v){

        // 多重起動防止
        if (captureWkup == true){return;}
        captureWkup = true;


        facemesh = new FaceMesh(
                this,
                FaceMeshOptions.builder()
                        .setStaticImageMode(true)
                        .setRefineLandmarks(true)
                        .setRunOnGpu(true)
                        .build());

        facemesh.setResultListener(
                faceMeshResult -> {
                    try{
                        NormalizedLandmark noseLandmark = faceMeshResult.multiFaceLandmarks().get(0).getLandmarkList().get(1);
                        //Log.d("AAA", String.valueOf(noseLandmark));
                        float Face_y = noseLandmark.getY();


                        //RINGBUFFへ保存
                        //初期化
                        if(RING_NOW_INDEX == RING_DUMMY_INDEX){
                            RING_NOW_INDEX = 0;
                            for(int i = 0;i<RING_MAX_INDEX;i++){
                                RING[i]=Face_y;
                            }
                        }
                        //ためる
                        RING[RING_NOW_INDEX] = Face_y;
                        RING_NOW_INDEX ++;
                        if(RING_NOW_INDEX == RING_MAX_INDEX){
                            RING_NOW_INDEX = 0;
                        }

                        //平均化
                        RING_AVE = 0;
                        for(int i = 0;i<RING_MAX_INDEX;i++){
                            RING_AVE += RING[i];
                        }
                        RING_AVE /= RING_MAX_INDEX;

                        //UNADUKI
                        if(Face_y - RING_AVE > TH_UNADUKI_RANGE){
                            if (prevtime == 0) {
                                prevtime = System.currentTimeMillis();
                            }
                        }else{
                            if(prevtime != 0){
                                if((int) (System.currentTimeMillis()-prevtime)<TH_UNADUKI_FRM){
                                    prevtime = 0;
                                    UNADUKI_COUNT++;
                                    text2.setText(String.valueOf(UNADUKI_COUNT));
                                }else{
                                    prevtime = 0;
                                }
                            }
                        }


                        text.setText(String.valueOf(Face_y)+" "+String.valueOf(RING_AVE));



                        //if (prevtime == 0) {
                        //    prevtime = System.currentTimeMillis();
                        //}
                        //text2.setText(String.valueOf((int) (System.currentTimeMillis()-prevtime)));
                        //prevtime = System.currentTimeMillis();

                    }catch(Exception e) {
                        Log.d("AAA", "err");
                        //pass
                    }
                }
                );

        new Thread(new Runnable() {
            @Override
            public void run() {
                // マルチスレッドにしたい処理 ここから
                // 画処理をここに実装する。

                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        facemesh.send(nowView);
                    }catch (Exception e){}

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            nowView = previewView.getBitmap();
                        }
                    });
                }
                // マルチスレッドにしたい処理 ここまで
            }
        }).start();

    }

    public void startAnimation(View v) {
        findViewById(R.id.titleButton).setVisibility(View.GONE);
        findViewById(R.id.titleLabel).setVisibility(View.GONE);
        fadeout(findViewById(R.id.titleImage));
        findViewById(R.id.capture_button).setVisibility(View.VISIBLE);
    }

    public  void fadeout(View target){
        PropertyValuesHolder ani1 = PropertyValuesHolder.ofFloat( "scaleX", 1f, 0f );
        PropertyValuesHolder ani2 = PropertyValuesHolder.ofFloat( "alpha", 1f, 0f );
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(target,ani1,ani2);
        objectAnimator.setDuration(1000);
        objectAnimator.setRepeatCount(0);//0で1回2
        objectAnimator.start();
    }

}