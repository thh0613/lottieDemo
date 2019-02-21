package com.taohuahua.lottiedemo;

import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.airbnb.lottie.ImageAssetDelegate;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.LottieListener;
import com.airbnb.lottie.LottieTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{
    private static final String WRITE_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String READ_STORAGE_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE";


    private static String[] PERMISSIONS_STORAGE = {WRITE_STORAGE_PERMISSION, READ_STORAGE_PERMISSION};

    private LottieAnimationView mLottieAnimationView;
    private Button mFromAssets;
    private Button mFromSdCard;
    private Button mFromNet;
    private Button mDynamicLoadImg;
    private Button mPauseLottie;
    private Button mResumeLottie;
    private Button mLottieProgress;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        initViewListener();
    }

    private void findView() {
        mLottieAnimationView = findViewById(R.id.animation_view);
        mFromAssets = findViewById(R.id.file_from_assets);
        mFromSdCard = findViewById(R.id.file_from_sdcard);
        mFromNet = findViewById(R.id.file_from_net);
        mDynamicLoadImg = findViewById(R.id.dynamic_load_image);
        mPauseLottie = findViewById(R.id.pause_anim);
        mResumeLottie = findViewById(R.id.resume_anim);
        mLottieProgress = findViewById(R.id.progress_anim);
        mSeekBar = findViewById(R.id.seek_bar);

        mLottieAnimationView.setRepeatCount(ValueAnimator.INFINITE);
    }

    private void initViewListener() {
        mFromAssets.setOnClickListener(this);
        mFromSdCard.setOnClickListener(this);
        mFromNet.setOnClickListener(this);
        mDynamicLoadImg.setOnClickListener(this);
        mPauseLottie.setOnClickListener(this);
        mResumeLottie.setOnClickListener(this);
        mLottieProgress.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);

        mSeekBar.setMax(100);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.file_from_assets) {
            loadFromAssets();
        }  else if (v.getId() == R.id.file_from_sdcard) {
            loadFromSdcard();
        } else if (v.getId() == R.id.file_from_net) {
            loadFromNet();
        } else if (v.getId() == R.id.dynamic_load_image) {
            dynamicLoadImg();
        } else if (v.getId() == R.id.pause_anim) {
            pauseLottie();
        } else if (v.getId() == R.id.resume_anim) {
            resumeLottie();
        } else if (v.getId() == R.id.progress_anim) {
            getLottieProgress();
        }
    }

    private void loadFromAssets() {
        mLottieAnimationView.cancelAnimation();
        mLottieAnimationView.setAnimation("lottie/test_loy.json");
        mLottieAnimationView.playAnimation();
    }

    private void loadFromSdcard() {
        if (!checkHasStoragePermission()) {
            return;
        }

        mLottieAnimationView.cancelAnimation();
        final String path = Environment.getExternalStorageDirectory().getPath();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String lottiePath = path + File.separator + "test_loy.json";
                    File file = new File(lottiePath);
                    if (!file.exists()) {
                        return;
                    }

                    FileInputStream inputStream = new FileInputStream(file);
                    final LottieTask<LottieComposition> lottieTask =
                            LottieCompositionFactory.fromJsonInputStream(inputStream, null);
                    lottieTask.addListener(new LottieListener<LottieComposition>() {
                        @Override
                        public void onResult(LottieComposition result) {
                            mLottieAnimationView.setComposition(result);
                            mLottieAnimationView.playAnimation();
                        }
                    });

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loadFromNet() {
        mLottieAnimationView.setAnimationFromUrl("https://raw.githubusercontent.com/thh0613/markdownres/master/test_loy.json");
        mLottieAnimationView.playAnimation();
    }

    private void dynamicLoadImg() {

        final String path = Environment.getExternalStorageDirectory().getPath();
        final File imageDir = new File(path, "images");


        mLottieAnimationView.setImageAssetDelegate(new ImageAssetDelegate() {
            @Override
            public Bitmap fetchBitmap(LottieImageAsset asset) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inScaled = true;
                opts.inDensity = 160;
                return BitmapFactory.decodeFile(imageDir.getAbsolutePath() + File.separator +
                        asset.getFileName(), opts);
            }
        });

        mLottieAnimationView.setAnimation("lottie/test_loy3.json");
        mLottieAnimationView.playAnimation();
    }

    private void pauseLottie() {
        mLottieAnimationView.pauseAnimation();
    }

    private void resumeLottie() {
        mLottieAnimationView.resumeAnimation();
    }

    private void getLottieProgress() {
        mLottieAnimationView.addAnimatorUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animValue = (float) animation.getAnimatedValue(); //range (0.0, 1.0)
                Log.i("thh", "animValue: " + animValue + " " + Thread.currentThread());
                mLottieProgress.setText("当前进度:  " + animValue * 100 + "%");
            }
        });
    }

    private boolean checkHasStoragePermission() {
        int permission = ActivityCompat.checkSelfPermission(this, WRITE_STORAGE_PERMISSION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,1);
            return false;
        }

        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mLottieAnimationView.setProgress(progress * 1.0f / 100);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
