package com.example.ueda_r.taiseiApp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ueda_r.osmdroidtest1022.R;

import org.apache.commons.lang3.SerializationUtils;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChildSettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChildSettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChildSettingFragment extends Fragment implements View.OnClickListener, ScanResultListAdapter.ScanResultListAdapterCallback{


    private AlertDialog dialog;
    private Context context;
    private Boolean CONNECTED = false;
    private Boolean DATA_SEND_READY = false;

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

    private MainActivity.Parameter childParameter;
    private ArrayList<Boolean> invalidArray;

    //BLE_SCAN
    final String CHARACTERISTIC_UUID = "716524d4-95ce-49c7-aac4-11ae3ad01007";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt = null;    // Gattサービスの検索、キャラスタリスティックの読み書き
    final int REQUEST_ENABLE_BT = 1;
    private boolean mScanning = false;
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 10000;

    private ProgressBar scanProgress;
    private ProgressDialog sendProgress;

    BluetoothGatt gattWriter;
    BluetoothGattCharacteristic characteristic;

    private ArrayList<byte[]> writeBuffer = new ArrayList<>();
    private Boolean writeBufferReady = false;
    private int writeCounter = 0;

    private ProgressDialog progressDialog;
    private String connectedAddress = "";

    //ListView
    ChildSettingFragmentCallback callback;
    ListView listView;
    ScanResultListAdapter adapter;
    ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    ArrayList<ScanResultListItem> itemList = new ArrayList<>();
    ArrayList<String> addressList = new ArrayList<>();

    ArrayList<BluetoothDevice> checkedDeviceList = new ArrayList<>();

    boolean bool = false;

    private ConnectedChildData childData = new ConnectedChildData();

    private ProgressBar itemProgressBar;
    private Button multiButton;
    private Button scanButton;
    private Button clearButton;
    private Button checkAllButton;

    private Boolean isMultiButtonEnable = false;
    private Boolean isScanButtonEnable = true;
    private Boolean isClearButtonEnable = false;
    private Boolean isCheckAllButtonEnable = false;

    private Boolean isSettingCheckedDevices = false;

    private void setDevice(final BluetoothDevice device) {

        String userId = "";
        String groupId = "";

        if ((device.getName() != null) && (device.getName().split(",").length == 2)) {
            String[] str = device.getName().split(",");
            userId = str[1];
            groupId = str[0];
        } else {
            userId = childParameter.getUserID();
            groupId = childParameter.getGroupID();
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View layout = inflater.inflate(R.layout.dialog_setting_child_without_admin, null);

        final EditText userIdEditText = layout.findViewById(R.id.userID_child_without_admin);
        userIdEditText.setText(userId);


        final EditText groupIdEditText = layout.findViewById(R.id.groupID_child_without_admin);
        groupIdEditText.setText(groupId);

//        final EditText password = layout.findViewById(R.id.password_child);
//        final Switch admin = layout.findViewById(R.id.adminSwitch_child);
//        admin.setChecked(false);
//        admin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (password.getText().toString().equals("1111")) {
//                    admin.setChecked(false);
//                }
//            }
//        });
//        admin.setEnabled(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("子機の設定");
        builder.setView(layout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                //childParameter.setAdmin(admin.isChecked());

                childParameter.setUserID(userIdEditText.getText().toString());
                childParameter.setGroupID(groupIdEditText.getText().toString());

                for (int num = 0; num <= itemList.size() - 1; num++) {
                    if (itemList.get(num).getDeviceAddress() == connectedAddress) {
                        itemList.get(num).setDeviceName(childParameter.getUserID() + "," + childParameter.getGroupID());
                        itemList.get(num).setDeviceStatus("接続中");
                        updateListView();
                    }
                }
                final ArrayList<BluetoothDevice> deviceArrayList = new ArrayList<>();   //要素数1の配列
                deviceArrayList.add(device);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("別のスレッドだよ");
                        setAllCheckedDevices(deviceArrayList, true);    //パラメータ設定有効
                    }
                }).start();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                enableAllConnectButton(true);
                //parameter.setAdmin(admin);
            }
        });
        // 表示
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    ScanResultListAdapter.ScanResultListAdapterCallback scanResultListAdapterCallback = new ScanResultListAdapter.ScanResultListAdapterCallback() {
        @Override
        public void connectPressed(BluetoothDevice bluetoothDevice, Button connectButton, ProgressBar progressBar) {

//            itemProgressBar = progressBar;
//            itemProgressBar.setVisibility(ProgressBar.VISIBLE);

            enableAllConnectButton(false);
            setDevice(bluetoothDevice);

//            Toast.makeText(getContext(), bluetoothDevice.getAddress().toString(), Toast.LENGTH_SHORT).show();
//            mBluetoothGatt = bluetoothDevice.connectGatt(getContext(), false, bluetoothGattCallback);
//
//            progressDialog = new ProgressDialog(context);
//            progressDialog.setMessage("接続中…");
//            progressDialog.show();
        }

        @Override
        public void deviceChecked(BluetoothDevice bluetoothDevice, Boolean isChecked) {
            if (isChecked == true) {
                if(!checkedDeviceList.contains(bluetoothDevice)) {
                    checkedDeviceList.add(bluetoothDevice);
                }
                Log.i("BLE", "Added to checked device list : " + bluetoothDevice.getName());
                Log.i("BLE", "List size : " + Integer.toString(checkedDeviceList.size()));

            } else {
                if(checkedDeviceList.contains(bluetoothDevice)){
                    checkedDeviceList.remove(bluetoothDevice);
                }
                Log.i("BLE", "removed from checked device list : " + bluetoothDevice.getName());
                Log.i("BLE", "List size : " + Integer.toString(checkedDeviceList.size()));
            }

            if (!checkedDeviceList.isEmpty() && !isSettingCheckedDevices) {
                multiButton.setEnabled(true);
            } else {
                multiButton.setEnabled(false);
            }
        }
    };

    private void updateListView() {
        Log.i("BLE", "listview update");

        for (int num = 0; num <= itemList.size() - 1; num++) {
            itemList.get(num).setConnectedAddress(connectedAddress);
            Log.i("BLE", "*>*>*>*>*>*>" + itemList.get(num).getDeviceAddress() + "," + itemList.get(num).getConnectedAddress());
            if (itemList.get(num).getDeviceAddress() == itemList.get(num).getConnectedAddress()) {
                Log.i("BLE", "~~~Address matched~~~");
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //Toast.makeText(getContext(), result.getDevice().toString(), Toast.LENGTH_SHORT).show();
            Log.i("BLE", "Scan result :" + result.getDevice().getAddress().toString());
            deviceList.add(result.getDevice());

            enableButtons(!itemList.isEmpty(), !checkedDeviceList.isEmpty(), (!isSettingCheckedDevices && !mScanning), (!isSettingCheckedDevices && !mScanning));

            if (!addressList.contains(result.getDevice().getAddress().toString())) {
                addressList.add(result.getDevice().getAddress().toString());

                ScanResultListItem item = new ScanResultListItem(result.getDevice());
                item.setConnectedAddress(connectedAddress);

                String name = result.getDevice().getName();
                if (name != "" || name != null) {
                    item.setDeviceName(name);
                } else {
                    item.setDeviceName("device name(test)");
                }

                Log.i("BLE", "Get item.");


//                //deviceNameに","が含まれるitemが来たら0番目に置き換える
//                if (item.getDeviceName() == null) {
//                    itemList.add(item);
//                } else if (!item.getDeviceName().contains(",") || itemList.size() <= 0) {
//                    itemList.add(item);
//                } else {
//                    ScanResultListItem temp = itemList.get(0);
//                    itemList.set(0, item);
//                    itemList.add(temp);
//                }

                itemList.add(item);
                itemList.sort(new Comparator<ScanResultListItem>() {
                    @Override
                    public int compare(ScanResultListItem item1, ScanResultListItem item2) {
                        String group1 = item1.getDeviceName();
                        String group2 = item2.getDeviceName();

                        //名前無しの場合後ろに送る
                        if (group1 == null || group1 == "") {
                            return 1;
                        }
                        if ((group1 == null || group1 == "") && (group2 == null || group2 == "")) {
                            return 0;
                        }
                        if (group2 == null || group2 == "") {
                            return -1;
                        }

                        //","が含まれない場合後ろに送る
                        if (!group1.contains(",")) {
                            return 1;
                        }
                        if (!group1.contains(",") && !group2.contains(",")) {
                            return group1.compareTo(group2);
                        }
                        if (!group2.contains(",")) {
                            return -1;
                        }

                        //名前有りかつ","ありの中で降順ソート
                        return group1.compareTo(group2);
                    }
                });

//                if (item.getDeviceName() != null) {
//                    if (item.getDeviceName().contains(",")) {
//                        ScanResultListItem temp = itemList.get(0);
//                        itemList.set(0, item);
//                        itemList.remove(itemList.get(itemList.size() - 1));
//                        itemList.add(temp);
//                    } else {
//                        itemList.add(item);
//                    }
//                } else {
//                    itemList.add(item);
//                }


                if (isSettingCheckedDevices || mScanning) {
                    item.setConnectEnable(false);
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });

            }
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Toast.makeText(getContext(), "onBatchScanResult", Toast.LENGTH_SHORT).show();
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Toast.makeText(getContext(), "onScanFailed", Toast.LENGTH_SHORT).show();
            super.onScanFailed(errorCode);
        }
    };
    private BluetoothLeScanner bluetoothLeScanner;
    Handler handler;
    Runnable r;
    private void scanLeDevice(final boolean enable) {
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        Log.i("BLE", "Get bluetooth le scanner.");

        handler = new Handler();
        if (enable) {
            r = new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(mLeScanCallback);
                    scanProgress.setVisibility(View.GONE);
                    //clearButton.setEnabled(true);
                    enableButtons(!itemList.isEmpty(), !checkedDeviceList.isEmpty() && !isSettingCheckedDevices, true, !itemList.isEmpty() && !isSettingCheckedDevices);
                    enableAllConnectButton(!isSettingCheckedDevices);
                    Log.i("BLE", "Scan stop");
                }
            };
            handler.postDelayed(r, SCAN_PERIOD);

//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mScanning = false;
//                    bluetoothLeScanner.stopScan(mLeScanCallback);
//                    scanProgress.setVisibility(View.GONE);
//                    //clearButton.setEnabled(true);
//                    enableButtons(!itemList.isEmpty(), !checkedDeviceList.isEmpty() && !isSettingCheckedDevices, true, !itemList.isEmpty() && !isSettingCheckedDevices);
//                    enableAllConnectButton(true);
//                    Log.i("BLE", "Scan stop");
//                }
//            }, SCAN_PERIOD);

            mScanning = true;
            //clearButton.setEnabled(false);
            enableButtons(!itemList.isEmpty(), false, false, false);
            enableAllConnectButton(false);
            bluetoothLeScanner.startScan(mLeScanCallback);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(mLeScanCallback);
            scanProgress.setVisibility(View.GONE);
            clearButton.setEnabled(true);
            Log.i("BLE", "Scan stop");
        }
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i("BLE", "state changed");

            Log.i("BLE", "newState:" + Integer.toString(newState));

            if (status == 133) {
                progressDialog.cancel();
                gatt.disconnect();
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("BLE", "connected");
                CONNECTED = true;
                mBluetoothGatt.requestMtu(512);
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("BLE", "disconnected");
                CONNECTED = false;
                Toast.makeText(context, "disconnected", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.i("BLE", "MTU size changed :" + Integer.toString(mtu));
            if (mBluetoothGatt.discoverServices()) {
                Log.i("BLE", "Service discover started");
            } else {
                Log.i("BLE", "Service discover start failed");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            gattWriter = gatt;
            Log.i("BLE", "Service discovered");
            List<BluetoothGattService> serviceList;
            serviceList = gatt.getServices();
            for (int i = 0; i <= serviceList.size() - 1; i++) {
                List<BluetoothGattCharacteristic> charaList;
                charaList = serviceList.get(i).getCharacteristics();

                for (int j = 0; j <= charaList.size() - 1; j++) {
                    Log.i("BLE", "characteristic >>> " + charaList.get(j).getUuid().toString());
                    //if(charaList.get(j).getUuid().toString().equalsIgnoreCase(CHARACTERISTIC_UUID)){

                    String uuid = "";
                    uuid = charaList.get(j).getUuid().toString();
                    if (!uuid.contains("0000-1000-8000-00805f9b34fb")) { //if not standard characteristics

                        Log.i("BLE", "Target characteristic found! ***>>>" + uuid);
                        characteristic = serviceList.get(i).getCharacteristic(UUID.fromString(uuid));

                        DATA_SEND_READY = true;
                        progressDialog.cancel();
                        connectedAddress = gatt.getDevice().getAddress();


                        updateListView();


                        Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("BLE", "Characteristic write success");

                if (writeCounter <= writeBuffer.size() - 1) {
                    if (writeCounter == 0) {
                        sendProgress.show();
                    }
                    characteristic.setValue(writeBuffer.get(writeCounter));
                    if (gattWriter.writeCharacteristic(characteristic)) {
                        writeCounter++;
                        //Log.i("BLE", "write:" + Integer.toHexString(writeBuffer.get(writeCounter)[0]));

                        if (writeCounter == writeBuffer.size() - 1) {
                            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
                            String formatDate = format.format(calendar.getTime());
                            childData.addChildData(formatDate, characteristic.getUuid().toString(), childParameter.getUserID(), childParameter.getGroupID());
                            childData.outputConnectedChildLog(MainActivity.PATH_MAIN_DIRECTORY);
                        }

                    } else {
                        Log.i("BLE", "writeCharacteristic returns false");
                    }
                } else {
                    Log.i("BLE", "GATT status : " + Integer.toString(status));
                    writeCounter = 0;
                    gatt.close();   //受信側から切るべき…
                    sendProgress.cancel();

                    addressList.clear();
                    itemList.clear();
                    adapter.notifyDataSetChanged();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage("設定完了")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    });

                    scanLeDevice(true);
                    scanProgress.setVisibility(View.VISIBLE);
                }
            }

            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.i("BLE", "Characteristic write failed");

            }
        }
    };

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ChildSettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChildSettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChildSettingFragment newInstance(String param1, String param2) {
        ChildSettingFragment fragment = new ChildSettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("childSettingFragment", "onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Do not call super class method here.
        Log.i("childSettingFragment","onSaveInstanceState");
        outState.putParcelable("PARAMETER", childParameter);
        outState.putSerializable("INVALIDAREA",invalidArray);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            childParameter = (MainActivity.Parameter) savedInstanceState.getParcelable("PARAMETER");
            invalidArray = (ArrayList<Boolean>) savedInstanceState.getSerializable("INVALIDAREA");
        } else {
            Bundle bundle = getArguments();     //インテントでカスタムParameterクラスのオブジェクトを渡す
            childParameter = (MainActivity.Parameter) bundle.getParcelable("PARAMETER");
            invalidArray = (ArrayList<Boolean>) bundle.getSerializable("INVALIDAREA");
        }

        View v = inflater.inflate(R.layout.fragment_child_setting, null);
        context = v.getContext();
        v.setFocusableInTouchMode(true);
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    // 何らかの処理
                    Log.i("ChildSettingFragment", "keyCode :" + Integer.toString(keyCode));
                    removeThisFragment();
                    return true;
                }
//                else if ((keyCode == KeyEvent.KEYCODE_HOME) || (keyCode == KeyEvent.KEYCODE_APP_SWITCH && event.getAction() == KeyEvent.ACTION_UP)) {
//                    Log.i("ChildSettingFragment", "keyCode :"+Integer.toString(keyCode));
//                    removeThisFragment();
//                }
                return false;
            }
        });

        ArrayList<ArrayList<GeoPoint>> childAreaData = new ArrayList<>();

        for (int areaNum = 0; areaNum <= invalidArray.size() - 1; areaNum++) {
            if (invalidArray.get(areaNum) == false) {
                childAreaData.add(childParameter.getAreaData().get(areaNum));
            }
        }

        childParameter.areaData = childAreaData;
        childParameter.areaQty = childAreaData.size();

        buildWriteBuffer(childParameter);


        ImageButton backButton = v.findViewById(R.id.childSettingBackButton);
        backButton.setOnClickListener(this);

        sendProgress = new ProgressDialog(context);
        sendProgress.setMessage("設定送信中…");

        scanProgress = v.findViewById(R.id.scanProgress);
        scanProgress.setVisibility(View.GONE);

        scanButton = v.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BLE", "Button pushed.");
                scanLeDevice(true);
                scanProgress.setVisibility(View.VISIBLE);
            }
        });

        clearButton = v.findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScanning) {

                } else {
                    Log.i("BLE", "clear button pressed");
                    setFirstVisibleItemToAllItems(0);
                    addressList.clear();
                    itemList.clear();
                    adapter.notifyDataSetChanged();
                    checkedDeviceList.clear();
                    //multiButton.setEnabled(false);
                    //clearButton.setEnabled(false);
                    enableButtons(false, false, true, false);
                }
            }
        });
        clearButton.setEnabled(false);

        multiButton = v.findViewById(R.id.multiButton);
        multiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //setAllCheckedDevices();
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("別のスレッドだよ");
                        ArrayList<BluetoothDevice> temp = new ArrayList();
                        temp = (ArrayList<BluetoothDevice>) checkedDeviceList.clone();
                        setAllCheckedDevices(temp, false);//パラメータ設定無効
                    }
                }).start();
            }
        });

        final int MAX_CHILD_NUM = 10;

        checkAllButton = v.findViewById(R.id.checkAllButton);
        checkAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isAllChecked = false;
                for(int num = 0; num <= itemList.size()-1; num++) {
                    isAllChecked |= itemList.get(num).getIsChecked();
                }
                if (isAllChecked) {
                    for (int num = 0; num <= itemList.size() - 1; num++) {
                        itemList.get(num).setCheck(false);
                        checkedDeviceList.remove(itemList.get(num).getBluetoothDevice());
                    }
                } else {
                    for (int num = 0; num <= MAX_CHILD_NUM - 1 && num <= itemList.size() - 1; num++) {

                        itemList.get(num).setCheck(true);
                        checkedDeviceList.add(itemList.get(num).getBluetoothDevice());
                    }
                }
                updateListView();
            }
        });
        checkAllButton.setEnabled(false);

        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            removeThisFragment();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        adapter = new ScanResultListAdapter(getContext(), R.layout.scan_result_item, itemList);
        if (adapter.isEmpty()) {
            listView = v.findViewById(R.id.scanResultListView);
            //adapter = new ScanResultListAdapter(getContext(), R.layout.scan_result_item, itemList);
            Log.i("BLE", "Get adapter.");

            adapter.setCallback(scanResultListAdapterCallback);
            Log.i("BLE", "Set callback.");

            listView.setAdapter(adapter);
            Log.i("BLE", "Set adapter.");

            Log.i("BLE", "item size:" + Integer.toString(itemList.size()));
        }

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.i("onScroll", "firstVisibleItem[" + Integer.toString(firstVisibleItem) + "]");
                setFirstVisibleItemToAllItems(firstVisibleItem);
            }
        });

        return v;
    }
    private void setFirstVisibleItemToAllItems(Integer num) {
        for(int cnt = 0; cnt <= itemList.size()-1; cnt++) {
            itemList.get(cnt).setFirstVisibleItemNum(num);
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
        if (mScanning && bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(mLeScanCallback);
        }
        addressList.clear();
        itemList.clear();
        adapter.notifyDataSetChanged();
        checkedDeviceList.clear();
        multiButton.setEnabled(false);
        clearButton.setEnabled(false);
        removeThisFragment();
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("ChildSettingFragment", "onDestroy");
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

        if(gattWriter!=null)gattWriter.close();
        if(bluetoothGatt!=null)bluetoothGatt.close();
        if(mBluetoothGatt!=null)mBluetoothGatt.close();

        if(handler!=null)handler.removeCallbacks(r);
        mScanning=false;

        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right).remove(this).commit();
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    public void connectPressed(BluetoothDevice bluetoothDevice, Button connectButton, ProgressBar progressBar) {

    }

    public void settingPressed(BluetoothDevice bluetoothDevice, Button settingButton, ProgressBar progressBar) {

    }

    public void deviceChecked(BluetoothDevice bluetoothDevice, Boolean isChecked) {
    }

    public void setCallback(ChildSettingFragmentCallback callback) {
        this.callback = callback;
    }

    interface ChildSettingFragmentCallback {
        void connectDevice(BluetoothDevice bluetoothDevice);
    }

    private byte[] convertToBytes(Object object) throws IOException {
        Log.i("BYTE", "Byte convert start");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            Log.i("BYTE", bos.toString());
            return bos.toByteArray();
        } catch (IOException e) {
            Log.i("BYTE", e.toString());
            return null;
        }
    }

    private void buildWriteBuffer(MainActivity.Parameter parameter) {

        if (writeBuffer.size() != 0) {
            writeBuffer.clear();
        }

        byte[] type = {PARAMETER_USERID};
        byte[] send = SerializationUtils.serialize((Serializable) parameter.getUserID());
        byte[] checksum = {0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_GROUPID};
        send = SerializationUtils.serialize((Serializable) parameter.getGroupID());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

//        type = new byte[]{PARAMETER_GROUPID};
//        send = SerializationUtils.serialize((Serializable) parameter.getGroupID());
//        checksum = new byte[]{0x00};
//        for (int num = 0; num <= send.length - 1; num++) {
//            checksum[0] += send[num];
//        }
//        writeBuffer.add(concatByteArray(type,send,checksum));

        type = new byte[]{PARAMETER_ADMIN};
        send = SerializationUtils.serialize((Serializable) parameter.isAdmin());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_AREA_QTY};
        send = SerializationUtils.serialize((Serializable) parameter.getAreaQty());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        for (int areaNum = 0; areaNum <= parameter.getAreaQty() - 1; areaNum++) {
            type = new byte[]{PARAMETER_AREA_DATA};
            send = SerializationUtils.serialize((Serializable) parameter.getAreaData().get(areaNum));
            checksum = new byte[]{0x00};
            for (int num = 0; num <= send.length - 1; num++) {
                checksum[0] += send[num];
            }
            writeBuffer.add(concatByteArray(type, send, checksum));
        }


        type = new byte[]{PARAMETER_ENTER_ALERT};
        send = SerializationUtils.serialize((Serializable) parameter.isEnterAlertOn());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_CLOSE_ALERT};
        send = SerializationUtils.serialize((Serializable) parameter.isCloseAlertOn());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_JUKI_ALERT};
        send = SerializationUtils.serialize((Serializable) parameter.isJukiAlertOn());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_VIBRATION};
        send = SerializationUtils.serialize((Serializable) parameter.isVibrationOn());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_CLOSE_VOLUME};
        send = SerializationUtils.serialize((Serializable) parameter.getCloseVolume());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_ENTER_VOLUME};
        send = SerializationUtils.serialize((Serializable) parameter.getEnterVolume());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_JUKI_VOLUME};
        send = SerializationUtils.serialize((Serializable) parameter.getJukiVolume());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_LOGGING};
        send = SerializationUtils.serialize((Serializable) parameter.isLoggingOn());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_START_HOUR};
        send = SerializationUtils.serialize((Serializable) parameter.getStartHour());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_START_MIN};
        send = SerializationUtils.serialize((Serializable) parameter.getStartMinute());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_END_HOUR};
        send = SerializationUtils.serialize((Serializable) parameter.getEndHour());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_END_MIN};
        send = SerializationUtils.serialize((Serializable) parameter.getEndMinute());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_CLOSE_DISTANCE};
        send = SerializationUtils.serialize((Serializable) parameter.getCloseDistance());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_JUKI_DISTANCE};
        send = SerializationUtils.serialize((Serializable) parameter.getJukiDistance());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_NORMAL_INTERVAL};
        send = SerializationUtils.serialize((Serializable) parameter.getNormalLogIntvl());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_SEMI_CLOSE_INTERVAL};
        send = SerializationUtils.serialize((Serializable) parameter.getSemiCloseLogIntvl());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_CLOSE_INTERVAL};
        send = SerializationUtils.serialize((Serializable) parameter.getCloseLogIntvl());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_ENTER_INTERVAL};
        send = SerializationUtils.serialize((Serializable) parameter.getEnterLogIntvl());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_JUKI_CLOSE_INTERVAL};
        send = SerializationUtils.serialize((Serializable) parameter.getJukiCloseLogIntvl());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_JUKI_QTY};
        send = SerializationUtils.serialize((Serializable) parameter.getJukiQty());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        type = new byte[]{PARAMETER_JUKI_DATA};
        send = SerializationUtils.serialize((Serializable) parameter.getJukiList());
        checksum = new byte[]{0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }
        writeBuffer.add(concatByteArray(type, send, checksum));

        writeBufferReady = true;
    }

    private byte[] concatByteArray(byte[] type, byte[] data, byte[] checksum) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(type);
            outputStream.write(data);
            outputStream.write(checksum);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytes = outputStream.toByteArray();
        return bytes;
    }

    private void sendParameter(byte PARAM_TYPE, Object data) {

        if (!DATA_SEND_READY) {
            return;
        }

        byte[] type = {PARAM_TYPE};

        byte[] send = SerializationUtils.serialize((Serializable) data);
        Object object = SerializationUtils.deserialize(send);

        byte checksum[] = {0x00};
        for (int num = 0; num <= send.length - 1; num++) {
            checksum[0] += send[num];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(type);
            outputStream.write(send);
            outputStream.write(checksum);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buffer = outputStream.toByteArray();

        for (int cnt = 0; cnt <= buffer.length - 1; cnt++) {
            Log.i("BLE", "count:" + Integer.toString(cnt) + ":" + Integer.toHexString(buffer[cnt]));
        }

        characteristic.setValue(buffer);

        if (gattWriter.writeCharacteristic(characteristic)) {
            Log.i("BLE", "writeCharacteristic returns true");

        } else {
            Log.i("BLE", "writeCharacteristic returns false");
        }
    }

    public class ConnectedChildData {
        private ArrayList<ChildData> childDataList;

        public class ChildData {
            private String connectedDate = "";
            private String uuid = "";
            private String userId = "";
            private String groupId = "";

            public ChildData(String connectedDate, String uuid, String userId, String groupId) {
                this.connectedDate = connectedDate;
                this.uuid = uuid;
                this.userId = userId;
                this.groupId = groupId;
            }
        }

        public ConnectedChildData() {
            childDataList = new ArrayList<>();
            if (isExistConnectedChildLogCsv(MainActivity.PATH_MAIN_DIRECTORY)) {
                childDataList = readConnectedChildLog(MainActivity.PATH_MAIN_DIRECTORY);
            }
        }

        private void addChildData(String connectedDate, String uuid, String userId, String groupId) {
            if (hasSpecifiedChildDataUsingUuid(uuid)) {  //既に登録されているUUIDなら
                replaceChildDataUsingUuid(uuid, new ChildData(connectedDate, uuid, userId, groupId));   //置き換える
            } else {
                this.childDataList.add(new ChildData(connectedDate, uuid, userId, groupId));
            }
        }

        private void removeAllChildData() {
            childDataList.clear();
        }

        private void outputConnectedChildLog(String path) {
            int listSize = childDataList.size() - 1;
            try {
                FileWriter fw = new FileWriter(path + "/" + "devices.csv", false);
                PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
                pw.print("date,uuid,userID,groupID");
                pw.println();
                for (int i = 0; i <= listSize; i++) {
                    String date = childDataList.get(i).connectedDate;
                    String uuid = childDataList.get(i).uuid;
                    String userId = childDataList.get(i).userId;
                    String groupId = childDataList.get(i).groupId;
                    pw.print(date + "," + uuid + "," + userId + "," + groupId);
                    pw.println();
                }
                pw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private ArrayList<ChildData> readConnectedChildLog(String path) {
            ArrayList<ChildData> readChildDataList = new ArrayList<>();
            ArrayList<String> readString = new ArrayList<>();

            try {
                FileInputStream fileInputStream = new FileInputStream(path + "/devices.csv");
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = "";
                line = reader.readLine();   //1行目は省く

                while ((line = reader.readLine()) != null) {
                    StringTokenizer stringTokenizer =
                            new StringTokenizer(line, ",");
                    //readChildDataList.add(new ChildData(stringTokenizer.nextToken(), stringTokenizer.nextToken(), stringTokenizer.nextToken(), stringTokenizer.nextToken()));
                    String str1 = stringTokenizer.nextToken();
                    String str2 = stringTokenizer.nextToken();
                    String str3 = stringTokenizer.nextToken();
                    String str4 = stringTokenizer.nextToken();
                    readChildDataList.add(new ChildData(str1, str2, str3, str4));

                }
                reader.close();
                inputStreamReader.close();
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return readChildDataList;
        }

        private boolean isExistConnectedChildLogCsv(String path) {
            File file = new File(path + "/devices.csv");
            return file.exists();
        }

        private boolean hasSpecifiedChildDataUsingUuid(String uuid) {
            for (int childNum = 0; childNum <= childDataList.size() - 1; childNum++) {
                if (childDataList.get(childNum).uuid.equals(uuid)) {
                    return true;
                }
            }
            return false;
        }

        private void replaceChildDataUsingUuid(String uuid, ChildData childData) {
            for (int childNum = 0; childNum <= childDataList.size() - 1; childNum++) {
                if (childDataList.get(childNum).uuid.equals(uuid)) {
                    childDataList.remove(childNum);
                    childDataList.add(childData);
                }
            }
        }

        private boolean hasSpecifiedChildLog(String userId) {
            return false;
        }

    }

    BluetoothGatt bluetoothGatt;
    Boolean next = false;

    ArrayList<byte[]> buffer = new ArrayList<>();
    int count = 0;

    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private View getCheckedDeviceView(BluetoothDevice device, ListView listView) {
        for (int num = 0; num <= itemList.size(); num++) {
            ScanResultListItem item = (ScanResultListItem) listView.getItemAtPosition(num);
            if (device.getAddress().equals(item.getDeviceAddress())) {
                return getViewByPosition(num, listView);
            }
        }
        return new View(getContext());
    }

    private void setProgressBarOnUiThread(final ProgressBar progressBar, final int progress, final Boolean animate) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress, animate);
            }
        });
    }

    private void setProgressBarVisibleOnUiThread(final ProgressBar progressBar, final Boolean visible) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (visible) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void enableAllConnectButton(Boolean enable) {
        for(int num = 0; num<=itemList.size()-1; num++) {
            itemList.get(num).setConnectEnable(enable);
        }
        updateListView();
    }

    private void enableButtons(final Boolean checkAllButtonEnable, final Boolean multiButtonEnable, final Boolean scanButtonEnable, final Boolean clearButtonEnable) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkAllButton.isEnabled() != checkAllButtonEnable) {
                    checkAllButton.setEnabled(!checkAllButton.isEnabled());
                }
                if (multiButton.isEnabled() != multiButtonEnable) {
                    multiButton.setEnabled(!multiButton.isEnabled());
                    enableAllConnectButton(multiButton.isEnabled());
                }
                if (scanButton.isEnabled() != scanButtonEnable) {
                    scanButton.setEnabled(!scanButton.isEnabled());
                }
                if (clearButton.isEnabled() != clearButtonEnable) {
                    clearButton.setEnabled(!clearButton.isEnabled());
                }
            }
        });
    }

    private ScanResultListItem getItemUsingDeviceAddress(BluetoothDevice device) {
        for (int num = 0; num <= itemList.size(); num++) {
            if (itemList.get(num).getDeviceAddress() == device.getAddress()) {
                return itemList.get(num);
            }
        }
        return null;
    }

    private void setAllCheckedDevices(final ArrayList<BluetoothDevice> devices, final Boolean enableParameter) {

        final int RETRY_TIMES_LIMIT = 1;    //失敗時リトライ回数
        int retryCounter = 0;

        enableButtons(false, false, false, false);
        isSettingCheckedDevices = true;
        for (int deviceNum = 0; deviceNum <= devices.size() - 1; deviceNum++) {
            View currentView = getCheckedDeviceView(devices.get(deviceNum), listView);

            final ProgressBar progressBar = currentView.findViewById(R.id.progressBar);
            progressBar.setMax(100);
            progressBar.setMin(0);
            setProgressBarVisibleOnUiThread(progressBar, true);
            setProgressBarOnUiThread(progressBar, 0, true);

            final int cnt = deviceNum;
            final TextView deviceStatusText = currentView.findViewById(R.id.deviceStatus);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getItemUsingDeviceAddress(devices.get(cnt)).setDeviceStatus("接続中...!");
                    updateListView();
                    //deviceStatusText.setText("接続中…");
                }
            });

            final TextView deviceName = currentView.findViewById(R.id.deviceName);

            Log.i("BLE", "===============loop[" + Integer.toString(deviceNum) + "]===============");
            Log.i("BLE", "setCheckedDevice => " + devices.get(deviceNum).toString());

            bluetoothGatt = devices.get(deviceNum).connectGatt(getContext(), false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, final int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i("BLE", "connected => " + gatt.getDevice().toString());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getItemUsingDeviceAddress(devices.get(cnt)).setDeviceStatus("接続");
                                updateListView();
                                //deviceStatusText.setText("接続");
                                //next = true;
                            }
                        });
                        CONNECTED = true;
                        gatt.requestMtu(512);
                        setProgressBarOnUiThread(progressBar, 10, true);
                    } else if (status == 133 || newState == BluetoothProfile.STATE_DISCONNECTED) {
                        if (status != 0) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getItemUsingDeviceAddress(devices.get(cnt)).setDeviceStatus("error[" + Integer.toString(status) + "]");
                                    updateListView();
                                    //deviceStatusText.setText("error[" + Integer.toString(status) + "]");
                                }
                            });
                        }
                        gatt.close();
                        //progressBar.setVisibility(View.GONE);
                        Log.i("BLE", "disconnected : [status=" + Integer.toString(status) + "],[newState=" + Integer.toString(newState) + "] => " + gatt.getDevice().toString());
                        CONNECTED = false;
                        next = true;
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    Log.i("BLE", "Service discovered =>" + gatt.getDevice().toString());
                    List<BluetoothGattService> serviceList;
                    serviceList = gatt.getServices();
                    ArrayList<String> uuidList = new ArrayList<>();
                    BluetoothGattCharacteristic targetCharacteristic;
                    setProgressBarOnUiThread(progressBar, 30, true);

                    for (int i = 0; i <= serviceList.size() - 1; i++) {
                        List<BluetoothGattCharacteristic> charaList;
                        charaList = serviceList.get(i).getCharacteristics();
                        for (int j = 0; j <= charaList.size() - 1; j++) {
                            Log.i("BLE", "characteristic >>> " + charaList.get(j).getUuid().toString() + " => " + gatt.getDevice().toString());
                            //if(charaList.get(j).getUuid().toString().equalsIgnoreCase(CHARACTERISTIC_UUID)){
                            String uuid = "";
                            uuid = charaList.get(j).getUuid().toString();
                            uuidList.add(uuid);
                            if (uuid.equals(CHARACTERISTIC_UUID)) {
                                Log.i("BLE", "characteristic found!");
                                connectedAddress = gatt.getDevice().getAddress();
                                sendParamData(gatt, charaList.get(j), enableParameter);
                            }
                        }
                        //gatt.disconnect();
                    }
                    if (!uuidList.contains(CHARACTERISTIC_UUID)) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getItemUsingDeviceAddress(devices.get(cnt)).setDeviceStatus("not target device");
                                updateListView();
                                //deviceStatusText.setText("not target device");
                            }
                        });
                        Log.i("BLE", "characteristic not found");
                        Log.i("BLE", "try to disconnect => " + gatt.getDevice().getAddress());
                        gatt.disconnect();
                    }
                }

                @Override
                public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                    super.onMtuChanged(gatt, mtu, status);
                    Log.i("BLE", "MTU size changed :" + Integer.toString(mtu) + " => " + gatt.getDevice().toString());
                    if (gatt.discoverServices()) {
                        Log.i("BLE", "Service discover started");
                    } else {
                        Log.i("BLE", "Service discover start failed");
                    }
                    setProgressBarOnUiThread(progressBar, 20, true);
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    Log.i("BLE", "onWriteCharacteristic => " + gatt.getDevice().getAddress());
                    if (count <= writeBuffer.size() - 1) {

                        characteristic.setValue(writeBuffer.get(count));
                        if (gatt.writeCharacteristic(characteristic)) {
                            setProgressBarOnUiThread(progressBar, 40 + 70 / writeBuffer.size() * count, true);
                            Log.i("BLE", "writeCharacteristic returns true" + "[count=" + Integer.toString(count) + "],[buffer size=" + Integer.toString(writeBuffer.size()) + "]");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getItemUsingDeviceAddress(devices.get(cnt)).setDeviceStatus("通信中");

                                    //deviceStatusText.setText("通信中");
                                }
                            });
                            if (count == writeBuffer.size() - 1) {
                                count = 0;
                                Log.i("BLE", "finish sending data");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getItemUsingDeviceAddress(devices.get(cnt)).setDeviceStatus("設定完了");

                                        //deviceStatusText.setText("設定完了");
                                        if (enableParameter) {
                                            deviceName.setText(childParameter.getUserID() + "," + childParameter.getGroupID());
                                        }
                                    }
                                });
                                gatt.disconnect();
                            }
                        } else {
                            Log.i("BLE", "writeCharacteristic returns false");
                        }
                        count++;
                    }
                }
            });
            while (next == false) {

            }

            if (CONNECTED == false && (retryCounter < RETRY_TIMES_LIMIT)) {
                deviceNum--;
                retryCounter++;
            } else {
                retryCounter = 0;
            }

            setProgressBarVisibleOnUiThread(progressBar, false);

            next = false;
        }
        Log.i("BLE", "setting finish");
        isSettingCheckedDevices = false;
        enableButtons(!itemList.isEmpty(), !checkedDeviceList.isEmpty(), true, true);
        enableAllConnectButton(true);
    }

    private void sendParamData(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, Boolean enableParameter) {

        String deviceName = gatt.getDevice().getName().toString();
        String userId = "";
        String groupId = "";

        if ((deviceName != null) && (!enableParameter)) {
            String[] str = deviceName.split(",");
            userId = str[1];
            groupId = str[0];
            childParameter.setUserID(userId);
            childParameter.setGroupID(groupId);
        }


        buildWriteBuffer(childParameter);
        for (int num = 0; num <= writeBuffer.size() - 1; num++) {
            Log.i("BLE", "write buffer : " + Integer.toString(num) + writeBuffer.get(num).toString());
        }

        Log.i("BLE", "CHARACTERISTIC :" + characteristic.getUuid().toString());
        count = 0;
        Log.i("BLE", "count reset" + Integer.toString(count));

        characteristic.setValue(writeBuffer.get(count));
        if (gatt.writeCharacteristic(characteristic)) {
            Log.i("BLE", "writeCharacteristic returns true" + "[count=" + Integer.toString(count) + "],[buffer size=" + Integer.toString(writeBuffer.size()) + "]");
            count++;
        } else {
            Log.i("BLE", "writeCharacteristic returns false");
        }

        for (int cnt = 0; cnt <= writeBuffer.size() - 1; cnt++) {
        }
    }

    private class CustomProgressBar {
        private View view;
        private ProgressBar progressBar;

        public void CustomProgressBar(View view) {
            progressBar = view.findViewById(R.id.progressBar);
            progressBar.setMax(100);
            progressBar.setMin(0);
        }

        private void setProgress(final ProgressBar progressBar, final int progress, final Boolean animate) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(progress, animate);
                }
            });
        }

        private void setVisible(Boolean enable) {
            if (enable) {
                this.progressBar.setVisibility(View.VISIBLE);
            } else {
                this.progressBar.setVisibility(View.INVISIBLE);
            }
        }


    }
}
