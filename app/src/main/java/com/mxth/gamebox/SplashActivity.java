package com.mxth.gamebox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by Administrator on 2017/5/3.
 */

public class SplashActivity extends Activity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = "http://sjbz.fd.zol-img.com.cn/t_s1080x1920c/g5/M00/00/00/ChMkJlfJTeOIVg8dAAUOuLjqbQkAAU9KQLvJuEABQ7Q371.jpg";
        setContentView(R.layout.activity_splash);
        ImageView iv = (ImageView) findViewById(R.id.iv);
        Glide.with(this).load(url).into(iv);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              startActivity(new Intent(SplashActivity.this,MainActivity.class));
                finish();
            }
        },1000);
    }
}
