package com.example.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

// https://dlucky.tistory.com/archive/201010
public class BluetoothActivity extends AppCompatActivity {
    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;

    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    TextView mTvBluetoothStatus;
    TextView mTvReceiveData;
    TextView mTvSendData;
    Button mBtnBluetoothOn;
    Button mBtnBluetoothOff;
    Button mBtnConnect;
    Button mBtnDisconnect;


    AcceptThread mThreadAcceptedBluetooth;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothServerSocket mBluetoothServerSocket;

    private Set<BluetoothDevice> mPairedDevices;
    private ArrayList<Object> mListPairedDevices;
    private String TAG = "tagsdd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mTvBluetoothStatus = findViewById(R.id.tvBluetoothStatus);
        mTvReceiveData = findViewById(R.id.tvReceiveData);
        mBtnBluetoothOn = findViewById(R.id.btnBluetoothOn);
        mBtnBluetoothOff = findViewById(R.id.btnBluetoothOff);
        mBtnConnect = findViewById(R.id.btnConnect);
        mBtnDisconnect = findViewById(R.id.btnDisconnect);

        mBtnBluetoothOn.setOnClickListener(v -> bluetoothOn());
        mBtnBluetoothOff.setOnClickListener(v -> bluetoothOff());

        mBtnConnect.setOnClickListener(v -> bluetoothOpen());
        mBtnDisconnect.setOnClickListener(v -> {
            if (mThreadAcceptedBluetooth != null) {
                mThreadAcceptedBluetooth.cancel();
            }
        });
    }

    void bluetoothOn() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
                mTvBluetoothStatus.setText("활성화");
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }

    void bluetoothOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            mTvBluetoothStatus.setText("비활성화");
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void bluetoothOpen() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "먼저 블루투스를 활성화주세요.", Toast.LENGTH_LONG).show();
        } else {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();
            /*
            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("장치 선택");

                mListPairedDevices = new ArrayList<>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
//                    mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, (dialog, item) -> {}); // connectSelectedDevice(items[item].toString())
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
            */

            final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        Log.d(TAG, "당첨: " + device.getName() + "\n" + device.getAddress());
                    }
                }
            };

//            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); // BroadcastReceiverIntentFilter
//            registerReceiver(mReceiver, filter);

            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            mTvReceiveData.setText("찾는 중...");
            startActivity(discoverableIntent);
            mTvReceiveData.setText("Discoverable!!");

            mThreadAcceptedBluetooth = new AcceptThread();
            mThreadAcceptedBluetooth.start();
        }
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            try {
                mmServerSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("me", MY_UUID_SECURE);
//                mmServerSocket= mBluetoothAdapter.listenUsingRfcommWithServiceRecord("me", MY_UUID_SECURE);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    if (mmServerSocket != null) {
                        socket = mmServerSocket.accept();
                        socket.getOutputStream().write("text".getBytes());
                    }
                } catch (IOException e) {
                    break;
                }

                if (socket != null) {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(500); //1초 동안 진동
                    // 넣으면 신호를 한번 밖에 받지 못하기 때문에 제거
//                    try {
//                        mmServerSocket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    break;
                } else if (mmServerSocket == null) {
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
                mmServerSocket = null;
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
