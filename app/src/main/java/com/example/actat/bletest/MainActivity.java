package com.example.actat.bletest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_ENABLEBLUETOOTH = 1; // Bluetooth 有効化要求時の識別コード
    private static final int REQUEST_CONNECTDEVICE = 2; // デバイス接続要求時の識別コード

    private BluetoothAdapter mBluetoothAdapter;
    private String mDeviceAddress = ""; // デバイスアドレス
    private BluetoothGatt mBluetoothGatt = null; // gattサービスの検索，キャラスタリスティックの読み書き

    private Button mButtonConnect;
    private Button mButtonDisconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // buttons
        mButtonConnect = (Button)findViewById(R.id.button_connect);
        mButtonConnect.setOnClickListener(this);
        mButtonDisconnect = (Button)findViewById(R.id.button_disconnect);
        mButtonDisconnect.setOnClickListener(this);

        // Android端末がBLEをサポートしてるかの確認
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_is_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bluetoothアダプタの取得
        BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            // 端末がBluetoothをサポートしていない
            Toast.makeText(this, R.string.bluetooth_is_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestBluetoothFeature();

        mButtonConnect.setEnabled(false);
        mButtonDisconnect.setEnabled(false);
        if (!mDeviceAddress.equals("")) {
            mButtonConnect.setEnabled(true);
        }
        mButtonConnect.callOnClick();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    private void requestBluetoothFeature() {
        if (mBluetoothAdapter.isEnabled()) {
            return;
        }
        // 有効化要求を行う
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLEBLUETOOTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLEBLUETOOTH:
                if (Activity.RESULT_CANCELED == resultCode) {
                    // 有効化されなかった
                    Toast.makeText(this, R.string.bluetooth_is_not_working, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                break;
            case REQUEST_CONNECTDEVICE:
                String strDeviceName;
                if (resultCode == Activity.RESULT_OK) {
                    // DeviceListActivityから情報を取得する
                    strDeviceName = data.getStringExtra(DeviceListActivity.EXTRAS_DEVICE_NAME);
                    mDeviceAddress = data.getStringExtra(DeviceListActivity.EXTRAS_DEVICE_ADDRESS);
                } else {
                    strDeviceName = "";
                    mDeviceAddress = "";
                }
                ((TextView)findViewById(R.id.textview_devicename)).setText(strDeviceName);
                ((TextView)findViewById(R.id.textview_deviceaddress)).setText(mDeviceAddress);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // オプションメニュー作成時の処理
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.activity_main, menu );
        return true;
    }

    // オプションメニューのアイテム選択時の処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menuitem_search:
                Intent devicelistactivityIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(devicelistactivityIntent, REQUEST_CONNECTDEVICE);
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mButtonConnect.getId()) {
            mButtonConnect.setEnabled(false);
            connect();
            return;
        }
        if (v.getId() == mButtonDisconnect.getId()) {
            mButtonDisconnect.setEnabled(false);
            disconnect();
            return;
        }
    }

    private void connect() {
        if (mDeviceAddress.equals("")) {
            return;
        }
        if (mBluetoothGatt != null) {
            // already connected
            return;
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        mBluetoothGatt = device.connectGatt(this, false, mGattcallback);
    }

    private void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        // mBluetoothGatt.disconnect()は使わない
        // mBluetoothGatt.close()とnullの代入による解放を行う
        // 「ユーザの意思による切断」と「接続範囲から外れた切断」を区別するため
        // 「ユーザの意思による切断」では，mBluetoothGattオブジェクトを開放し，再度接続する場合はオブジェクトの構築から行う
        // 「接続範囲から外れた切断」では，内部処理でmBluetoothGatt.disconnect()が実施される．切断時のコールバックでmBluetoothGatt.connect()を呼んでおくと，接続範囲に入った時自動的に再接続できる
        mBluetoothGatt.close();
        mBluetoothGatt = null;

        mButtonConnect.setEnabled(true);
        mButtonDisconnect.setEnabled(false);
    }

    private final BluetoothGattCallback mGattcallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mButtonDisconnect.setEnabled(true);
                    }
                });
                return;
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // 接続範囲から外れた切断が起こった
                mBluetoothGatt.connect();
                return;
            }
        }
    };
}
