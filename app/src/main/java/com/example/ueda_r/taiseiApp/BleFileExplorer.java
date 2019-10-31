package com.example.ueda_r.taiseiApp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ueda_r.osmdroidtest1022.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class BleFileExplorer extends Fragment{

    final int REQUEST_ENABLE_BT = 1;

    String SERVICE_UUID = "ac77f566-4882-4b4c-b3d5-39b4652e3fe3";//""11111809-0000-1000-8000-00805f9b34fb";
    String CHARACTERISTIC_UUID = "12221809-0000-1000-8000-00805f9b34fb";
    String CHARACTERISTIC_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";//"aa77f566-4882-4b4c-b3d5-39b4652e3fe3";//"22221809-0000-1000-8000-00805f9b34fb";// "33f1815f-c4dc-473e-a15a-6626d1ab68cf";

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private Boolean isAdvertising = false;

    private Boolean isConnected = false;

    private BluetoothGattServer btGattServer;
    private BluetoothGattServerCallback btGattServerCallback;
    private BluetoothGattService btGattService;
    private BluetoothGattCharacteristic btGattCharacteristic;
    private BluetoothGattDescriptor btGattDescriptor;
    private AdvertiseData scanResponseData;

    private int MTU_SIZE = 27;

    private BluetoothLeAdvertiser btAdvertiser;
    private AdvertiseCallback advertiseCallback;

    private MainActivity.Parameter parameter;

    private final byte RESULT_ACK = 0x06;
    private final byte RESULT_NAK = 0x15;

    private BluetoothDevice connectedDevice;
    private Handler handler;
    private Runnable r;

    private Fragment fragment;

    private boolean dataSend = false;

    public interface BleFileExplorerCallback {
        void showBfeProgressDialog(Boolean enable, String message);
        void showBfeToast(String message);
    }

    private BleFileExplorerCallback bfeCallback;

    public void setCallback(BleFileExplorerCallback callback) {
        bfeCallback = callback;
    }

    private void runNotificationHanlder(Boolean enable) {
        if (enable) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // TODO: ここで処理を実行する
                            handler = new Handler();
                            r = new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("BLE", "send notification");
                                    byte[] b = new byte[5];
                                    new Random().nextBytes(b);
                                    btGattCharacteristic.setValue(b);
                                    btGattServer.notifyCharacteristicChanged(connectedDevice, btGattCharacteristic, false);
                                    handler.postDelayed(this, 1000);
                                }
                            };
                            handler.post(r);
                        }
                    }, 3000);
                }
            });
        } else if ((!enable) && (handler != null)) {
            handler.removeCallbacks(r);
            //handler = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("BLE", "destoroy");
        if (isAdvertising) {
            stopAdvertise();
            bfeCallback.showBfeProgressDialog(false, "");
        }
        bfeCallback.showBfeProgressDialog(false,"");
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();     //インテントでカスタムParameterクラスのオブジェクトを渡す
        parameter = (MainActivity.Parameter)bundle.getParcelable("PARAMETER");

        btManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);

        if (btManager != null) {
            btAdapter = btManager.getAdapter();
            Log.i("BLE", "btManager is not null");
        } else {
            Log.i("BLE", "btManager is null");
        }
        initBle();

        bfeCallback.showBfeProgressDialog(true,"通信中...");

        fragment = this;

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ble_file_explorer, null);
        return v;
    }

    private void initBle() {
        Log.i("BLE", "initBLE()");

        if ((btAdapter != null) && (!isAdvertising)) {
            Log.i("BLE", "Init start");

            btAdapter.setName(parameter.getUserID() + "," + parameter.getGroupID());
            setBtGattServerCallback();
            Log.i("BLE", "server callback set");

            //サービスにキャラクタリスティックを追加してサービスをサーバに登録
            btGattServer = btManager.openGattServer(getContext(), btGattServerCallback);
            btGattService = new BluetoothGattService(UUID.fromString(SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY);

            btGattCharacteristic = new BluetoothGattCharacteristic(UUID.fromString(CHARACTERISTIC_UUID)
                    , (BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS)
                    , BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);

            btGattDescriptor = new BluetoothGattDescriptor(UUID.fromString(CHARACTERISTIC_CONFIG_UUID),
                    BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ);

            btGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

            btGattCharacteristic.addDescriptor(btGattDescriptor);

            btGattService.addCharacteristic(btGattCharacteristic);
            btGattServer.addService(btGattService);

            Log.i("BLE", "service set");

            if (btAdvertiser == null) {
                btAdvertiser = btAdapter.getBluetoothLeAdvertiser();
                Log.i("BLE", "get adapter");
            } else {
                Log.i("BLE", "advertiser not null");
            }
            setAdvertiseCallback();
            Log.i("BLE", "adv callback set");

            startAdvertise();
        }
    }

    private void setBtGattServerCallback() {
        btGattServerCallback = new BluetoothGattServerCallback() {
            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLE", "Service added :" + service.getUuid().toString());
                } else {
                    Log.d("BLE", "Service add failed");
                }
            }

            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                Log.i("BLE", "GattServer Status changed");
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i("BLE", "Connected");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "接続！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    connectedDevice = device;
                    isConnected = true;
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i("BLE", "Disconnected");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "切断！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    getFragmentManager().beginTransaction().remove(BleFileExplorer.this).commit();
                    isConnected = false;
                }
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                Log.i("BLE", "Read Request");
                btGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
                toastOnUithread("read request" + device.toString());
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                byte[] data;
                byte[] result = new byte[]{0x00};

                btGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);

                switch (value[0]) {
                    case 0x01:  //ファイル一覧を取得
                        if(new File(MainActivity.PATH_MAIN_DIRECTORY).isDirectory()) {
                            data = getFileListString(MainActivity.PATH_MAIN_DIRECTORY).getBytes();
                            result[0] = RESULT_ACK;
                        }else {
                            result[0] = RESULT_NAK;
                            data = new byte[]{0x00};
                        }
                        sendVariableBytesUsingNotification(result,data);
                        break;

                    case 0x02:  //ファイル名を指定して読み出し:
                        if (value.length <= 1) {
                            return;
                        }
                        byte[] bytes = Arrays.copyOfRange(value, 1, value.length);  //先頭1byte除いたファイルネーム取得
                        String filename = new String(bytes, StandardCharsets.UTF_8);
                        if (new File(MainActivity.PATH_MAIN_DIRECTORY + "/" + filename).exists()) {
                            data = getCsvString(MainActivity.PATH_MAIN_DIRECTORY, filename).getBytes();
                            result[0] = RESULT_ACK;
                        } else {
                            data = new byte[]{0x00};
                            result[0] = RESULT_NAK;
                        }
                        //dataSend = true;
                        sendVariableBytesUsingNotification(result, data);
                        break;

                    case 0x03:  //ファイル名を指定して削除
                        data = new byte[]{0x00};    //データ部は空
                        if (value.length <= 1) {
                            return;
                        }
                        bytes = Arrays.copyOfRange(value, 1, value.length);  //先頭1byte除いたファイルネーム取得
                        filename = new String(bytes, StandardCharsets.UTF_8);
                        result[0] = deleteFile(filename);
                        sendVariableBytesUsingNotification(result, data);
                        break;

                    case 0x04:  //履歴データ一括削除
                        data = new byte[]{0x00};
                        List<String> deleteList = new ArrayList<>();
                        File[] fileList = new File(MainActivity.PATH_MAIN_DIRECTORY).listFiles();
                        for (int num = 0; num <= fileList.length - 1; num++) {
                            if (!fileList[num].isDirectory() && !fileList[num].getName().contains("parameter.csv") && !fileList[num].getName().contains(".area")) {
                                deleteList.add(fileList[num].getName());    //履歴データのリスト作成
                            }
                        }
                        result[0] = RESULT_ACK;
                        for (int num = 0; num <= deleteList.size() - 1; num++) {
                            if (deleteFile(deleteList.get(num)) == RESULT_NAK) {    //一つでも削除失敗ならNAK返す
                                result[0] = RESULT_NAK;
                            }
                        }
                        sendVariableBytesUsingNotification(result, data);
                        break;

                    case 0x05:
                        result[0] = RESULT_ACK;
                        byte[] test = new byte[1024*1024];
                        for (int cnt = 0; cnt <= test.length - 1; cnt++) {
                            test[cnt] = (byte) cnt;
                        }
                        sendVariableBytesUsingNotification(result, test);
                        break;

                    default:
                        break;
                }
                Log.i("BLE", Integer.toString(value.length) + " bytes");
                String hexValue = Byte.toString(value[0]);
                Log.i("BLE", hexValue);
                btGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
                toastOnUithread("write request" + value.toString());
            }

            private byte deleteFile(String filename) {
                byte result;
                String path = MainActivity.PATH_MAIN_DIRECTORY + "/" + filename;
                File file = new File(path);
                if (file.exists()) {
                    String deleteCmd = "rm -r " + path;
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        Process p = runtime.exec(deleteCmd);
                        result = RESULT_ACK;    //削除成功
                    } catch (IOException e) {
                        e.printStackTrace();
                        result = RESULT_NAK;    //削除失敗
                    }
                } else {
                    result = RESULT_NAK;    //ファイル存在しない
                }
                return result;
            }

            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
                if (responseNeeded) {
                    btGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value);
                }
                toastOnUithread("descriptor write request" + device.toString());
            }

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                super.onDescriptorReadRequest(device, requestId, offset, descriptor);
                btGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                toastOnUithread("descriptor read request" + device.toString());
            }

            @Override
            public void onMtuChanged(BluetoothDevice device, int mtu) {
                MTU_SIZE = mtu - 3;     //ペイロードはmtu-3
                Log.d("BLE", "onMtuChanged: " + Integer.toString(mtu));
                toastOnUithread("mtu changed =>" + Integer.toString(mtu));
            }

            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                super.onExecuteWrite(device, requestId, execute);
            }

            @Override
            public void onNotificationSent(BluetoothDevice device, int status) {
                super.onNotificationSent(device, status);
            }
        };
    }

    private void toastOnUithread(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    int ADVERTISE_TIMEOUT_IM_MILLS = 10 * 1000;
    private AdvertiseSettings createAdvSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        builder.setConnectable(true);
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        builder.setTimeout(ADVERTISE_TIMEOUT_IM_MILLS);
        return builder.build();
    }

    private AdvertiseData createAdvData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeTxPowerLevel(true);
        builder.setIncludeDeviceName(true);
        //builder.addServiceUuid(ParcelUuid.fromString(SERVICE_UUID));
        return builder.build();
    }

    private void setAdvertiseCallback() {
        advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                isAdvertising = true;
                Log.i("BLE", "adv start success");
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Log.i("BLE", "adv start failed");
            }
        };
    }

    private void startAdvertise() {
        if (btAdvertiser != null) {
            btAdvertiser.startAdvertising(createAdvSettings(), createAdvData(), advertiseCallback);
            Log.i("BLE", "start advertising");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isConnected) {
                        Log.d("BleFileExplorer", "timeout");
                        stopAdvertise();
                        bfeCallback.showBfeProgressDialog(false, "");
                        bfeCallback.showBfeToast("接続タイムアウト");
                    }
                }
            }, ADVERTISE_TIMEOUT_IM_MILLS);
        }
    }

    private void stopAdvertise() {
        if (btGattServer != null) {
            btGattServer.clearServices();
            btGattServer.close();
            btGattServer = null;
        }
        if (btAdvertiser != null) {
            btAdvertiser.stopAdvertising(advertiseCallback);
            isAdvertising = false;
            Log.i("BLE", "stop advertising");
        }
    }

    private File[] files;
    private String getFileListString(String path) {
        files = new File(path).listFiles();
        String listString = "";

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                //listString += (files[i].getName().toString() + "/,");
                //Log.i("FILE", files[i].toString());
            } else if ((!files[i].getName().contains("parameter.csv")) && (!files[i].getName().contains(".area"))) {

                int fileSize = (int) files[i].length() / 1024;
                if (fileSize == 0) {
                    fileSize = 1;
                }
                String fileSizeString = Integer.toString(fileSize) + "[KB]";
                listString += (files[i].getName().toString() + "," + fileSizeString + ",");
                Log.i("FILE", files[i].toString());
            }
        }
        StringBuilder stringBuilder = new StringBuilder(listString);
        if (stringBuilder.length() >= 1) {
            stringBuilder.setLength(stringBuilder.length() - 1);
        }
        listString = stringBuilder.toString();
        return listString;
    }

    private String getCsvString(String path, String filename) {
        String csvString = "";

        try {
            FileInputStream fileInputStream = new FileInputStream(path + "/" + filename);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = "";

            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                csvString += line + "\r\n";
                Log.d("BLE", "csv read line : " + lineNum);
                lineNum++;
            }
            Log.d("BLE", "csv read done.");

        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder stringBuilder = new StringBuilder(csvString);
        stringBuilder.setLength(stringBuilder.length() - 1);
        csvString = stringBuilder.toString();

        return csvString;
    }

    private byte calculateChecksum(byte[] data) {
        byte sum = 0x00;
        for (int cnt = 0; cnt <= data.length - 1; cnt++) {
            sum += data[cnt];
        }
        return sum;
    }

    private void sendVariableBytesUsingNotification(byte[] result, final byte[] data) {

        final List<byte[]> splitSendData = new ArrayList<>();

        byte[] dataSize = ByteBuffer.allocate(4).putInt(data.length).array();   //先頭4バイトはデータ長
        ByteBuffer byteBuffer = ByteBuffer.allocate(1 + 4 + data.length + 1);   //(ACK:1バイト + length:4バイト + data.length +  checksum:1byte)が送るデータの全体サイズ
        byteBuffer.put(result);
        byteBuffer.put(dataSize);
        byteBuffer.put(data);
        byteBuffer.put(calculateChecksum(data));

        byte[] sendData = byteBuffer.array();
        int splitSize = (sendData.length / MTU_SIZE);
        int amari = sendData.length % MTU_SIZE;

        for (int num = 0; num < splitSize; num++) {
            splitSendData.add(Arrays.copyOfRange(sendData, num * MTU_SIZE, num * MTU_SIZE + MTU_SIZE));
        }
        if (amari != 0) {
            splitSendData.add(Arrays.copyOfRange(sendData, splitSize * MTU_SIZE, splitSize * MTU_SIZE + amari));
        }

        double size = 0;

        for (int cnt = 0; cnt <= splitSendData.size() - 1; cnt++) {
            size = size + splitSendData.get(cnt).length;
        }

        if (btGattCharacteristic == null) {
            Log.d("BLE", "characteristic is null");
            return;
        }

        Log.d("BLE", "mtu = " + Integer.toString(MTU_SIZE) + ", array size = " + Integer.toString(splitSendData.size()));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int num = 0;
                        // TODO: ここで処理を実行する
                        handler = new Handler();
                        r = new Runnable() {
                            int cnt = 0;
                            double size = 0;
                            public void run() {
                                if (cnt < splitSendData.size()) {
                                    Log.d("BLE", Double.toString(size += splitSendData.get(cnt).length) + " [bytes]");
//                                    if(!(dataSend&&(cnt>=2))){
//                                    }
                                    btGattCharacteristic.setValue(splitSendData.get(cnt));
                                    btGattServer.notifyCharacteristicChanged(connectedDevice, btGattCharacteristic, false);
                                    cnt++;
                                }
                                handler.postDelayed(this, 10);
                            }
                        };
                        handler.post(r);
                    }
                }, 10);
            }
        });
    }
}
