# BLEtest

BLEを使うために必要な諸々を記録しておきたい．
試した実機はASUS ZenPad 8.0 (Z380M)で，Androidのバージョンは5.0.2

## 設定
### 「BLEの機能を使用する宣言」および「BLEの機能を使用するためのパーミッションの宣言」
- AndroidManifest.xmlに追記する
```
<uses-feature
    android:name="android.hardware.bluetooth_le"
    android:required="true"/>
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
```

## 処理@MainActivity
### onCreate
- ボタン(connect, disconnect)メンバ変数代入，クリックリスナーへ登録
- 「Android端末がBLEをサポートしてるかの確認」と「Bluetoothアダプタの取得」

### onResume
- 初回表示時やポーズからの復帰時にrequestBluetoothFeatureを呼ぶ
- ボタンの状態を調整する

### onPause
- バックグラウンドに行くときに接続を終了する

### onDestroy
- activityの終了時はBluetoothGattクラスのコールバックを削除する

### requestBluetoothFeature
- mBluetoothAdapterで有効になっているかを確認
- 無効だった場合，有効化を要求する

### onOptionsItemSelected
- メニューバーにある虫眼鏡が選択されたらDeviceListActivityを呼び出す

### onClick
- implements View.OnClickListenerしたので
- 連打対策のためにボタンを無効にしてからconnect(), disconnect()を呼び出す

### connect
- mGattcallbackを登録する

### disconnect
- 切断して，disconnectのボタンをオフにする
- この関数ではmBluetoothGatt.disconnect()は使わない
- mBluetoothGatt.close()とnullの代入による解放を行う
- 「ユーザの意思による切断」と「接続範囲から外れた切断」を区別するため
- 「ユーザの意思による切断」では，mBluetoothGattオブジェクトを開放し，再度接続する場合はオブジェクトの構築から行う
- 「接続範囲から外れた切断」では，内部処理でmBluetoothGatt.disconnect()が実施される．切断時のコールバックでmBluetoothGatt.connect()を呼んでおくと，接続範囲に入った時自動的に再接続できる

### BluetoothGattCallback mGattcallback
- Bluetooth接続の状態が変わった時に呼び出される
- 接続されたらdisconnectのボタンを有効にする
- 上に書いたとおり「接続範囲から外れた切断」のときはmBluetoothGatt.connect()を呼んでおく

## 処理@DeviceListActivity
### onCreate
- ListViewの設定
- mHandlerの作成
- Bluetoothアダプタの取得

### onResume, onPause
- バックグラウンドに追いやられたらスキャンを止める
- 再開した場合（と初回表示時）はBluetoothを有効にして，スキャンを始める

### requestBluetoothFeature, onActivityResult
- bluetoothの有効化

### startScan, stopScan
- mHandlerを利用してSCAN_PERIOD後にstopScanが呼ばれるようにしたらscanを始める
- stopScanはmHandlerのRunnableを削除してからscanを止める

### onItemClick
- MainActivityに選択されたdeviceの名前とアドレスを渡して終了する

### onCreateOptionsMenu, onOptionsItemSelected
- scan, stop, progressの表示
- タップされたときに適切な処理の呼び出し

### static class DeviceListAdapter extends BaseAdapter
- scanして見つけたdeviceを保存するArrayList
- deviceの名前とアドレスを表示する

## 参考
- [BLE通信ソフトを作る ( Android Studio 2.3.3 + RN4020 )](https://www.hiramine.com/programming/blecommunicator/index.html)
