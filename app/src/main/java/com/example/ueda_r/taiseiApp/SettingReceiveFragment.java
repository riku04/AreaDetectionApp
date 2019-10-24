package com.example.ueda_r.taiseiApp;

import android.app.ProgressDialog;
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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.ueda_r.osmdroidtest1022.R;

import org.apache.commons.lang3.SerializationUtils;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingReceiveFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingReceiveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingReceiveFragment extends Fragment implements View.OnClickListener, Serializable {

    private Context context;
    //MainActivity.Parameter parameter;
    static final byte PARAMETER_USERID = 0x00;
    static final byte PARAMETER_GROUPID = 0x01;
    static final byte PARAMETER_ADMIN = 0x02;
    static final byte PARAMETER_AREA_QTY = 0x03;
    static final byte PARAMETER_AREA_DATA = 0x04;
    static final byte PARAMETER_ENTER_ALERT = 0x05;
    static final byte PARAMETER_CLOSE_ALERT = 0x06;
    static final byte PARAMETER_JUKI_ALERT = 0x07;
    static final byte PARAMETER_VIBRATION = 0x08;
    static final byte PARAMETER_CLOSE_VOLUME = 0x09;
    static final byte PARAMETER_ENTER_VOLUME = 0x0a;
    static final byte PARAMETER_JUKI_VOLUME = 0x0b;
    static final byte PARAMETER_LOGGING = 0x0c;
    static final byte PARAMETER_START_HOUR = 0x0d;
    static final byte PARAMETER_START_MIN = 0x0e;
    static final byte PARAMETER_END_HOUR = 0x0f;
    static final byte PARAMETER_END_MIN = 0x10;
    static final byte PARAMETER_CLOSE_DISTANCE = 0x11;
    static final byte PARAMETER_JUKI_DISTANCE = 0x12;
    static final byte PARAMETER_NORMAL_INTERVAL = 0x13;
    static final byte PARAMETER_SEMI_CLOSE_INTERVAL = 0x14;
    static final byte PARAMETER_CLOSE_INTERVAL = 0x15;
    static final byte PARAMETER_ENTER_INTERVAL = 0x16;
    static final byte PARAMETER_JUKI_CLOSE_INTERVAL = 0x17;
    static final byte PARAMETER_JUKI_QTY = 0x18;
    static final byte PARAMETER_JUKI_DATA = 0x19;

    String SERVICE_UUID = "cc77f566-4882-4b4c-b3d5-39b4652e3fe3";
    String CHARACTERISTIC_UUID = "716524d4-95ce-49c7-aac4-11ae3ad01007";
    String CHARACTERISTIC_CONFIG_UUID = "17d1815f-ccdc-473e-a15a-6626d1ab68cf";

    public static final String PATH = MainActivity.PATH_MAIN_DIRECTORY + "/" + "uuid.txt";
    String uuid = "";
    String mac = "";

    final int REQUEST_ENABLE_BT = 1;

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private Boolean isAdvertising = false;

    private BluetoothGattServer btGattServer;
    private BluetoothGattServerCallback btGattServerCallback;
    private BluetoothGattService btGattService;
    private BluetoothGattCharacteristic btGattCharacteristic;
    private BluetoothGattDescriptor btGattDescriptor;
    private AdvertiseData scanResponseData;

    private BluetoothLeAdvertiser btAdvertiser;
    private AdvertiseCallback advertiseCallback;

    ProgressDialog progressDialog;
    ProgressDialog advertiseProgress;

    private MainActivity.Parameter parameter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SettingReceiveFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShareFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingReceiveFragment newInstance(String param1, String param2) {
        SettingReceiveFragment fragment = new SettingReceiveFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting_receive, null);

        context = v.getContext();

        v.setFocusableInTouchMode(true);
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    // 何らかの処理
                    removeThisFragment();
                    stopAdvertise();
                    return true;
                }
                return false;
            }
        });

        Bundle bundle = getArguments();     //インテントでカスタムParameterクラスのオブジェクトを渡す
        parameter = (MainActivity.Parameter) bundle.getParcelable("PARAMETER");
        //parameter.clearAreaData();

//        if (isExistUuid(PATH)) {
//            uuid = readUuid(PATH);
//            Log.i("BLE", "UUID exists : " + uuid);
//        } else {
//            uuid = UUID.randomUUID().toString();
//            saveUuid(PATH, uuid);
//            Log.i("BLE", "UUID generated : " + uuid);
//        }

        uuid = CHARACTERISTIC_UUID;

        ImageButton backButton = v.findViewById(R.id.settingReceiveBackButton);
        backButton.setOnClickListener(this);

        btManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        if (btManager != null) {
            btAdapter = btManager.getAdapter();
            Log.i("BLE", "btManager not null");
        } else {
            Log.i("BLE", "btManager null");
        }
        initBle();

        final Button advertiseButton = v.findViewById(R.id.advertiseButton);
        advertiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BLE", "adv start button clicked");
                startAdvertise();

                advertiseProgress = new ProgressDialog(context);

                advertiseProgress.setCanceledOnTouchOutside(false);

                advertiseProgress.setMessage("通信中…");
                advertiseProgress.setButton(DialogInterface.BUTTON_NEGATIVE, "cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopAdvertise();
                    }
                });
                advertiseProgress.show();

            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // インテントでBluetoothをOnにしたら、使用準備開始.
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if ((btAdapter != null)
                        || (btAdapter.isEnabled())) {
                    // if BLE is enabled, start advertising.
                    //this.prepareBle();
                }
                break;
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onClick(View v) {
        removeThisFragment();
        stopAdvertise();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SETTING FRAGMENT", "onDestroy");
        stopAdvertise();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("SETTING FRAGMENT", "onDestroyView");

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void removeThisFragment() {
        getFragmentManager().beginTransaction().remove(this).commit();
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right).remove(this).commit();
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }
    private void removeThisFragmentWithoutOpenMenu(){
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right).remove(this).commit();
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
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i("BLE", "Disconnected");
                }
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                Log.i("BLE", "Read Request");

                btGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

                Log.i("BLE", Integer.toString(value.length) + " bytes");

                String hexValue = Byte.toString(value[0]);
                Log.i("BLE", hexValue);

                btGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);

                boolean isLastData = receiveParameter(value, device);   //受け取ったパラメータで最後なら設定受信画面閉じる
                if (isLastData) {

                    UUID uuid = characteristic.getUuid();



                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //stopAdvertise();
                            progressDialog.cancel();
                            advertiseProgress.cancel();
                            Toast.makeText(getActivity(), "設定完了", Toast.LENGTH_SHORT).show();
                            removeThisFragmentWithoutOpenMenu();
                            stopAdvertise();
                            new AlertDialog.Builder(getActivity())
                                    .setMessage("設定完了")
                                    .setPositiveButton("OK", null)
                                    .show();
                            //btGattServer.cancelConnection(device);
                        }
                    });
                }

            }

            @Override
            public void onMtuChanged(BluetoothDevice device, int mtu) {
                Log.d("BLE", "onMtuChanged: " + Integer.toString(mtu));

            }

        };
    }

    private void setAdvertiseCallback() {
        advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                isAdvertising = true;
                Log.i("BLE", "Adv start");
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Log.i("BLE", "Adv start failed");
            }
        };
    }

    private AdvertiseSettings createAdvSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        builder.setConnectable(true);
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        return builder.build();
    }

    private AdvertiseData createAdvData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeTxPowerLevel(false);
        builder.setIncludeDeviceName(true);
        //builder.addServiceUuid(ParcelUuid.fromString(SERVICE_UUID));
        return builder.build();
    }

    private void initBle() {
        Log.i("BLE", "initBLE()");

        if ((btAdapter != null) && (!isAdvertising)) {
            Log.i("BLE", "Init start");

            btAdapter.setName(parameter.getGroupID() + "," + parameter.getUserID());
            setBtGattServerCallback();
            Log.i("BLE", "server callback set");

            //サービスにキャラクタリスティックを追加してサービスをサーバに登録
            btGattServer = btManager.openGattServer(getContext(), btGattServerCallback);
            btGattService = new BluetoothGattService(UUID.fromString(SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY);

            btGattCharacteristic = new BluetoothGattCharacteristic(UUID.fromString(uuid)
                    , BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE
                    , BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);

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
            //btAdvertiser.startAdvertising(createAdvSettings(),createAdvData(),advertiseCallback);

        }
    }

    private void startAdvertise() {
        if (btAdvertiser != null) {
            btAdvertiser.startAdvertising(createAdvSettings(), createAdvData(), advertiseCallback);
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
        }
    }

    private Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }

    private void sendReceiveCompleted() {

    }

    private boolean receiveParameter(byte[] receive, final BluetoothDevice device) {

        boolean isLastData =false;
        byte paramType = receive[0];
        byte data[] = Arrays.copyOfRange(receive, 1, receive.length - 1);

        for (int cnt = 0; cnt <= data.length - 1; cnt++) {
            //Log.i("BLE", "***count:" + Integer.toString(cnt) + ":" + Integer.toHexString(data[cnt]));
        }

        byte checksum = 0;
        for (int num = 0; num <= data.length - 1; num++) {
            checksum += data[num];
        }
        if (checksum != receive[receive.length - 1]) {
            Log.i("BLE", "Checksum failed");
            //***要再送要求***//
            return false;
        } else {
            Log.i("BLE", "Checksum success >>> " + Integer.toHexString(checksum));

            switch (paramType) {
                case PARAMETER_USERID:

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog = new ProgressDialog(context);
                            progressDialog.setMessage("設定受信中…");
                            progressDialog.show();
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    stopAdvertise();
                                }
                            });
                        }
                    });
                    parameter.clearAreaData();//
                    Log.i("BLE", "UserId received");
                    String userId = SerializationUtils.deserialize(data);
                    Log.i("BLE", "UserId:" + userId);
                    parameter.setUserID(userId);

                    break;

                case PARAMETER_GROUPID:
                    Log.i("BLE", "GroupId received");
                    String groupId = SerializationUtils.deserialize(data);
                    Log.i("BLE", "GroupId:" + groupId);
                    parameter.setGroupID(groupId);
                    break;

                case PARAMETER_ADMIN:
                    Log.i("BLE", "Admin received");
                    Boolean admin = SerializationUtils.deserialize(data);
                    Log.i("BLE", "Adnim:" + Boolean.toString(admin));
                    parameter.setAdmin(admin);
                    break;

                case PARAMETER_AREA_QTY:
                    Log.i("BLE", "Area Qty received");
                    int areaQty = SerializationUtils.deserialize(data);
                    Log.i("BLE", "AreaQty:" + Integer.toString(areaQty));
                    if (areaQty == 0) {
                        parameter.clearAreaData();
                    }
                    break;

                case PARAMETER_AREA_DATA:
                    Log.i("BLE", "Area Data received");
                    ArrayList<GeoPoint> areaData = SerializationUtils.deserialize(data);
                    Log.i("BLE", "AreaData:");
                    Log.i("BLE", areaData.toString());
                    parameter.addAreaDataParam(areaData);
                    break;

                case PARAMETER_ENTER_ALERT:
                    Log.i("BLE", "Enter Alert received");
                    Boolean enterAlert = SerializationUtils.deserialize(data);
                    Log.i("BLE", "EnterAlert:" + Boolean.toString(enterAlert));
                    parameter.setEnterAlert(enterAlert);
                    break;

                case PARAMETER_CLOSE_ALERT:
                    Log.i("BLE", "Close Alert received");
                    Boolean closeAlert = SerializationUtils.deserialize(data);
                    Log.i("BLE", "CloseAlert:" + Boolean.toString(closeAlert));
                    parameter.setCloseAlert(closeAlert);
                    break;

                case PARAMETER_JUKI_ALERT:
                    Log.i("BLE", "Juki Alert received");
                    Boolean jukiAlert = SerializationUtils.deserialize(data);
                    Log.i("BLE", "JukiAlert:" + Boolean.toString(jukiAlert));
                    parameter.setJukiAlert(jukiAlert);
                    break;

                case PARAMETER_VIBRATION:
                    Log.i("BLE", "Vibration received");
                    Boolean vibration = SerializationUtils.deserialize(data);
                    Log.i("BLE", "Vibration:" + Boolean.toString(vibration));
                    parameter.setVibration(vibration);
                    break;

                case PARAMETER_CLOSE_VOLUME:
                    Log.i("BLE", "Close Volume received");
                    int closeVolume = SerializationUtils.deserialize(data);
                    Log.i("BLE", "CloseVolume:" + Integer.toString(closeVolume));
                    parameter.setCloseVolume(closeVolume);
                    break;

                case PARAMETER_ENTER_VOLUME:
                    Log.i("BLE", "Enter Volume received");
                    int enterVolume = SerializationUtils.deserialize(data);
                    Log.i("BLE", "EnterVolume:" + Integer.toString(enterVolume));
                    parameter.setEnterVolume(enterVolume);
                    break;

                case PARAMETER_JUKI_VOLUME:
                    Log.i("BLE", "Juki Volume received");
                    int jukiVolume = SerializationUtils.deserialize(data);
                    Log.i("BLE", "JukiVolume:" + Integer.toString(jukiVolume));
                    parameter.setJukiVolume(jukiVolume);
                    break;

                case PARAMETER_LOGGING:
                    Log.i("BLE", "Logging received");
                    Boolean logging = SerializationUtils.deserialize(data);
                    Log.i("BLE", "Logging:" + Boolean.toString(logging));
                    parameter.setLoggingOn(logging);
                    break;

                case PARAMETER_START_HOUR:
                    Log.i("BLE", "Start Hour received");
                    int startHour = SerializationUtils.deserialize(data);
                    Log.i("BLE", "StartHour:" + Integer.toString(startHour));
                    parameter.setStartHour(startHour);
                    break;

                case PARAMETER_START_MIN:
                    Log.i("BLE", "Start Minute received");
                    int startMinute = SerializationUtils.deserialize(data);
                    Log.i("BLE", "StartMinute:" + Integer.toString(startMinute));
                    parameter.setStartMinute(startMinute);
                    break;

                case PARAMETER_END_HOUR:
                    Log.i("BLE", "End Hour received");
                    int endHour = SerializationUtils.deserialize(data);
                    Log.i("BLE", "EndHour:" + Integer.toString(endHour));
                    parameter.setEndHour(endHour);
                    break;

                case PARAMETER_END_MIN:
                    Log.i("BLE", "End Minute received");
                    int endMin = SerializationUtils.deserialize(data);
                    Log.i("BLE", "EndMin:" + Integer.toString(endMin));
                    parameter.setEndMinute(endMin);
                    break;

                case PARAMETER_CLOSE_DISTANCE:
                    Log.i("BLE", "Close Distance received");
                    int closeDistance = SerializationUtils.deserialize(data);
                    Log.i("BLE", "CloseDistance:" + Integer.toString(closeDistance));
                    parameter.setCloseDistance(closeDistance);
                    break;

                case PARAMETER_JUKI_DISTANCE:
                    Log.i("BLE", "Enter Distance received");
                    int jukiDistance = SerializationUtils.deserialize(data);
                    Log.i("BLE", "EnterDistance:" + Integer.toString(jukiDistance));
                    parameter.setJukiDistance(jukiDistance);
                    break;

                case PARAMETER_NORMAL_INTERVAL:
                    Log.i("BLE", "Normal Interval received");
                    int normalInterval = SerializationUtils.deserialize(data);
                    Log.i("BLE", "NormalInterval:" + Integer.toString(normalInterval));
                    parameter.setNormalLogIntvl(normalInterval);
                    break;

                case PARAMETER_SEMI_CLOSE_INTERVAL:
                    Log.i("BLE", "Semi Close Interval received");
                    int semiCloseInterval = SerializationUtils.deserialize(data);
                    Log.i("BLE", "SemiCloseInterval:" + Integer.toString(semiCloseInterval));
                    parameter.setSemiCloseLogIntvl(semiCloseInterval);
                    break;

                case PARAMETER_CLOSE_INTERVAL:
                    Log.i("BLE", "Close Interval received");
                    int closeInterval = SerializationUtils.deserialize(data);
                    Log.i("BLE", "CloseInterval:" + Integer.toString(closeInterval));
                    parameter.setCloseLogIntvl(closeInterval);
                    break;

                case PARAMETER_ENTER_INTERVAL:
                    Log.i("BLE", "Enter Interval received");
                    int enterInterval = SerializationUtils.deserialize(data);
                    Log.i("BLE", "EnterInterval:" + enterInterval);
                    parameter.setEnterLogIntvl(enterInterval);
                    break;

                case PARAMETER_JUKI_CLOSE_INTERVAL:
                    Log.i("BLE", "Juki Interval received");
                    int jukiInterval = SerializationUtils.deserialize(data);
                    Log.i("BLE", "JukiInterval:" + jukiInterval);
                    parameter.setJukiCloseLogIntvl(jukiInterval);
                    break;

                case PARAMETER_JUKI_QTY:
                    Log.i("BLE", "Juki Qty received");
                    int jukiQty = SerializationUtils.deserialize(data);
                    Log.i("BLE", "JukiQty:" + jukiQty);
                    break;

                case PARAMETER_JUKI_DATA:
                    Log.i("BLE", "Juki Data received");
                    ArrayList<String> jukiData = SerializationUtils.deserialize(data);
                    Log.i("BLE", "JukiData:" + jukiData.toString());
                    parameter.setJukiList(jukiData);
                    parameter.outputParamToCsv();
                    isLastData = true;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            reloadLocationService();
                        }
                    });
                    break;
            }
        }
        return isLastData;
    }

    private void saveUuid(String path, String uuid) {
        try {
            FileWriter fw = new FileWriter(path, false);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            pw.print(uuid);
//          pw.println();
            pw.close();
        } catch (Exception e) {
            Log.i("BLE",e.toString());
            e.printStackTrace();
        }
    }

    public String readUuid(String path) {
        Log.i("BLE","readUuid");
        String uuid = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            uuid = reader.readLine();

            reader.close();
            inputStreamReader.close();
            fileInputStream.close();
        } catch (Exception e) {
            Log.i("BLE",e.toString());
        }
        return uuid;
    }
    public boolean isExistUuid(String path){
        Boolean exist = false;;

        File file = new File(path);

        exist = file.exists();
        Log.i("BLE", "PATH = " + path + "====>>>" + exist.toString());

        return exist;
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i("LocationService", "Bind on [SettingReceiveFragment]");
        Intent reloadIntent = new Intent(getActivity().getApplicationContext(), LocationService.class);
        getActivity().bindService(reloadIntent,serviceConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i("LocationService", "Bind off [SettingReceiveFragment]");
        getActivity().unbindService(serviceConnection);
    }

    private LocationService locationService;
    private boolean bound = false;

    public void reloadLocationService(){
        if (bound) {
            locationService.reloadParameter();
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.MyBinder binder = (LocationService.MyBinder) service;
            locationService = binder.getService();
            //locationService.reloadParameter();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };


}

