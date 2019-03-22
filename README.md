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

## 処理
### onCreate
- 「Android端末がBLEをサポートしてるかの確認」と「Bluetoothアダプタの取得」

### onResume
- 初回表示時やポーズからの復帰時にrequestBluetoothFeatureを呼ぶ

### requestBluetoothFeature
- mBluetoothAdapterで有効になっているかを確認
- 無効だった場合，有効化を要求する

## 参考
- [BLE通信ソフトを作る ( Android Studio 2.3.3 + RN4020 )](https://www.hiramine.com/programming/blecommunicator/index.html)
