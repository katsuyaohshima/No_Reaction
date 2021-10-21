package com.example.no_reaction;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //タイトル画面用のアクティビティを表示する。
        findViewById(R.id.titleImage).setVisibility(View.VISIBLE);
        findViewById(R.id.titleLabel).setVisibility(View.VISIBLE);
        findViewById(R.id.titleButton).setVisibility(View.VISIBLE);
        findViewById(R.id.titleButton).setOnClickListener(this::startAnimation);
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