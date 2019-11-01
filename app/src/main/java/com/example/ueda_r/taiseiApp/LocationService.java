package com.example.ueda_r.taiseiApp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StatFs;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.ueda_r.osmdroidtest1022.R;
import com.github.lassana.osmdroid_shape_extension.ShapeAsPointsBuilder;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class LocationService extends Service implements LocationListener {
    private String loggingStartTimeString = "";

    public void setLoggingStartTimeString(String loggingStartTimeString) {
        this.loggingStartTimeString = loggingStartTimeString;
    }

    public String getLoggingStartTimeString() {
        return loggingStartTimeString;
    }

    private DataLogger beaconLogger = new DataLogger();

    GeoPoint lastPoint = new GeoPoint(0.0, 0.0);

    private LocationManager locationManager;
    private Context context;

    private static final int MinTime = 1 * 1000;
    private static final float MinDistance = 0;

    private static final int AlertInterval = 1000;

    private static final double RemainSizeMB = 10000;//50355.0;

    CustomDangerArea dangerArea;
    CustomParameter parameter;
    CustomLocationLogger logger;
    CustomLocationLogger locationLogger;

    Boolean isAreaWatchingStarted = false;
    Boolean isGpsStarted = false;
    Handler handler;
    Runnable r;

    private int intervalCounter = 0;
    private LocationStatus lastStatus = LocationStatus.OUTSIDE;
    private LocationStatus currentStatus = LocationStatus.OUTSIDE;
    final static int COUNTER_THRESHOLD = 0;

    private double lastGpsChangeTimeMillis = 0.0;

    private AudioAttributes audioAttributes;
    private SoundPool soundPool;
    private int soundPi;
    private boolean isAlertLoaded = false;

    public interface LocationServiceCallback{
        void showAlertMessage(Boolean enable, String message);
    }
    private boolean hasAlertShown = false;

    private LocationServiceCallback serviceCallback;

    public void setCallback(LocationServiceCallback callback) {
        serviceCallback = callback;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Toast.makeText(context, "LocationService running", Toast.LENGTH_SHORT).show();

        parameter = new CustomParameter();
        parameter.readParameter(MainActivity.PATH_PARAMETER);

        dangerArea = new CustomDangerArea();
        dangerArea.areaDataArray.clear();
        for (int num = 0; num <= parameter.areaQty - 1; num++) {
            dangerArea.addAreaData(parameter.areaData.get(num));
            Log.i("LocationService", "AreaData Loaded (" + Integer.toString(num) + ") : " + parameter.areaData.get(num).toString());
        }
        logger = new CustomLocationLogger();
        beaconLogger.addStringLine("Date" + "," + "beacon lat" + "," + "beacon lon" + "," + "gps lat" + "," + "gps long" + "," + "distance[m]" + "," + "distance(rssi)[m]");

        locationLogger = new CustomLocationLogger(dangerArea, parameter);

    }

    private int getAvailableStrageMB() {
        StatFs statFs = new StatFs(MainActivity.PATH_MAIN_DIRECTORY);
        long blockSize = statFs.getBlockSizeLong();
        long availableBlockSize = statFs.getAvailableBlocksLong();
        int freeSize = (int) (blockSize * availableBlockSize / 1024 / 1024);
        Log.d("LocationService", "Available Storage = [" + (freeSize) + "]" + ", Remain Storage = [" + (RemainSizeMB) + "]");
        return freeSize;
    }

    private double getAvailableStorageMB() {
        StatFs statFs = new StatFs(MainActivity.PATH_MAIN_DIRECTORY);
        long blockSize = statFs.getBlockSizeLong();
        long availableBlockSize = statFs.getAvailableBlocksLong();
        double freeSize = (double) (blockSize * availableBlockSize / 1024.0 / 1024.0);
        if (serviceCallback != null) {
            //serviceCallback.showAlertMessage(true, "Available Storage = [" + (freeSize) + "]" + ", \r\nRemain Storage = [" + (RemainSizeMB) + "]");
        }
        Log.d("LocationService", "Available Storage = [" + (freeSize) + "]" + ", Remain Storage = [" + (RemainSizeMB) + "]");
        return freeSize;
    }

    private void removeOldestLogData() {

        String oldestFilename = "";
        long oldestDate = 0;

        List<File> fileList = new ArrayList<>(Arrays.asList(new File(MainActivity.PATH_MAIN_DIRECTORY).listFiles()));
        List<File> logFileList = new ArrayList<>();

//        List<File> list = new ArrayList<>();
//        for (int num = 0; num <= logFileList.size() - 1; num++) {
//            String filename = logFileList.get(num).getName();
//            if (!filename.contains("parameter.csv") && !filename.contains(".area") && !filename.contains("maps")) {
//                list.add(logFileList.get(num));
//            }
//        }
//        if (list.size() <= 1) {
//            Log.d("LocationService", "stop to remove the old file");
//            return;
//        }

        for (int num = 0; num <= fileList.size() - 1; num++) {
            String filename = fileList.get(num).getName();
            if (!filename.contains("parameter.csv") && !filename.contains(".area") && !filename.contains("maps")) {   //履歴データない場合はここで弾かれる & oldestDate != 0 で弾かれる
                logFileList.add(fileList.get(num));
                String dateString = filename.substring(filename.length() - 16);
                dateString = dateString.substring(0, 12);
                try {
                    long date = Long.parseLong(dateString);
                    if (oldestDate == 0 || date < oldestDate) {
                        oldestDate = date;
                        oldestFilename = fileList.get(num).getName();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

            }
        }

        if (oldestDate != 0) {
            String deleteCmd = "rm -r " + MainActivity.PATH_MAIN_DIRECTORY + "/" + oldestFilename;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
                Log.d("LocationService", "old file removed : " + oldestFilename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private Boolean isWorkingTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);

        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        double currentTime = currentHour + (double) currentMinute / 60;
        double startTime = parameter.startHour + (double) parameter.startMinute / 60;

        double startLunchTime = parameter.startLunchHour + (double) parameter.startLunchMinute / 60;
        double endLunchTime = parameter.endLunchHour + (double) parameter.endLunchMinute / 60;

        double endTime = parameter.endHour + (double) parameter.endMinute / 60;
        Log.i("LocationService", "Is it working time now?");

        Log.i("LocationService", "Current Time => " + (currentTime));
        Log.i("LocationService", "Start Time => " + (startTime));
        Log.i("LocationService", "Start Lunch Time => " + (startLunchTime));
        Log.i("LocationService", "End Lunch Time => " + (endLunchTime));
        Log.i("LocationService", "End Time => " + (endTime));

        if (((currentTime >= startTime) && (currentTime <= startLunchTime)) || ((currentTime >= endLunchTime) && (currentTime <= endTime))) {
            Log.i("LocationService", "It is working time now");
            return true;
        } else {
            Log.i("LocationService", "It is Not working time now");
        }
        return false;
    }

    public double getBatteryPercent() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        if (isCharging) {
            //return 1.0;
        }

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = (level / (float) scale);
        Log.d("LocationService", "battery : " + batteryPct * 100 + " %");
        return batteryPct * 100;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocationService", "startCommand");

        if (intent.getAction().equals("START_FOREGROUND_ACTION")) {
            Log.i("LocationService", "START_FOREGROUNG_ACTION");
            int requestCode = 0;
            String channelId = "default";
            String title = context.getString(R.string.app_name);

            PendingIntent pendingIntent =
                    PendingIntent.getActivity(context, requestCode,
                            intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // ForegroundにするためNotificationが必要、Contextを設定
            NotificationManager notificationManager =
                    (NotificationManager) context.
                            getSystemService(Context.NOTIFICATION_SERVICE);

            // Notification　Channel 設定
            NotificationChannel channel = new NotificationChannel(
                    channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Silent Notification");
            // 通知音を消さないと毎回通知音が出てしまう
            // この辺りの設定はcleanにしてから変更
            channel.setSound(null, null);
            // 通知ランプを消す
            channel.enableLights(false);
            channel.setLightColor(Color.BLUE);
            // 通知バイブレーション無し
            channel.enableVibration(false);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(context, channelId)
                        .setContentTitle(title)
                        // 本来なら衛星のアイコンですがandroid標準アイコンを設定
                        .setSmallIcon(android.R.drawable.btn_star)
                        .setContentText("GPS")
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .build();

                // startForeground
                startForeground(1, notification);

                initScan();

                handler = new Handler();
                r = new Runnable() {
                    int count = 0;

                    @Override
                    public void run() {
                        count++;
                        if (count > 10) {
                            //return;
                        }
                        if (isWorkingTime() && (getBatteryPercent() >= 5.0)) {
                            startGPS();
                            //startScan();
                            if (getLoggingStartTimeString() == "") {
                                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);
                                SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
                                String formatDate = format.format(calendar.getTime());
                                setLoggingStartTimeString(formatDate); //履歴保存開始の時刻
                            }
                        } else {
                            stopGPS();
                            //stopScan();
                            if (getLoggingStartTimeString() != "") {
                                setLoggingStartTimeString("");
                            }
                        }
                        if (serviceCallback != null) {
                            double battery = getBatteryPercent();
                            battery = MainActivity.getRoundedDouble(battery, 1);
                            if ((battery < 30) && (!hasAlertShown)) {
                                serviceCallback.showAlertMessage(true, "バッテリー残量低下 : " + (int) battery + "%");
                                hasAlertShown = true;
                            } else if (battery >= 30 && hasAlertShown) {    //アラート表示後に充電してバッテリー容量が復活したらその後にバッテリー減った時にアラート表示する
                                hasAlertShown = false;
                            }
                            //serviceCallback.showAlertMessage(true, "tick(" + count + ")" + "\r\nbattery : " + battery * 100.0);
                        }

                        Log.i("LocationService:", "tick(" + Integer.toString(count) + ")");
                        Log.i("LocationService", "danger area qty:" + Integer.toString(dangerArea.areaDataArray.size()));
                        handler.postDelayed(this, 10000);
                    }
                };
                handler.post(r);

                /*******************************************************************/
                if (false) {
                    final GeoPoint testPoint = new GeoPoint(35.7100625, 139.8107028);
                    final Handler testHandler = new Handler();
                    Runnable testRunnable = new Runnable() {
                        double cnt = 0;
                        @Override
                        public void run() {
                            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);
                            locationLogger.appendLog(calendar, testPoint, LocationStatus.OUTSIDE, MainActivity.PATH_MAIN_DIRECTORY, "battery_test" + locationLogger.createdTime + ".csv");
                            Log.d("TEST", Double.toString(cnt));
                            cnt++;
                            testHandler.postDelayed(this, 10);
                        }
                    };
                    testHandler.post(testRunnable);
                }
                /*******************************************************************/
            }
            loadAlert();

            Log.i("LocationService", "START_STICKY");
            return START_REDELIVER_INTENT;

        } else if (intent.getAction().equals("STOP_FOREGROUND_ACTION")) {
            Log.i("LocationService", "STOP_FOREGROUND_ACTION");
            handler.removeCallbacks(r);
            stopGPS();
            stopForeground(true);
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    protected void startGPS() {
        if (!isGpsStarted) {
            isGpsStarted = true;
            Log.i("LocationService", "startGPS");

            final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gpsEnabled) {
                enableLocationSettings();
            }

            if (locationManager != null) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MinTime, MinDistance, this);
                    Log.i("LocationService", "location update requested");
                    Log.i("LocationService", "MinTime = " + Integer.toString(MinTime) + ", MinDistance = " + Float.toString(MinDistance));

                } catch (Exception e) {
                    Log.i("LocationService", e.toString());
                    e.printStackTrace();
                }
            } else {
                Log.i("LocationService", "LocationManager is null");
            }
        } else {
            Log.i("LocationService", "GPS is already started");
        }
    }

    private void stopGPS() {
        if (isGpsStarted) {
            isGpsStarted = false;
            Log.i("LocationService", "stopGPS");

            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.removeUpdates(this);
            }
        } else {
            Log.i("LocationService", "GPS is already stopped");
        }
    }

    private void changeGPS(int MinTime, float MinDistance) {
        if (locationManager != null) {
            stopGPS();
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                //locationManager.removeUpdates(this);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MinTime * 1000, MinDistance, this);
                Log.i("LocationService", "location update requested");
                Log.i("LocationService", "MinTime = " + Integer.toHexString(MinTime * 1000) + "[ms], MinDistance = " + Float.toString(MinDistance) + "[m]");

            } catch (Exception e) {
                Log.i("LocationService", e.toString());
                e.printStackTrace();
            }
        } else {
            Log.i("LocationService", "LocationManager is null");
        }
    }

    private boolean hasEnoughInterval(LocationStatus status) {
        boolean bool = false;
        double currentTimeMillis = System.currentTimeMillis();
        double duration = currentTimeMillis - lastGpsChangeTimeMillis;
        if (lastGpsChangeTimeMillis == 0.0) {
            status = LocationStatus.OUTSIDE;
            duration = 100 * 1000;
        }
        switch (status) {
            case OUTSIDE:
                if (duration >= parameter.normalLogIntvl * 1000) {
                    bool = true;
                    lastGpsChangeTimeMillis = currentTimeMillis;
                }
                break;
            case SEMI_CLOSE:
                if (duration >= parameter.semiCloseLogIntvl * 1000) {
                    bool = true;
                    lastGpsChangeTimeMillis = currentTimeMillis;
                }
                break;
            case CLOSE:
                if (duration >= parameter.closeLogIntvl * 1000) {

                    bool = true;
                    lastGpsChangeTimeMillis = currentTimeMillis;
                }
                break;
            case INSIDE:
                if (duration >= parameter.enterLogIntvl * 1000) {

                    bool = true;
                    lastGpsChangeTimeMillis = currentTimeMillis;
                }
                break;
        }
        return bool;
    }

    private void intervalManager(LocationStatus status) {
        if ((status != currentStatus) && (status == lastStatus)) {
            intervalCounter++;
            Log.i("LocationService", "interval counter :" + Integer.toString(intervalCounter));
        } else {
            lastStatus = status;
            intervalCounter = 0;
            return;
        }
        if (intervalCounter >= COUNTER_THRESHOLD) {
            currentStatus = status;
            switch (status) {
                case OUTSIDE:
                    serviceCallback.showAlertMessage(true,"OUTSIDE");
                    Log.i("LocationService", "status changed => OUTSIDE");
                    changeGPS(parameter.normalLogIntvl, 1);
                    break;
                case SEMI_CLOSE:
                    serviceCallback.showAlertMessage(true,"SEMI");
                    Log.i("LocationService", "status changed => SEMI-CLOSE");
                    changeGPS(parameter.semiCloseLogIntvl, 1);
                    break;
                case CLOSE:
                    serviceCallback.showAlertMessage(true,"CLOSE");
                    Log.i("LocationService", "status changed => CLOSE");
                    changeGPS(parameter.closeLogIntvl, 1);
                    break;
                case INSIDE:
                    serviceCallback.showAlertMessage(true,"INSIDE");
                    Log.i("LocationService", "status changed => INSIDE");
                    changeGPS(parameter.enterLogIntvl, 1);
                    break;
                default:
                    break;
            }
            intervalCounter = 0;
        }
    }

    private void enableLocationSettings() {
        Log.i("LocationService", "enableLocationSettings");
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    private void vibrate(int milliSecond, int power) {
        if (!parameter.vibrationOn || !isAreaWatchingStarted) {
            return;
        }
        if (!(power > 255 || power < 1) || (milliSecond <= 0)) {
            Log.i("LocationService", "Invalid alert parameter");
        }
        Log.i("LocationService", "****!!!Alert Called!!!****");
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibrationEffect = VibrationEffect.createOneShot(milliSecond, power);

        vibrator.vibrate(vibrationEffect);
    }

    private void loadAlert() {
        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(1)
                .build();
        soundPi = soundPool.load(this, R.raw.pi, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                isAlertLoaded = true;
                Log.i("Alert", "sampleId" + sampleId);
                Log.i("Alert", "status" + status);
            }
        });
    }

    private Long alertTime = 0L;

    private void playAlert(float volume) {
        if (!parameter.vibrationOn || !isAreaWatchingStarted) {
            return;
        }
        if (!isAlertLoaded) {
            return;
        }
        if ((alertTime == 0L) || (System.currentTimeMillis() - alertTime >= AlertInterval)) {   //前回鳴らした時間から一定時間経っていたら鳴らす
            soundPool.play(soundPi, volume, volume, 0, 3, 1);
            Log.i("Alert", "Alert");
            alertTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (MainActivity.USB_ONLY_OUTPUT) {
            //return;
        }
        Date d = new Date(location.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yy");
        String sDate = sdf.format(d);
        Log.i("LocationService", "[" + sDate + "]");

        //小数点以下7桁で取得
        lastPoint = new GeoPoint(MainActivity.getRoundedDouble(location.getLatitude(), 7),
                MainActivity.getRoundedDouble(location.getLongitude(), 7));

        lastPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);

        Log.i("LocationService", lastPoint.toString());
        LocationStatus status;
        Integer areaNum = 99;
        if (dangerArea.isInsideDangerArea(lastPoint) != null) {
            status = LocationStatus.INSIDE;
            Log.i("LocationService", "Status INSIDE");

            vibrate(300, 255);
            float volume = parameter.enterVolume / 10.0f;
            playAlert(volume);
            areaNum = dangerArea.isInsideDangerArea(lastPoint);

            logger.addLocationLog(calendar, lastPoint, status, areaNum);
            //locationLogger.addLog(calendar, lastPoint, status);
            if (hasEnoughInterval(status)) {
                Toast.makeText(context, "Inside", Toast.LENGTH_SHORT).show();

                locationLogger.appendLog(calendar, lastPoint, status, MainActivity.PATH_MAIN_DIRECTORY, parameter.groupID + "_" + parameter.userID + "_" + locationLogger.createdTime + ".csv");
            }
            } else if (dangerArea.isInsideExpandedDangerArea(lastPoint) != null) {
                status = LocationStatus.CLOSE;
                Log.i("LocationService", "Status CLOSE");
                areaNum = dangerArea.isInsideExpandedDangerArea(lastPoint);

                logger.addLocationLog(calendar, lastPoint, status, areaNum);
                //locationLogger.addLog(calendar, lastPoint, status);
                if (hasEnoughInterval(status)) {
                    Toast.makeText(context, "Inside Expanded", Toast.LENGTH_SHORT).show();

                    locationLogger.appendLog(calendar, lastPoint, status, MainActivity.PATH_MAIN_DIRECTORY, parameter.groupID + "_" + parameter.userID + "_" + locationLogger.createdTime + ".csv");
                }

            } else if (dangerArea.isInsideSemiExpandedDangerArea(lastPoint) != null) {
                status = LocationStatus.SEMI_CLOSE;
                Log.i("LocationService", "Status Semi-CLOSE");
                areaNum = dangerArea.isInsideSemiExpandedDangerArea(lastPoint);
                logger.addLocationLog(calendar, lastPoint, status, areaNum);
                //locationLogger.addLog(calendar, lastPoint, status);
                if (hasEnoughInterval(status)) {
                    Toast.makeText(context, "Inside Semi-Expanded", Toast.LENGTH_SHORT).show();

                    locationLogger.appendLog(calendar, lastPoint, status, MainActivity.PATH_MAIN_DIRECTORY, parameter.groupID + "_" + parameter.userID + "_" + locationLogger.createdTime + ".csv");
                }
            } else {
                status = LocationStatus.OUTSIDE;
                Log.i("LocationService", "Status OUTSIDE");
                logger.addLocationLog(calendar, lastPoint, status, 99);
                //locationLogger.addLog(calendar, lastPoint, status);
                if (hasEnoughInterval(status)) {
                    Toast.makeText(context, "Outside", Toast.LENGTH_SHORT).show();

                    locationLogger.appendLog(calendar, lastPoint, status, MainActivity.PATH_MAIN_DIRECTORY, parameter.groupID + "_" + parameter.userID + "_" + locationLogger.createdTime + ".csv");
                }
            }
        //intervalManager(status);
    }

    public void gpsChanged(Location location) {
        Date d = new Date(location.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yy");
        String sDate = sdf.format(d);
        Log.i("LocationService", "[" + sDate + "]");

        //小数点以下7桁で取得
        lastPoint = new GeoPoint(MainActivity.getRoundedDouble(location.getLatitude(), 7),
                MainActivity.getRoundedDouble(location.getLongitude(), 7));

        lastPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);

        Log.i("LocationService", lastPoint.toString());
        LocationStatus status;
        Integer areaNum = 99;
        if (dangerArea.isInsideDangerArea(lastPoint) != null) {
            status = LocationStatus.INSIDE;
            Toast.makeText(context, "Inside", Toast.LENGTH_SHORT).show();
            Log.i("LocationService", "Status INSIDE");

            vibrate(300, 255);
            float volume = parameter.enterVolume / 10.0f;
            playAlert(volume);
            areaNum = dangerArea.isInsideDangerArea(lastPoint);

            logger.addLocationLog(calendar, lastPoint, status, areaNum);
            //locationLogger.addLog(calendar, lastPoint, status);
            locationLogger.appendLog(calendar, lastPoint, status, MainActivity.PATH_MAIN_DIRECTORY, parameter.groupID + "_" + parameter.userID + "_" + locationLogger.createdTime + ".csv");

        } else if (dangerArea.isInsideExpandedDangerArea(lastPoint) != null) {
            status = LocationStatus.CLOSE;
            Toast.makeText(context, "Inside Expanded", Toast.LENGTH_SHORT).show();
            Log.i("LocationService", "Status CLOSE");
            areaNum = dangerArea.isInsideExpandedDangerArea(lastPoint);

            logger.addLocationLog(calendar, lastPoint, status, areaNum);
            //locationLogger.addLog(calendar, lastPoint, status);
            locationLogger.appendLog(calendar, lastPoint, status, MainActivity.PATH_MAIN_DIRECTORY, parameter.groupID + "_" + parameter.userID + "_" + locationLogger.createdTime + ".csv");


        } else if (dangerArea.isInsideSemiExpandedDangerArea(lastPoint) != null) {
            status = LocationStatus.SEMI_CLOSE;
            Toast.makeText(context, "Inside Semi-Expanded", Toast.LENGTH_SHORT).show();
            Log.i("LocationService", "Status Semi-CLOSE");
            areaNum = dangerArea.isInsideSemiExpandedDangerArea(lastPoint);
            logger.addLocationLog(calendar, lastPoint, status, areaNum);
            //locationLogger.addLog(calendar, lastPoint, status);
            locationLogger.appendLog(calendar, lastPoint, status, MainActivity.PATH_MAIN_DIRECTORY, parameter.groupID + "_" + parameter.userID + "_" + locationLogger.createdTime + ".csv");

        } else {
            status = LocationStatus.OUTSIDE;
            Toast.makeText(context, "Outside", Toast.LENGTH_SHORT).show();
            Log.i("LocationService", "Status OUTSIDE");
            logger.addLocationLog(calendar, lastPoint, status, 99);
            //locationLogger.addLog(calendar, lastPoint, status);
            locationLogger.appendLog(calendar, lastPoint, status, MainActivity.PATH_MAIN_DIRECTORY, parameter.groupID + "_" + parameter.userID + "_" + locationLogger.createdTime + ".csv");
        }

        DateFormat df = new SimpleDateFormat("yyMMdd");
        Date date = new Date(System.currentTimeMillis());
        String ymd = df.format(date);
        if (parameter.loggingOn) {

            int year = logger.logList.get(0).locationDate.year - 2000;
            String yearStr = Integer.toString(year);

            int month = logger.logList.get(0).locationDate.month;
            String monthStr = Integer.toString(month);
            if (month <= 9) {
                monthStr = "0" + monthStr;
            }

            int day = logger.logList.get(0).locationDate.day;
            String dayStr = Integer.toString(day);
            if (day <= 9) {
                dayStr = "0" + dayStr;
            }

            int hour = logger.logList.get(0).locationDate.hour;
            String hourStr = Integer.toString(hour);
            if (hour <= 9) {
                hourStr = "0" + hourStr;
            }

            int minute = logger.logList.get(0).locationDate.minute;
            String minuteStr = Integer.toString(minute);

            int second = logger.logList.get(0).locationDate.second;
            String secondStr = Integer.toString(second);
            if (second <= 9) {
                secondStr = "0" + secondStr;
            }

            String dateString = yearStr + monthStr + dayStr + hourStr + minuteStr + secondStr;

            dateString = getLoggingStartTimeString();//履歴保存開始時の時刻を使う

            //logger.outputCsvLog(MainActivity.PATH_MAIN_DIRECTORY, parameter.groupID + "_" + parameter.userID + "_" + dateString + ".log");
            //dangerArea.outputAreaData(MainActivity.PATH_MAIN_DIRECTORY, parameter.groupID + "_" + parameter.userID + "_" + dateString + ".area");

            //locationLogger.outputLog(MainActivity.PATH_MAIN_DIRECTORY, parameter.groupID + "_" + parameter.userID + "_" + locationLogger.createdTime + ".csv");
        }
        intervalManager(status);
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("LocationService", "onStatusChanged");

        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.i("LocationService", "=> AVAILABLE");

                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.i("LocationService", "=> OUT_OF_SERVICE");

                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.i("LocationService", "=> UNAVAILABLE");

                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("LocationService", "onDestroy");
        handler.removeCallbacks(r);
        vibrate(500, 255);
        stopGPS();
        stopScan();

        soundPool.unload(soundPi);
        soundPool.release();
        isAlertLoaded = false;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    private class CustomParameter {
        private String userID = "user";  //ユーザーID
        private String groupID = "group";    //グループID

        private boolean admin = false;

        private int areaQty = 0;    //禁止領域数
        private ArrayList<ArrayList<GeoPoint>> areaData;    //禁止領域データ
        private boolean enterAlert = true;  //進入時アラートオン
        private boolean closeAlert = true;  //接近時アラートオン
        private boolean jukiAlert = true;   //重機接近時アラートオン
        private boolean vibrationOn = true; //アラート時振動オン
        private int closeVolume = 10;    //アラート音量
        private int enterVolume = 10;
        private int jukiVolume = 10;
        private boolean loggingOn = true;   //記録オン

        private int startHour = 9;  //開始時刻
        private int startMinute = 0;

        private int startLunchHour = 12;
        private int startLunchMinute = 0;

        private int endLunchHour = 13;
        private int endLunchMinute = 0;

        private int endHour = 18;   //終了時刻
        private int endMinute = 0;

        private int closeDistance = 10;
        private int jukiDistance = 10;

        private int normalLogIntvl = 5;    //通常時記録間隔
        private int semiCloseLogIntvl = 3; //準接近時記録間隔
        private int closeLogIntvl = 1;  //接近時記録間隔
        private int enterLogIntvl = 1;  //進入時記録間隔
        private int jukiCloseLogIntvl = 3;  //重機接近時記録間隔
        private int jukiQty = 0;    //重機数
        private ArrayList<String> jukiList; //重機データ

        public CustomParameter() {
            this.areaData = new ArrayList<>();
            this.jukiList = new ArrayList<>();
        }

        private void readParameter(String path) {
            ArrayList<String> titleList = new ArrayList<>();
            ArrayList<String> dataList = new ArrayList<>();

            int nowListNum = 0;

            try {
                FileInputStream fileInputStream = new FileInputStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = "";


                while ((line = reader.readLine()) != null) {
                    StringTokenizer stringTokenizer =
                            new StringTokenizer(line, ",");

                    titleList.add(stringTokenizer.nextToken());
                    dataList.add(stringTokenizer.nextToken());
                }
                reader.close();
                inputStreamReader.close();
                fileInputStream.close();


                this.userID = dataList.get(0);
                nowListNum++;

                this.groupID = dataList.get(1);
                nowListNum++;

                this.admin = Boolean.parseBoolean(dataList.get(2));
                nowListNum++;

                this.areaQty = Integer.parseInt(dataList.get(3));
                nowListNum++;

                ArrayList<Integer> coordQtyList = new ArrayList<>();
                for (int areaNum = 1; areaNum <= areaQty; areaNum++) {
                    String qty = dataList.get(nowListNum);
                    nowListNum++;
                    coordQtyList.add(Integer.parseInt(qty));
                }
                ArrayList<ArrayList<GeoPoint>> areaData = new ArrayList<>();
                ArrayList<GeoPoint> pointlist = new ArrayList<>();
                for (int areaNum = 1; areaNum <= areaQty; areaNum++) {
                    int coordQty = coordQtyList.get(areaNum - 1);
                    for (int coordNum = 1; coordNum <= coordQty; coordNum++) {
                        String coordStr = dataList.get(nowListNum);
                        nowListNum++;
                        StringTokenizer stringTokenizer = new StringTokenizer(coordStr, "/");
                        GeoPoint point = new GeoPoint(
                                Double.parseDouble(stringTokenizer.nextToken()),
                                Double.parseDouble(stringTokenizer.nextToken())
                        );
                        pointlist.add(new GeoPoint(point));
                    }
                    areaData.add(new ArrayList<GeoPoint>(pointlist));
                    //Log.i("LocationService", "Current area :" + areaData.toString());
                    pointlist.clear();
                }
                this.areaData = areaData;

                this.enterAlert = Boolean.parseBoolean(dataList.get(nowListNum));
                nowListNum++;

                this.closeAlert = Boolean.parseBoolean(dataList.get(nowListNum));
                nowListNum++;

                this.jukiAlert = Boolean.parseBoolean(dataList.get(nowListNum));
                nowListNum++;

                this.vibrationOn = Boolean.parseBoolean(dataList.get(nowListNum));
                nowListNum++;

                this.closeVolume = Integer.parseInt(dataList.get(nowListNum));
                nowListNum++;

                this.enterVolume = Integer.parseInt(dataList.get(nowListNum));
                nowListNum++;

                this.jukiVolume = Integer.parseInt(dataList.get(nowListNum));
                nowListNum++;

                this.loggingOn = Boolean.parseBoolean(dataList.get(nowListNum));
                nowListNum++;

                String time = dataList.get(nowListNum);
                StringTokenizer stringTokenizer = new StringTokenizer(time, ":");
                this.startHour = Integer.parseInt(stringTokenizer.nextToken());
                this.startMinute = Integer.parseInt(stringTokenizer.nextToken());
                nowListNum++;

                time = dataList.get(nowListNum);
                stringTokenizer = new StringTokenizer(time, ":");
                startLunchHour = Integer.parseInt(stringTokenizer.nextToken());
                startLunchMinute = Integer.parseInt(stringTokenizer.nextToken());
                nowListNum++;

                time = dataList.get(nowListNum);
                stringTokenizer = new StringTokenizer(time, ":");
                endLunchHour = Integer.parseInt(stringTokenizer.nextToken());
                endLunchMinute = Integer.parseInt(stringTokenizer.nextToken());
                nowListNum++;

                time = dataList.get(nowListNum);
                stringTokenizer = new StringTokenizer(time, ":");
                this.endHour = Integer.parseInt(stringTokenizer.nextToken());
                this.endMinute = Integer.parseInt(stringTokenizer.nextToken());
                nowListNum++;

                this.closeDistance = Integer.parseInt(dataList.get(nowListNum));
                nowListNum++;

                this.jukiDistance = Integer.parseInt(dataList.get(nowListNum));
                nowListNum++;

                this.enterLogIntvl = Integer.parseInt(dataList.get(nowListNum));
                nowListNum++;

                this.closeLogIntvl = Integer.parseInt(dataList.get(nowListNum));
                nowListNum++;

                this.semiCloseLogIntvl = Integer.parseInt(dataList.get(nowListNum));
                nowListNum++;

                this.jukiCloseLogIntvl = Integer.parseInt(dataList.get(nowListNum));
                nowListNum++;

                this.normalLogIntvl = Integer.parseInt(dataList.get(nowListNum));
                nowListNum++;

                ArrayList<String> jukiList = new ArrayList<>();
                this.jukiQty = Integer.parseInt(dataList.get(nowListNum));

                nowListNum++;
                for (int jukiNum = 1; jukiNum <= jukiQty; jukiNum++) {
                    jukiList.add(dataList.get(nowListNum));
                    nowListNum++;
                }
                this.jukiList = jukiList;


            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    private class CustomDangerArea {

        private ArrayList<AreaData> areaDataArray;

        public class AreaData {
            private Polygon areaPolygon;
            private Polygon expandedPolygon;
            private Polygon semiExpandedPolygon;

            public AreaData(Polygon polygon) {
                areaPolygon = polygon;
                expandedPolygon = getFixedExpandedPolygon(polygon, parameter.closeDistance);
                semiExpandedPolygon = getFixedExpandedPolygon(polygon, 20);
            }

        }

        public CustomDangerArea() {
            areaDataArray = new ArrayList<>();
        }

        private void addAreaData(ArrayList<GeoPoint> points) {
            Polygon polygon = new Polygon();
            Log.i("LocationService", "Polygon *** " + polygon.toString());
            polygon.setPoints(points);
            Log.i("LocationService", "Polygon *** " + polygon.toString());

            areaDataArray.add(new AreaData(polygon));
        }

        private Polygon getFixedExpandedPolygon(Polygon polygon, double mergin) {
            Polygon exPolygon = new Polygon();
            ArrayList<GeoPoint> points = (ArrayList<GeoPoint>) polygon.getPoints();     //拡張された領域の各頂点(激うま
            ArrayList<GeoPoint> defaultPoints = new ArrayList<GeoPoint>(points);        //拡張される前の各頂点

            ArrayList<GeoPoint> outline1 = getExpandedPointArray(points, mergin);    //右or左回転方向で拡張

            Collections.reverse(points);

            ArrayList<GeoPoint> outline2 = getExpandedPointArray(points, mergin);    //逆方向で拡張

            double s1 = findAreaSize(outline1); //面積計算
            double s2 = findAreaSize(outline2); //面積計算

            //面積が広い方の領域を採用
            if (s1 > s2) {
                exPolygon = getFixedPolygon(outline1, defaultPoints, MainActivity.Clockwise.RIGHT);  //自己交差を修復して角をまるめる
                //exPolygon.setPoints(outline1);
            } else {
                exPolygon = getFixedPolygon(outline2, defaultPoints, MainActivity.Clockwise.LEFT);   //自己交差を修復して角をまるめる
                //exPolygon.setPoints(outline2);
            }
            exPolygon.setFillColor(Color.argb(83, 0, 0, 83));
            exPolygon.setStrokeColor(Color.BLUE);
            exPolygon.setStrokeWidth(1F);
            return exPolygon;
        }

        private ArrayList<GeoPoint> getExpandedPointArray(ArrayList<GeoPoint> points, double mergin) {
            //CustomPolygon exPolygon = new CustomPolygon();
            //<GeoPoint> points = (ArrayList<GeoPoint>) polygon.getPoints();    //90deg rotated
            double tolerancean = mergin + 1;

            ArrayList<GeoPoint> outline = new ArrayList<>();

            //マップ上での緯度経度のあれこれをベクトルで扱いたいクラス
            class GeoVector {
                double X;
                double Y;

                GeoVector(double x, double y) {
                    this.X = x;
                    this.Y = y;
                }

                private GeoVector rotateLeft90deg() {
                    double temp = this.X;
                    this.X = -this.Y;
                    this.Y = temp;
                    return this;
                }

                private GeoVector rotateRight90deg() {
                    double temp = this.X;
                    this.X = this.Y;
                    this.Y = -temp;
                    return this;
                }

                private double getLength() {
                    return Math.sqrt(Math.pow(this.X, 2.0) + Math.pow(this.Y, 2.0));
                }
            }

            //自己交差しない右回りの閉ループとその拡張された領域を考える
            for (int pointNum = 0; pointNum <= points.size() - 2; pointNum++) {

                //ある一辺をなす点Aと点Bの座標
                double Ax = points.get(pointNum).getLongitude();
                double Ay = points.get(pointNum).getLatitude();
                double Bx = points.get(pointNum + 1).getLongitude();
                double By = points.get(pointNum + 1).getLatitude();

                //ベクトルABを左に90度回転 = ベクトルAB'
                GeoVector vector = new GeoVector(Bx - Ax, By - Ay);
                vector.rotateLeft90deg();

                double m = vector.Y / vector.X;   //傾きm

                //点Aを通る傾きmの方向の直線の式：y = m * (x - Ax) + Ay

                double Px = Ax; //目標値の初期値
                double Py = Ay; //目標値の初期値

                double N1;
                double N = calcDistance(Ax, Ay, Px, Py);

                //直線の傾きが正なら目標値になるまで経度をプラスしていく(ベクトルABの傾きが負の時は条件反転)
                if (Bx - Ax > 0) {
                    if (m > 0) {
                        while (!(N >= mergin)) {
                            Px = Px + 0.00000001;
                            Py = m * (Px - Ax) + Ay;
                            //N1 = N;
                            N = calcDistance(Ax, Ay, Px, Py);
                        }
                    } else {
                        //直線の傾きが負なら目標値になるまで経度をマイナスしていく
                        while (!(N >= mergin)) {
                            Px = Px - 0.00000001;
                            Py = m * (Px - Ax) + Ay;
                            //N1 = N;
                            N = calcDistance(Ax, Ay, Px, Py);
                        }
                    }
                } else {
                    if (m < 0) {
                        while (!(N >= mergin)) {
                            Px = Px + 0.00000001;
                            Py = m * (Px - Ax) + Ay;
                            //N1 = N;
                            N = calcDistance(Ax, Ay, Px, Py);
                        }
                    } else {
                        //直線の傾きが負なら目標値になるまで経度をマイナスしていく
                        while (!(N >= mergin)) {
                            Px = Px - 0.00000001;
                            Py = m * (Px - Ax) + Ay;
                            //N1 = N;
                            N = calcDistance(Ax, Ay, Px, Py);
                        }
                    }
                }
                outline.add(new GeoPoint(Py, Px));

                //点Bを通る傾きmの方向の直線の式：y = m * (x - Bx) + By

                Px = Bx;    //目標値の初期値
                Py = By;    //目標値の初期値

                N = calcDistance(Bx, By, Px, Py);

                //直線の傾きが正なら目標値になるまで経度をプラスしていく(ベクトルABの傾きが負の時は条件反転)
                if (Bx - Ax > 0) {
                    if (m > 0) {
                        while (!(N >= mergin)) {
                            Px = Px + 0.00000001;
                            Py = m * (Px - Bx) + By;
                            //N1 = N;
                            N = calcDistance(Bx, By, Px, Py);
                        }
                    } else {
                        //直線の傾きが負なら目標値になるまで経度をマイナスしていく
                        while (!(N >= mergin)) {
                            Px = Px - 0.00000001;
                            Py = m * (Px - Bx) + By;
                            //N1 = N;
                            N = calcDistance(Bx, By, Px, Py);
                        }
                    }
                } else {
                    if (m < 0) {
                        while (!(N >= mergin)) {
                            Px = Px + 0.00000001;
                            Py = m * (Px - Bx) + By;
                            //N1 = N;
                            N = calcDistance(Bx, By, Px, Py);
                        }
                    } else {
                        //直線の傾きが負なら目標値になるまで経度をマイナスしていく
                        while (!(N >= mergin)) {
                            Px = Px - 0.00000001;
                            Py = m * (Px - Bx) + By;
                            //N1 = N;
                            N = calcDistance(Bx, By, Px, Py);
                        }
                    }
                }
                outline.add(new GeoPoint(Py, Px));
            }

            //ある一辺をなす点Aと点Bの座標
            double Ax = points.get(points.size() - 1).getLongitude();
            double Ay = points.get(points.size() - 1).getLatitude();
            double Bx = points.get(0).getLongitude();
            double By = points.get(0).getLatitude();

            //ベクトルABを左に90度回転 = ベクトルAB'
            GeoVector vector = new GeoVector(Bx - Ax, By - Ay);
            vector.rotateLeft90deg();

            //点Aを通るベクトルAB'の方向の直線の式：y = (Vab.Y / Vab.X) * (x - Ax) + Ay
            double m = vector.Y / vector.X;   //傾きm

            double Px = Ax; //目標値の初期値
            double Py = Ay; //目標値の初期値

            double N1;
            double N = calcDistance(Ax, Ay, Px, Py);

            //直線の傾きが正なら目標値になるまで経度をプラスしていく(ベクトルABの傾きが負の時は条件反転)
            if (Bx - Ax > 0) {
                if (m > 0) {
                    while (!(N >= mergin)) {
                        Px = Px + 0.00000001;
                        Py = m * (Px - Ax) + Ay;
                        //N1 = N;
                        N = calcDistance(Ax, Ay, Px, Py);
                    }
                } else {
                    //直線の傾きが負なら目標値になるまで経度をマイナスしていく
                    while (!(N >= mergin)) {
                        Px = Px - 0.00000001;
                        Py = m * (Px - Ax) + Ay;
                        //N1 = N;
                        N = calcDistance(Ax, Ay, Px, Py);
                    }
                }
            } else {
                if (m < 0) {
                    while (!(N >= mergin)) {
                        Px = Px + 0.00000001;
                        Py = m * (Px - Ax) + Ay;
                        //N1 = N;
                        N = calcDistance(Ax, Ay, Px, Py);
                    }
                } else {
                    //直線の傾きが負なら目標値になるまで経度をマイナスしていく
                    while (!(N >= mergin)) {
                        Px = Px - 0.00000001;
                        Py = m * (Px - Ax) + Ay;
                        //N1 = N;
                        N = calcDistance(Ax, Ay, Px, Py);
                    }
                }
            }
            outline.add(new GeoPoint(Py, Px));

            //点Aを通るベクトルAB'の方向の直線の式：y = (v.Y / v.X) * (x - Bx) + By

            Px = Bx; //目標値の初期値
            Py = By; //目標値の初期値

            N = calcDistance(Bx, By, Px, Py);

            //直線の傾きが正なら目標値になるまで経度をプラスしていく(ベクトルABの傾きが負の時は条件反転)
            if (Bx - Ax > 0) {
                if (m > 0) {
                    while (!(N <= tolerancean && N >= mergin)) {
                        Px = Px + 0.00000001;
                        Py = m * (Px - Bx) + By;
                        //N1 = N;
                        N = calcDistance(Bx, By, Px, Py);
                    }
                } else {
                    //直線の傾きが負なら目標値になるまで経度をマイナスしていく
                    while (!(N <= tolerancean && N >= mergin)) {
                        Px = Px - 0.00000001;
                        Py = m * (Px - Bx) + By;
                        //N1 = N;
                        N = calcDistance(Bx, By, Px, Py);
                    }
                }
            } else {
                if (m < 0) {
                    while (!(N <= tolerancean && N >= mergin)) {
                        Px = Px + 0.00000001;
                        Py = m * (Px - Bx) + By;
                        //N1 = N;
                        N = calcDistance(Bx, By, Px, Py);
                    }
                } else {
                    while (!(N <= tolerancean && N >= mergin)) {
                        Px = Px - 0.00000001;
                        Py = m * (Px - Bx) + By;
                        //N1 = N;
                        N = calcDistance(Bx, By, Px, Py);
                    }
                }
            }
            outline.add(new GeoPoint(Py, Px));
            return outline;
        }

        private Polygon getFixedPolygon(ArrayList<GeoPoint> points, ArrayList<GeoPoint> defaultPoints, MainActivity.Clockwise clockwise) {
            //ArrayList<GeoPoint> defaultPoints = new ArrayList<GeoPoint>(points);
            ArrayList<ArrayList<GeoPoint>> fixedPoints = new ArrayList<>();
            ArrayList<GeoPoint> roundedPoints;

            Polygon fixedPolygon = new Polygon();
            final ShapeAsPointsBuilder shapeBuilder = new ShapeAsPointsBuilder();
            //各辺の自己交差を修復しつつPolygonを組み立てる
            GeoPoint crossPoint;
            int pointSize = points.size();
            for (int pointNum = 0; pointNum <= pointSize - 4; pointNum += 2) {
                crossPoint = getIntersection(points.get(pointNum), points.get(pointNum + 1),     //line1 : start,end
                        points.get(pointNum + 2), points.get(pointNum + 3));  //line2 ; start,end
                if (crossPoint != null) {
                    //交差していれば
                    points.set(pointNum + 1, crossPoint);
                    points.remove(pointNum + 2);
                    pointNum--;
                    pointSize--;

                    ArrayList<GeoPoint> arrayList = new ArrayList<>();
                    arrayList.add(crossPoint);
                    fixedPoints.add(arrayList);
                } else {
                    ArrayList<GeoPoint> arrayList = new ArrayList<>();
                    arrayList.add(points.get(pointNum + 1));
                    arrayList.add(points.get(pointNum + 2));
                    fixedPoints.add(arrayList);
                }
            }
            // 最後の線分と最初の線分の交差判別
            crossPoint = getIntersection(points.get(points.size() - 2), points.get(points.size() - 1),   //line1 : start,end
                    points.get(0), points.get(1));                              //line2 ; start,end
            if (crossPoint != null) {
                //if cross point exist
                points.set(points.size() - 1, crossPoint);
                points.remove(0);

                ArrayList<GeoPoint> arrayList = new ArrayList<>();
                arrayList.add(crossPoint);
                fixedPoints.add(arrayList);

            } else {
                ArrayList<GeoPoint> arrayList = new ArrayList<>();
                arrayList.add(points.get(points.size() - 1));
                arrayList.add(points.get(0));
                fixedPoints.add(arrayList);
            }


            int defaultPointNum = 1;

            //Polygonの角を調整
            if (clockwise == MainActivity.Clockwise.RIGHT) {
                for (int pointNum = 0; pointNum <= fixedPoints.size() - 2; pointNum++) {
                    if (fixedPoints.get(pointNum).size() == 2) {
                        shapeBuilder.CWA(
                                fixedPoints.get(pointNum).get(0),
                                defaultPoints.get(defaultPointNum),
                                fixedPoints.get(pointNum).get(1)
                        );
                        defaultPointNum++;
                    } else {
                        shapeBuilder.GRC(fixedPoints.get(pointNum).get(0));
                        defaultPointNum++;
                    }
                }


                if (fixedPoints.get(fixedPoints.size() - 1).size() == 2) {
                    shapeBuilder.CWA(
                            fixedPoints.get(fixedPoints.size() - 1).get(0),
                            defaultPoints.get(0),
                            fixedPoints.get(fixedPoints.size() - 1).get(1)
                    );
                    defaultPointNum++;
                } else {
                    shapeBuilder.GRC(fixedPoints.get(fixedPoints.size() - 1).get(0));
                }
                roundedPoints = new ArrayList<>(shapeBuilder.toList());
                fixedPolygon.setPoints(roundedPoints);

            } else if (clockwise == MainActivity.Clockwise.LEFT) {

                Collections.reverse(defaultPoints);

                for (int pointNum = 0; pointNum <= fixedPoints.size() - 2; pointNum++) {
                    if (fixedPoints.get(pointNum).size() == 2) {
                        shapeBuilder.CWA(
                                fixedPoints.get(pointNum).get(0),
                                defaultPoints.get(defaultPointNum),
                                fixedPoints.get(pointNum).get(1)
                        );
                        defaultPointNum++;
                    } else {
                        shapeBuilder.GRC(fixedPoints.get(pointNum).get(0));
                        defaultPointNum++;
                    }
                }
                if (fixedPoints.get(fixedPoints.size() - 1).size() == 2) {
                    shapeBuilder.CWA(fixedPoints.get(fixedPoints.size() - 1).get(0),
                            defaultPoints.get(0),
                            fixedPoints.get(fixedPoints.size() - 1).get(1)
                    );
                    defaultPointNum++;
                } else {
                    shapeBuilder.GRC(fixedPoints.get(fixedPoints.size() - 1).get(0));
                }

                roundedPoints = new ArrayList<>(shapeBuilder.toList());
                fixedPolygon.setPoints(roundedPoints);

            } else {
                fixedPolygon.setPoints(points);     //kokoiru?
            }

            return fixedPolygon;
        }

        private GeoPoint getIntersection(GeoPoint startPoint1, GeoPoint endPoint1, GeoPoint startPoint2, GeoPoint endPoint2) {

            double x0 = startPoint1.getLongitude();
            double y0 = startPoint1.getLatitude();
            double x1 = endPoint1.getLongitude();
            double y1 = endPoint1.getLatitude();

            double x2 = startPoint2.getLongitude();
            double y2 = startPoint2.getLatitude();
            double x3 = endPoint2.getLongitude();
            double y3 = endPoint2.getLatitude();

            double a0 = (y1 - y0) / (x1 - x0);
            double a1 = (y3 - y2) / (x3 - x2);

            double x = (a0 * x0 - y0 - a1 * x2 + y2) / (a0 - a1);
            double y = (y1 - y0) / (x1 - x0) * (x - x0) + y0;

            if (Math.abs(a0) == Math.abs(a1)) {
                return null;
            }

            if (x > Math.max(x0, x1) || x > Math.max(x2, x3) ||
                    y > Math.max(y0, y1) || y > Math.max(y2, y3) ||
                    x < Math.min(x0, x1) || x < Math.min(x2, x3) ||
                    x < Math.min(x0, x1) || y < Math.min(y2, y3)) {
                return null;
            }

            return new GeoPoint(y, x);
        }

        private double findAreaSize(ArrayList<GeoPoint> points) {
            double area = 0;
            int pointSize = points.size();
            int j = pointSize - 1;
            for (int i = 0; i < pointSize; i++) {
                area += (points.get(j).getLongitude() + points.get(i).getLongitude()) * (points.get(j).getLatitude() - points.get(i).getLatitude());
                j = i;
            }
            return Math.abs(area / 2.0);
        }

        private double calcDistance(double x1, double y1, double x2, double y2) {
            final double GRS80_A = 6378137.000;//長半径 a(m)
            final double GRS80_E2 = 0.00669438002301188;//第一遠心率  eの2乗

            double lon1 = Double.valueOf(x1);
            double lat1 = Double.valueOf(y1);

            double lon2 = Double.valueOf(x2);
            double lat2 = Double.valueOf(y2);

            double my = deg2rad((lat1 + lat2) / 2.0); //緯度の平均値
            double dy = deg2rad(lat1 - lat2); //緯度の差
            double dx = deg2rad(lon1 - lon2); //経度の差

            //卯酉線曲率半径を求める(東と西を結ぶ線の半径)
            double sinMy = Math.sin(my);
            double w = Math.sqrt(1.0 - GRS80_E2 * sinMy * sinMy);
            double n = GRS80_A / w;

            //子午線曲線半径を求める(北と南を結ぶ線の半径)
            double mnum = GRS80_A * (1 - GRS80_E2);
            double m = mnum / (w * w * w);

            //ヒュベニの公式
            double dym = dy * m;
            double dxncos = dx * n * Math.cos(my);
            return Math.sqrt(dym * dym + dxncos * dxncos);
        }

        private double deg2rad(double deg) {
            return deg * Math.PI / 180.0;
        }

        private Boolean isPointInPolygon(GeoPoint p, Polygon poly) {
            GeoPoint p1, p2;
            Boolean inside = false;
            GeoPoint oldPoint = poly.getPoints().get(poly.getPoints().size() - 1);
            for (int i = 0; i < poly.getPoints().size(); i++) {
                GeoPoint newPoint = poly.getPoints().get(i);
                if (newPoint.getLongitude() > oldPoint.getLongitude()) {
                    p1 = oldPoint;
                    p2 = newPoint;
                } else {
                    p1 = newPoint;
                    p2 = oldPoint;
                }
                if ((p1.getLongitude() < p.getLongitude()) == (p.getLongitude() <= p2.getLongitude())
                        && (p.getLatitude() - p1.getLatitude()) * (p2.getLongitude() - p1.getLongitude())
                        < (p2.getLatitude() - p1.getLatitude()) * (p.getLongitude() - p1.getLongitude())
                ) {
                    inside = !inside;
                }
                oldPoint = newPoint;
            }
            return inside;
        }

        private Integer isInsideDangerArea(GeoPoint point) {
            int size = areaDataArray.size() - 1;
            for (int i = 0; i <= size; i++) {
                if (isPointInPolygon(point, areaDataArray.get(i).areaPolygon) == true) {
                    return i;
                }
            }
            return null;
        }

        private Integer isInsideExpandedDangerArea(GeoPoint point) {
            int size = areaDataArray.size() - 1;
            for (int i = 0; i <= size; i++) {
                if (isPointInPolygon(point, areaDataArray.get(i).expandedPolygon) == true) {
                    return i;
                }
            }
            return null;
        }

        private Integer isInsideSemiExpandedDangerArea(GeoPoint point) {
            int size = areaDataArray.size() - 1;
            for (int i = 0; i <= size; i++) {
                if (isPointInPolygon(point, areaDataArray.get(i).semiExpandedPolygon) == true) {
                    return i;
                }
            }
            return null;
        }

        private void outputAreaData(String path, String filename) {
            int listSize = areaDataArray.size() - 1;
            ArrayList<String> outputDataStringArray = new ArrayList<>();

            for (int listNum = 0; listNum <= listSize; listNum++) {
                int polySize = areaDataArray.get(listNum).areaPolygon.getPoints().size() - 1;
                ArrayList<GeoPoint> polyPoint = (ArrayList<GeoPoint>) areaDataArray.get(listNum).areaPolygon.getPoints();
                String NUMBER = String.valueOf(listNum);

                for (int pointNum = 0; pointNum <= polySize; pointNum++) {
                    String LATITUDE = String.valueOf(getRoundedDouble(polyPoint.get(pointNum).getLatitude(), 7));
                    String LONGITUDE = String.valueOf(getRoundedDouble(polyPoint.get(pointNum).getLongitude(), 7));
                    outputDataStringArray.add(NUMBER + "," + LATITUDE + "," + LONGITUDE);
                }
            }

            try {
                //FileWriter fw = new FileWriter(path+"/"+filename+".csv", false);
                PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "/" + filename), "utf-8")));
                pw.print(Integer.toString(parameter.closeDistance));
                pw.println();
                pw.print("No.,latitude[deg.],longitude[deg.]");
                pw.println();
                for (int i = 0; i <= outputDataStringArray.size() - 1; i++) {
                    pw.print(outputDataStringArray.get(i));
                    pw.println();
                }
                pw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Double getRoundedDouble(Double val, int decimalDegit) {
            BigDecimal bigDecimal = new BigDecimal(Double.toString(val));
            bigDecimal = bigDecimal.setScale(decimalDegit, BigDecimal.ROUND_HALF_UP);
            val = Double.parseDouble(bigDecimal.toString());
            return val;
        }
    }

    enum LocationStatus {
        OUTSIDE,
        SEMI_CLOSE,
        CLOSE,
        INSIDE
    }

    private class CustomLocationLogger {
        private List<LogData> logList;
        private List<String> outpusStringArray;
        private CustomDangerArea loggingArea;
        private CustomParameter loggingParameter;

        private String createdTime;

        CustomLocationLogger() {
            logList = new ArrayList<>();
            outpusStringArray = new ArrayList<>();
        }

        CustomLocationLogger(CustomDangerArea dangerArea, CustomParameter parameter) {
            logList = new ArrayList<>();
            outpusStringArray = new ArrayList<>();
            loggingArea = dangerArea;
            loggingParameter = parameter;
            outpusStringArray.add("Approach Distance[m]");
            outpusStringArray.add(Integer.toString(parameter.closeDistance));
            outpusStringArray.add("Area Coordinates[deg.]");
            for (int areaNum = 0; areaNum <= dangerArea.areaDataArray.size() - 1; areaNum++) {
                String areaString = "";
                for (int pointNum = 0; pointNum <= dangerArea.areaDataArray.get(areaNum).areaPolygon.getPoints().size() - 1; pointNum++) {
                    double lat = dangerArea.areaDataArray.get(areaNum).areaPolygon.getPoints().get(pointNum).getLatitude();
                    double lon = dangerArea.areaDataArray.get(areaNum).areaPolygon.getPoints().get(pointNum).getLongitude();

                    String latStr = String.format("%1$.7f", lat);
                    String lonStr = String.format("%1$.7f", lon);

                    areaString += latStr + "," + lonStr + ",";
                }
                outpusStringArray.add(areaString);
            }
            outpusStringArray.add("Date,Time,Latitude[deg.],Longitude[deg.],Status");

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);
            SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
            createdTime = format.format(calendar.getTime());
        }

        private String getSrtingAppendedZero(int num) {
            String numberString = "";
            if (num <= 9) {
                numberString = "0" + Integer.toString(num);
            } else {
                numberString = Integer.toString(num);
            }
            return numberString;
        }

        private void appendLog(Calendar calendar, GeoPoint point, LocationStatus status, String path, String filename) {
            //int free = getAvailableStrageMB();
            double free = getAvailableStorageMB();
            if (free <= RemainSizeMB) {

                List<File> fileList = new ArrayList<>(Arrays.asList(new File(MainActivity.PATH_MAIN_DIRECTORY).listFiles()));
                List<File> logFileList = new ArrayList<>();
                for (int num = 0; num <= fileList.size() - 1; num++) {
                    if (!fileList.get(num).getName().contains("parameter.csv") && !fileList.get(num).getName().contains(".area") && !fileList.get(num).getName().contains("maps")) {   //履歴データない場合はここで弾かれる & oldestDate != 0 で弾かれる
                        logFileList.add(fileList.get(num));
                    }
                }
                if (logFileList.size() == 1 && logFileList.get(0).getName().contains(filename)) {
                    Log.d("LocationService", "*** cannot append log data ***");
                    if (serviceCallback != null) {
                        serviceCallback.showAlertMessage(true, "ストレージ容量上限\r\n記録できません");
                    }
                    return;
                } else {
                    removeOldestLogData();
                    if (serviceCallback != null) {
                        serviceCallback.showAlertMessage(true, "ストレージ容量上限\r\n古いデータは削除されます");
                    }
                }

            }
            String logString = "";
            int ampm = calendar.get(Calendar.AM_PM) * 12;

            logString = getSrtingAppendedZero(calendar.get(Calendar.YEAR)) + "/" + getSrtingAppendedZero(calendar.get(Calendar.MONTH) + 1) + "/" + getSrtingAppendedZero(calendar.get(Calendar.DAY_OF_MONTH)) + ","
                    + getSrtingAppendedZero(calendar.get(Calendar.HOUR) + ampm) + ":" + getSrtingAppendedZero(calendar.get(Calendar.MINUTE)) + ":" + getSrtingAppendedZero(calendar.get(Calendar.SECOND)) + ",";

            String latStr = MessageFormat.format("{0,number,0.0000000}", point.getLatitude());
            String lonStr = MessageFormat.format("{0,number,0.0000000}", point.getLongitude());

            logString += latStr + "," + lonStr + ",";

            switch (status) {
                case OUTSIDE:
                    logString += "0";
                    break;
                case SEMI_CLOSE:
                    logString += "1";
                    break;
                case CLOSE:
                    logString += "2";
                    break;
                case INSIDE:
                    logString += "3";
                    break;
            }

            ////////////////////////////////////////////

            logString += "," + getBatteryPercent();

            ////////////////////////////////////////////


            File appendedFile = new File(path + "/" + filename);
            FileWriter fw = null;
            if (appendedFile.exists()) {    //ファイルが既に存在していたら追記する
                try {
                    fw = new FileWriter(appendedFile, true);
                    //pw = new PrintWriter(fw);
                    fw.write(logString + "\r\n");
                    //fw.flush();   //close時に実行されるらしい
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {    //ファイルがまだない場合は新規生成する
                List<File> fileList = new ArrayList<>(Arrays.asList(new File(MainActivity.PATH_MAIN_DIRECTORY).listFiles()));
                List<File> logFileList = new ArrayList<>();
                for (int num = 0; num <= fileList.size() - 1; num++) {  //履歴データ数のカウント
                    if (!fileList.get(num).getName().contains("parameter.csv") && !fileList.get(num).getName().contains(".area") && !fileList.get(num).getName().contains("maps")) {   //履歴データない場合はここで弾かれる & oldestDate != 0 で弾かれる
                        logFileList.add(fileList.get(num));
                    }
                }
                if (logFileList.size() < 30) {    //履歴データが最大数に達していなければ新規生成
                    locationLogger.outputLog(MainActivity.PATH_MAIN_DIRECTORY, filename);
                } else {    //履歴数が最大なら一番古いデータを削除して新規生成
                    Log.d("LocationService", "log data reach to max quantity");
                    removeOldestLogData();
                    locationLogger.outputLog(MainActivity.PATH_MAIN_DIRECTORY, filename);
                    Toast.makeText(context, "履歴データの数が上限に達しました", Toast.LENGTH_SHORT).show();

                    if (serviceCallback != null) {
                        serviceCallback.showAlertMessage(true, "履歴データ数上限\r\n古いデータは削除されます");
                    }
                }
            }
        }

        private void addLog(Calendar calendar, GeoPoint point, LocationStatus status) {
            String logString = "";
            logString = getSrtingAppendedZero(calendar.get(calendar.YEAR)) + "/" + getSrtingAppendedZero(calendar.get(calendar.MONTH)) + "/" + getSrtingAppendedZero(calendar.get(calendar.DAY_OF_MONTH)) + ","
                    + getSrtingAppendedZero(calendar.get(calendar.HOUR)) + ":" + getSrtingAppendedZero(calendar.get(calendar.MINUTE)) + ":" + getSrtingAppendedZero(calendar.get(calendar.SECOND)) + ",";

            String latStr = MessageFormat.format("{0,number,0.0000000}", point.getLatitude());
            String lonStr = MessageFormat.format("{0,number,0.0000000}", point.getLongitude());

            latStr = Double.toString(point.getLatitude());
            lonStr = Double.toString(point.getLongitude());

            logString += latStr + "," + lonStr + ",";

            switch (status) {
                case OUTSIDE:
                    logString += "0";
                    break;
                case SEMI_CLOSE:
                    logString += "1";
                    break;
                case CLOSE:
                    logString += "2";
                    break;
                case INSIDE:
                    logString += "3";
                    break;
            }
            outpusStringArray.add(logString);
        }

        private void outputLog(String path, String filename) {

            try {
                FileWriter fw = new FileWriter(path + "/" + filename, false);
                PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
                for (int i = 0; i <= outpusStringArray.size() - 1; i++) {
                    pw.print(outpusStringArray.get(i) + "\r\n");
                    //pw.println();
                }
                pw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private class LocationDate {
            private int year;
            private int month;
            private int day;
            private int hour;
            private int minute;
            private int second;

            LocationDate(Calendar calendar) {
                this.year = calendar.get(Calendar.YEAR);
                this.month = calendar.get(Calendar.MONTH) + 1;
                this.day = calendar.get(Calendar.DAY_OF_MONTH);
                this.hour = calendar.get(Calendar.HOUR_OF_DAY);
                this.minute = calendar.get(Calendar.MINUTE);
                this.second = calendar.get(Calendar.SECOND);
            }

            LocationDate(int year, int month, int day, int hour, int minute, int second) {
                this.year = year;
                this.month = month;
                this.day = day;
                this.hour = hour;
                this.minute = minute;
                this.second = second;
            }

            private String getString() {
                String string = "";
                string = Integer.toString(year) + "/" + Integer.toString(month) + "/" + Integer.toString(day) + "\r\n" + Integer.toString(hour) + ":" + Integer.toString(minute) + ":" + Integer.toString(second);
                return string;
            }
        }

        private class LogData {
            private LocationDate locationDate;
            private double latitude;
            private double longitude;
            private int locationStatus;
            private int areaNum;

            LogData(Calendar calendar, double lat, double lon, LocationStatus locationStatus, int areaNum) {
                locationDate = new LocationDate(calendar);
                this.latitude = lat;
                this.longitude = lon;
                this.areaNum = areaNum;
                switch (locationStatus) {
                    case OUTSIDE:
                        this.locationStatus = 0;
                        break;
                    case SEMI_CLOSE:
                        this.locationStatus = 1;
                        break;
                    case CLOSE:
                        this.locationStatus = 2;
                        break;
                    case INSIDE:
                        this.locationStatus = 3;
                        break;
                }
            }
        }

        private void addLocationLog(Calendar calendar, GeoPoint point, LocationStatus status, Integer areaNum) {
            logList.add(new LogData(calendar, point.getLatitude(), point.getLongitude(), status, areaNum));
        }

        private void clearLog() {
            logList.clear();
        }

        private void outputCsvLog(String path, String filename) {
            int listSize = logList.size() - 1;
            ArrayList<String> outputDataStringArray = new ArrayList<>();
            for (int i = 0; i <= listSize; i++) {
                String YEAR = String.valueOf((int) logList.get(i).locationDate.year);
                String MONTH = String.valueOf((int) logList.get(i).locationDate.month);
                String DAY = String.valueOf((int) logList.get(i).locationDate.day);

                String HOUR = "";
                if (logList.get(i).locationDate.hour <= 9) {
                    HOUR = "0" + String.valueOf((int) logList.get(i).locationDate.hour);
                } else {
                    HOUR = String.valueOf((int) logList.get(i).locationDate.hour);
                }

                String MINUTE = "";
                if (logList.get(i).locationDate.minute <= 9) {
                    MINUTE = "0" + String.valueOf((int) logList.get(i).locationDate.minute);
                } else {
                    MINUTE = String.valueOf((int) logList.get(i).locationDate.minute);
                }

                String SECOND = "";
                if (logList.get(i).locationDate.second <= 9) {
                    SECOND = "0" + String.valueOf((int) logList.get(i).locationDate.second);
                } else {
                    SECOND = String.valueOf((int) logList.get(i).locationDate.second);
                }

                String LATITUDE = Double.toString(logList.get(i).latitude);
                String LONGITUDE = Double.toString(logList.get(i).longitude);
                String STATUS = Integer.toString(logList.get(i).locationStatus);
                String AREANUM = Integer.toString(logList.get(i).areaNum);
                if (AREANUM == "99") {
                    AREANUM = "null";
                }
                //String CHECKSUM = "0";
                outputDataStringArray.add(YEAR + "/" + MONTH + "/" + DAY + "," + HOUR + ":" + MINUTE + ":" + SECOND + "," + LATITUDE + "," + LONGITUDE + "," + STATUS + "," + AREANUM);
            }
            try {
                FileWriter fw = new FileWriter(path + "/" + filename, false);
                PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

                pw.print("date,time,latitude[deg.],longitude[deg.],status,area number");
                pw.println();
                for (int i = 0; i <= listSize; i++) {
                    pw.print(outputDataStringArray.get(i));
                    pw.println();
                }
                pw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final IBinder binder = new MyBinder();

    public class MyBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void reloadParameter() {
        parameter.readParameter(MainActivity.PATH_PARAMETER);
        try {
            new Handler().postDelayed(new Runnable() {  //読み出し終わるまで100ms待つ
                @Override
                public void run() {
                    Log.i("LocationService", "Parameter reloaded");
                    //if (!dangerArea.areaDataArray.isEmpty()) {
                    Log.i("LocationService", "reload:" + parameter.areaData.toString());
                    dangerArea.areaDataArray.clear();
                    for (int num = 0; num <= parameter.areaQty - 1; num++) {
                        dangerArea.addAreaData(parameter.areaData.get(num));
                        Log.i("LocationService", "AreaData Loaded (" + Integer.toString(num) + ") : " + parameter.areaData.get(num).toString());
                    }
                    locationLogger = new CustomLocationLogger(dangerArea, parameter);
                    //} else {
                    //    Log.i("LocationService", "area data is empty");
                    //}
                }
            }, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleAreaWatching() {
        isAreaWatchingStarted = !isAreaWatchingStarted;
        Log.i("LocationService", "AreaWatching : " + Boolean.toString(isAreaWatchingStarted));
    }

    private String LABEL_BEACON = "gps";
    private Boolean isScanning = false;
    private BluetoothManager manager;
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner btScanner;
    private ScanSettings scanSettings;
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getName() != null) {
                //Log.i("ScanResult", result.getDevice().getName());
            }
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (device.getName() != null) {
                if (device.getName().contains(LABEL_BEACON)) {
                    Log.i("ScanResult", device.getName() + " / rssi = " + Integer.toString(result.getRssi()) + " / txPower = " + Integer.toString(result.getTxPower()));
                    String name = device.getName();
                    String[] nameLatLon = name.split(",");
                    double latitude = Double.parseDouble(nameLatLon[1]);
                    double longitude = Double.parseDouble(nameLatLon[2]);

                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
                    String formatDate = format.format(calendar.getTime());

                    //beaconLogger.addStringLine(formatDate + "," + Double.toString(latitude) + "," + Double.toString(longitude));

                    double distance = calcDistance(lastPoint.getLatitude(), lastPoint.getLongitude(), latitude, longitude);

                    double rssiDistance = calcDistance(result.getRssi(), result.getTxPower());

                    beaconLogger.addStringLine(formatDate + "," + Double.toString(latitude) + "," + Double.toString(longitude) + "," + Double.toString(lastPoint.getLatitude()) + "," + Double.toString(lastPoint.getLongitude()) + "," + Double.toString(distance) + "," + Double.toString(rssiDistance));
                    beaconLogger.outputCsvLog(MainActivity.PATH_MAIN_DIRECTORY, "beaconLog.csv");

                    playAlert(parameter.jukiVolume / 10.0f);
                    stopScan();

                }
            }
        }

        public static final double GRS80_A = 6378137.000;//長半径 a(m)
        public static final double GRS80_E2 = 0.00669438002301188;//第一遠心率  eの2乗

        public double deg2rad(double deg) {
            return deg * Math.PI / 180.0;
        }

        public double calcDistance(double lat1, double lng1, double lat2, double lng2) {
            double my = deg2rad((lat1 + lat2) / 2.0); //緯度の平均値
            double dy = deg2rad(lat1 - lat2); //緯度の差
            double dx = deg2rad(lng1 - lng2); //経度の差

            //卯酉線曲率半径を求める(東と西を結ぶ線の半径)
            double sinMy = Math.sin(my);
            double w = Math.sqrt(1.0 - GRS80_E2 * sinMy * sinMy);
            double n = GRS80_A / w;

            //子午線曲線半径を求める(北と南を結ぶ線の半径)
            double mnum = GRS80_A * (1 - GRS80_E2);
            double m = mnum / (w * w * w);

            //ヒュベニの公式
            double dym = dy * m;
            double dxncos = dx * n * Math.cos(my);
            return Math.sqrt(dym * dym + dxncos * dxncos);
        }

        public double calcDistance(int rssi, int txpower) {
            if (rssi == 0) {
                return -1.0;
            }
            double ratio = rssi * 1.0 / txpower;
            if (ratio < 1.0) {
                return Math.pow(ratio, 10);
            } else {
                double distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
                return distance;
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private void initScan() {
        Log.i("BLE", "init scan");
        manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = manager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
    }

    private void startScan() {
        if (isScanning == false) {
            Log.i("BLE", "start scan");
            btScanner.startScan(scanCallback);
            isScanning = true;
        }
    }

    private void stopScan() {
        if (isScanning = true) {
            Log.i("BLE", "stop scan");
            btScanner.stopScan(scanCallback);
            isScanning = false;
        }
    }
}
