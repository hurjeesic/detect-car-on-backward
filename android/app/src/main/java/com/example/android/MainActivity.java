package com.example.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewById(R.id.vibration_Button).setOnClickListener(onClickListener);
        findViewById(R.id.bluetooth_button).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.vibration_Button:
                    makeVibration();
                    break;
                case  R.id.bluetooth_button:
                    startActivity(new Intent(getApplicationContext(), BluetoothActivity.class));
                    break;
            }
        }
    };

    private void makeVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(1000); //1초 동안 진동
    }

}
