package com.dvinc.testtaskwb;

import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity{

    private final static String TAG = "TEST APP LOG";
    private final static int COUNT_VALUE = 100;
    private final static int REQUEST_WRITE_PERMISSION = 1;
    private boolean isFirstThreadFinished = false;

    @BindView(R.id.simpleButton) Button simpleButton;
    @BindView(R.id.simpleProgressbar) ProgressBar simpleProgressBar;

    File filePath;
    File txtFile;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        filePath = this.getExternalFilesDir(null);
        txtFile = new File(filePath, "result.txt");
        handler = new Handler();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }
    }

    private Runnable countRunnable = () -> {
        for (int i = 1; i <= COUNT_VALUE; i++) {
            magicMethod(Thread.currentThread().getName(), String.valueOf(i));

            if(i == COUNT_VALUE) checkFinish(Thread.currentThread().getName());
        }
    };

    synchronized void magicMethod(String threadName, String value) {
        try {
            FileOutputStream stream = new FileOutputStream(txtFile, true);
            String stringData = String.format("%s did record with value  %s \n", threadName, value);
            stream.write(stringData.getBytes());
            stream.close();
        } catch (Exception e){
            Log.d(TAG, e.toString());
        }
    }

    synchronized void checkFinish(String threadName){
        if(isFirstThreadFinished){
            magicMethod(threadName, "FINISH");
            isFirstThreadFinished = false;
            handler.post(() -> {
                simpleButton.setText(getResources().getString(R.string.readyText_Button));
                simpleProgressBar.setVisibility(View.GONE);
            });
        } else{
            isFirstThreadFinished = true;
        }
    }

    @OnClick(R.id.simpleButton)
    void start(){
        handler.post(() -> {
            simpleButton.setText(getResources().getString(R.string.startText_Button));
            simpleProgressBar.setVisibility(View.VISIBLE);
        });
        new Thread(countRunnable, "Thread 1").start();
        new Thread(countRunnable, "Thread 2").start();
    }
}
