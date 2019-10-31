package com.example.ueda_r.taiseiApp;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ueda_r.osmdroidtest1022.R;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.github.lassana.osmdroid_shape_extension.ShapeAsPointsBuilder;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.mapsforge.map.android.rendertheme.AssetsRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.config.Configuration;
import org.osmdroid.gpkg.tiles.raster.GeoPackageMapTileModuleProvider;
import org.osmdroid.gpkg.tiles.raster.GeoPackageProvider;
import org.osmdroid.gpkg.tiles.raster.GeopackageRasterTileSource;
import org.osmdroid.mapsforge.MapsForgeTileModuleProvider;
import org.osmdroid.mapsforge.MapsForgeTileSource;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileApproximater;
import org.osmdroid.tileprovider.modules.MapTileAssetsProvider;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SettingFragment.OnFragmentInteractionListener,
        SettingReceiveFragment.OnFragmentInteractionListener,
        ChildSettingFragment.OnFragmentInteractionListener,
        ReadAreaFragment.OnFragmentInteractionListener,
        ReadAreaFragment.ReadAreaFragmentCallback,
        ReadHistoryFragment.OnFragmentInteractionListener,
        ReadHistoryFragment.ReadHistoryFragmentCallback,
        Marker.OnMarkerClickListener,
        LocationService.LocationServiceCallback,
        BleFileExplorer.BleFileExplorerCallback {

    String keyword = "";

    public static final String PATH_MAIN_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/areadetection";
    public static final String PATH_PARAMETER = PATH_MAIN_DIRECTORY + "/parameter.csv";
    public static final String PATH_MAP = PATH_MAIN_DIRECTORY + "/maps";

    protected enum ColorMarker {
        MARKER_RED,
        MARKER_BLUE,
        MARKER_YELLOW,
        MARKER_GREEN,
        MARKER_GRAY
    }

    protected enum LogMarker {
        MARKER_START,
        MARKER_END,
        MARKER_POINT,
        MARKER_POINT_OUTSIDE,
        MARKER_POINT_CLOSE,
        MARKER_POINT_INSIDE,
        MARKER_WALK,
        MARKER_OUTSIDE,
        MARKER_SEMI_CLOSE,
        MARKER_CLOSE,
        MARKER_INSIDE
    }

    protected enum Clockwise {
        RIGHT,
        LEFT
    }

    private MapView mMapView = null;
    private Set<ITileSource> tileSources = new HashSet<>();

    public Parameter parameter;
    private DangerArea dangerArea;
    private GeoPoint lastLocation = new GeoPoint(35.698353, 139.773114);

    private LocationLogger locationLogger = new LocationLogger();

    private IFilesystemCache tileWriter = null;

    private TextView userIdText;
    private TextView groupIdText;

    private ImageView imageView;
    private TextView alertText;
    private Button alertConfirm;
    private LinearLayout alertLayout;
    private Button logRemoveButton;

    private Button debugButtonPlus;

    final static boolean USB_ENABLE = true;
    final static boolean USB_ONLY_OUTPUT = true;

    private Boolean isLocationServiceRunning = false;
    private Boolean isBindStarted = false;

    private EditText searchPlace;

    private CalculateLocation calculateLocation;

    private ProgressDialog progressDialog;
    private BleFileExplorer bleFileExplorer;

    FloatingActionButton fabSetMarker;
    FloatingActionButton fabUndo;
    FloatingActionButton fabNowLocation;

    enum LogPlayMode {
        LOG_PLAY_1x,
        LOG_PLAY_2x,
        LOG_PLAY_10x,
        LOG_PLAY_MINUS_1x,
        LOG_PLAY_MINUS_2x,
        LOG_PLAY_MINUS_10x
    }
    private LogPlayMode logPlayMode = LogPlayMode.LOG_PLAY_1x;
    private RelativeLayout logPlayLayout;
    private TextView logPlayCurrentTimeText;
    private TextView logPlayTotalTimeText;
    private SeekBar logPLaySeekBar;
    private TextView logPlayModeText;
    private FloatingActionButton logPlayToggleFab;
    private FloatingActionButton logPlayNextFab;
    private FloatingActionButton logPlayPreviousFab;
    private boolean isPlayingLog = false;
    final Handler logPlayHandler = new Handler();
    final Runnable logPLayRunnable = new Runnable() {
        @Override
        public void run() {
            if (logPlayMode == LogPlayMode.LOG_PLAY_1x || logPlayMode == LogPlayMode.LOG_PLAY_2x || logPlayMode == LogPlayMode.LOG_PLAY_10x) {
                if (locationLogger.getNextPoint() == null) {
                    logPlayToggleFab.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    logPlayToggleFab.setImageResource(android.R.drawable.ic_media_play);
                    logPlayHandler.removeCallbacks(this);
                    isPlayingLog = false;
                    return;
                }
                int second = locationLogger.drawNextPoint().locationDate.getTimeInSec();
                if (locationLogger.getNextPoint() == null) {
                    logPlayToggleFab.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    logPlayToggleFab.setImageResource(android.R.drawable.ic_media_play);
                    logPlayHandler.removeCallbacks(this);
                    isPlayingLog = false;
                    return;
                }
                int nextSecond = locationLogger.getNextPoint().locationDate.getTimeInSec();
                int delay = (nextSecond - second) * 1000;
                if (delay <= 0) {
                    delay = 1;
                }
                switch (logPlayMode) {
                    case LOG_PLAY_1x:
                        delay = delay / 1;
                        break;
                    case LOG_PLAY_2x:
                        delay = delay / 2;
                        break;
                    case LOG_PLAY_10x:
                        delay = delay / 10;
                    default:
                        break;
                }
                logPlayHandler.postDelayed(this, delay);

            } else if (logPlayMode == LogPlayMode.LOG_PLAY_MINUS_1x || logPlayMode == LogPlayMode.LOG_PLAY_MINUS_2x || logPlayMode == LogPlayMode.LOG_PLAY_MINUS_10x) {
                if (locationLogger.getPreviousPoint() == null) {
                    logPlayToggleFab.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    logPlayToggleFab.setImageResource(android.R.drawable.ic_media_play);
                    logPlayHandler.removeCallbacks(this);
                    isPlayingLog = false;
                    return;
                }
                int second = locationLogger.drawPrevioutPoint().locationDate.getTimeInSec();
                if (locationLogger.getPreviousPoint() == null) {
                    logPlayToggleFab.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    logPlayToggleFab.setImageResource(android.R.drawable.ic_media_play);
                    logPlayHandler.removeCallbacks(this);
                    isPlayingLog = false;
                    return;
                }
                int previousSecond = locationLogger.getPreviousPoint().locationDate.getTimeInSec();

                int delay = (second - previousSecond) * 1000;
                if (delay <= 0) {
                    delay = 1;
                }
                switch (logPlayMode) {
                    case LOG_PLAY_MINUS_1x:
                        delay = delay / 1;
                        break;
                    case LOG_PLAY_MINUS_2x:
                        delay = delay / 2;
                        break;
                    case LOG_PLAY_MINUS_10x:
                        delay = delay / 10;
                    default:
                        break;
                }
                logPlayHandler.postDelayed(this, delay);
            }
        }
    };



    public void requestReadWritePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if ((checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                && (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Log.i("Permission", "Read Write Requested");
            parameter.readParamFromCsv();
            startLocationService();
            startBind();
            requestLocationPermission();
        }
    }

    @Override
    public void showAlertMessage(Boolean enable, String message) {
        if (alertText == null) {
            return;
        }
        alertText.setText(message);
        if (enable) {
            alertText.setVisibility(View.VISIBLE);
            alertConfirm.setVisibility(View.VISIBLE);
            alertLayout.setVisibility(View.VISIBLE);
        } else {
            alertText.setVisibility(View.INVISIBLE);
            alertConfirm.setVisibility(View.VISIBLE);
            alertLayout.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void showBfeProgressDialog(Boolean enable, String message) {
        if (progressDialog == null) {
            return;
        }
        progressDialog.setMessage(message);
        if (enable) {
            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }
            progressDialog.show();
        } else {
            progressDialog.cancel();
        }
    }
    @Override
    public void showBfeToast(String message){
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    public int getBatteryPercent() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = (level / (float) scale) * 100;
        return  (int)batteryPct;
    }

    private MotionEvent event;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.d("TouchEvent", "X:" + event.getX() + ",Y:" + event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.event = event;
                Log.d("TouchEvent", "getAction()" + "ACTION_DOWN");
                break;
            case MotionEvent.ACTION_UP:
                this.event = event;
                Log.d("TouchEvent", "getAction()" + "ACTION_UP");
                break;
            case MotionEvent.ACTION_MOVE:
                this.event = event;
                Log.d("TouchEvent", "getAction()" + "ACTION_MOVE");
                break;
            case MotionEvent.ACTION_CANCEL:
                this.event = event;
                Log.d("TouchEvent", "getAction()" + "ACTION_CANCEL");
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestReadWritePermission();

        Context mContext = getApplicationContext();
        Configuration.getInstance().load(mContext, PreferenceManager.getDefaultSharedPreferences(mContext));

        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        imageView.setColorFilter(Color.RED);

        progressDialog = new ProgressDialog(this);

        alertText = findViewById(R.id.alertText);
        alertText.setVisibility(View.INVISIBLE);
        alertLayout = findViewById(R.id.alertLayout);
        alertLayout.setVisibility(View.INVISIBLE);
        alertConfirm = findViewById(R.id.alertConfirmButton);
        alertConfirm.setVisibility(View.INVISIBLE);
        alertConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertMessage(false, alertText.getText().toString());
            }
        });

        mMapView = findViewById(R.id.map);
        mMapView.setMinZoomLevel(4.0);

        mMapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });

        dangerArea = new DangerArea(mMapView);
        parameter = new Parameter();

        dangerArea.addAreaData(parameter.areaData);

        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);

        final IMapController mapController = mMapView.getController();
        mapController.setZoom(14.0);
        mapController.setCenter(lastLocation);

        RotationGestureOverlay mRotate = new RotationGestureOverlay(this, mMapView);
        mRotate.setEnabled(false);
        mMapView.getOverlays().add(mRotate);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                Parameter temp = new Parameter();
                temp.readParamFromCsv();
                setUserIdText(temp.getUserID());
                setGroupIdText(temp.getGroupID());
                parameter.setUserID(temp.getUserID());
                parameter.setGroupID(temp.getGroupID());
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        userIdText = headerView.findViewById(R.id.textView1);
        groupIdText = headerView.findViewById(R.id.textView2);
        userIdText.setText("USER ID : " + parameter.userID);
        groupIdText.setText("GROUP : " + parameter.groupID);
        View searchView = headerView.findViewById(R.id.searchView);
        searchView.setBackgroundColor(Color.WHITE);

        searchPlace = headerView.findViewById(R.id.searchText);
        searchPlace.setFocusedByDefault(false);
        searchPlace.setFocusedByDefault(false);

        searchPlace.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_DONE) && (!searchPlace.getText().toString().isEmpty())) {
                    keyword = searchPlace.getText().toString() + " 日本";
                    new SearchGeocode().execute();
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });

        searchPlace.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    searchPlace.setCursorVisible(true);
                } else {
                    searchPlace.setCursorVisible(false);
                }
            }
        });
        searchPlace.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        ImageButton searchButton = headerView.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchPlace.getText().toString() != "") {
                    keyword = searchPlace.getText().toString() + "日本";
                    new SearchGeocode().execute();
                }
            }
        });

        fabSetMarker = (FloatingActionButton) findViewById(R.id.fab_SetMarker);
        fabSetMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dangerArea.geoPointArray.size() >= 6 && !isEditMode) {
                    Toast.makeText(getApplicationContext(), "領域の頂点は6つ以下である必要があります", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (dangerArea.areaDataArray.size() >= 10) {
                    Toast.makeText(getApplicationContext(), "設定できる領域は10個までです", Toast.LENGTH_SHORT).show();
                    return;
                }
                double clat = mMapView.getMapCenter().getLatitude();
                double clon = mMapView.getMapCenter().getLongitude();
                GeoPoint center = new GeoPoint(clat, clon);
                //dangerArea.setMarker(center,ColorMarker.marker_blue);

                if (dangerArea.geoPointArray.size() >= 1) {
                    double lastLat = dangerArea.geoPointArray.get(dangerArea.geoPointArray.size() - 1).getLatitude();
                    double lastLon = dangerArea.geoPointArray.get(dangerArea.geoPointArray.size() - 1).getLongitude();

                    boolean boolLat = (lastLat == center.getLatitude());
                    boolean boolLon = (lastLon == center.getLongitude());

                    if (boolLat && boolLon) {
                        //message : please choose another point!
                        return;
                    }
                }

                dangerArea.addPoint(center);
                if (dangerArea.isPolygonDrawable()) {
                    if (dangerArea.isPolygonSet()) {
                        dangerArea.cleanDraftPolygon();
                    }
                    dangerArea.setDraftPolygon(dangerArea.geoPointArray);
                    dangerArea.drawDraftPolygon();
                }
                isEditMode = false;
            }
        });

        fabUndo = (FloatingActionButton) findViewById(R.id.fab_Undo);
        fabUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dangerArea.undoDraftPolygon();
            }
        });

        final ProgressBar calcProgress = findViewById(R.id.calculateGpsProgress);
        calcProgress.setVisibility(ProgressBar.INVISIBLE);

        fabNowLocation = (FloatingActionButton) findViewById(R.id.fab_NowLocation);
        fabNowLocation.setOnClickListener(new View.OnClickListener() {
            Boolean isLocationCalculated = false;

            @Override
            public void onClick(View view) {

                fabNowLocation.setEnabled(false);

                calcProgress.setVisibility(ProgressBar.VISIBLE);
                calcProgress.setMax(10);

                final Handler handler = new Handler();
                final Runnable r = new Runnable() {
                    int cnt = 0;

                    @Override
                    public void run() {
                        cnt++;
                        if (cnt > 10) {
                            calcProgress.setVisibility(ProgressBar.INVISIBLE);
                            fabNowLocation.setEnabled(true);
                            return;
                        }
                        calcProgress.setProgress(cnt, true);
                        handler.postDelayed(this, 1000);
                    }
                };
                handler.post(r);

                calculateLocation = new CalculateLocation(MainActivity.this, 10, 1, 0, new CalculateLocationCallback() {
                    @Override
                    public void onLocationCalculated(Location location) {
                        isLocationCalculated = true;
                        Log.i("CalcLocation", location.toString());
                        //Toast.makeText(getApplicationContext(), location.toString(), Toast.LENGTH_SHORT).show();
                        location.getAccuracy();

                        GeoPoint calcLocation = new GeoPoint((location.getLatitude()), location.getLongitude());
                        mapController.animateTo(calcLocation);
                    }

                    @Override
                    public void onStopGPS() {
                        if (!isLocationCalculated) {
                            Toast.makeText(getApplicationContext(), "位置情報を取得できませんでした", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                calculateLocation.startGPS();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        calculateLocation.stopGPS();
                        calcProgress.setVisibility(ProgressBar.INVISIBLE);

                    }
                }, 10 * 1000);
            }
        });

        FloatingActionButton fabDetermineArea = (FloatingActionButton) findViewById(R.id.fab_AreaDetermine);
        fabDetermineArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dangerArea.determineAreaData();
                if (isEditMode) isEditMode = false;
            }
        });

        FloatingActionButton fabOpenDrawer = (FloatingActionButton) findViewById(R.id.fab_OpenDrawer);
        fabOpenDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(Gravity.START);
            }
        });

        final int accent = fetchAccentColor();
        final int primary = fetchPrimaryColor();

        final com.robertlevonyan.views.customfloatingactionbutton.FloatingActionButton fabToggleLogging = findViewById(R.id.fab_ToggleLogging);
        fabToggleLogging.setFabColor(primary);
        fabToggleLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationService.isAreaWatchingStarted == false) {
                    locationService.toggleAreaWatching();
                    fabToggleLogging.setFabText("判定中");
                    fabToggleLogging.setFabTextColor(primary);
                    fabToggleLogging.setFabIconColor(primary);
                    fabToggleLogging.setFabColor(accent);
                    //fabToggleLogging.invalidate();
                } else {
                    locationService.toggleAreaWatching();
                    fabToggleLogging.setFabText("判定開始");
                    fabToggleLogging.setFabTextColor(accent);
                    fabToggleLogging.setFabIconColor(accent);
                    fabToggleLogging.setFabColor(primary);
                }
            }
        });

        logRemoveButton = findViewById(R.id.logRemoveButton);
        logRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlayingLog && (logPlayHandler != null) && (logPLayRunnable != null)) {
                    logPlayHandler.removeCallbacks(logPLayRunnable);
                }
                targetIconFadein();

                logPlayToggleFab.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                logPlayToggleFab.setImageResource(android.R.drawable.ic_media_play);
                //logPlayHandler.removeCallbacks(logPLayRunnable);
                isPlayingLog = false;

                locationLogger.removeLogLine();
                locationLogger.removeLogArea();
                logRemoveButton.setVisibility(View.INVISIBLE);
                logPlayLayout.setVisibility(View.INVISIBLE);

                fabSetMarker.setVisibility(View.VISIBLE);
                fabUndo.setVisibility(View.VISIBLE);
                fabNowLocation.setVisibility(View.VISIBLE);
            }
        });
        logRemoveButton.setVisibility(View.INVISIBLE);

        /////////////////////////////////////////////////////////////////////
        debugButtonPlus = findViewById(R.id.debugButtonPlus);
        debugButtonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IGeoPoint point = mMapView.getMapCenter();
                Location location = new Location("dummy");
                location.setLatitude(point.getLatitude());
                location.setLongitude(point.getLongitude());
                locationService.gpsChanged(location);
            }
        });
        /////////////////////////////////////////////////////////////////////

        int x = getResources().getDisplayMetrics().widthPixels / 15;
        int y = getResources().getDisplayMetrics().heightPixels / 7;

        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mMapView);
        mMapView.getOverlays().add(scaleBarOverlay);
        scaleBarOverlay.setEnableAdjustLength(true);
        //scaleBarOverlay.setScaleBarOffset((int) (getResources().getDisplayMetrics().widthPixels / 2 - getResources().getDisplayMetrics().xdpi / 2), 10);
        scaleBarOverlay.setScaleBarOffset(x, y);

        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(getApplicationContext());
        mMapView.getOverlays().add(copyrightOverlay);
        x = getResources().getDisplayMetrics().widthPixels / 2 - 40;
        copyrightOverlay.setOffset(x, 5);

        logPlayLayout = findViewById(R.id.logPlayLayout);
        logPLaySeekBar = findViewById(R.id.logPlaySeekBar);
        logPlayModeText = findViewById(R.id.logPlayModeText);
        logPlayCurrentTimeText = findViewById(R.id.logPlayCurrentTimeText);
        logPlayTotalTimeText = findViewById(R.id.logPlayTotalTimeText);
        logPlayToggleFab = findViewById(R.id.logPlayToggle);
        logPlayNextFab = findViewById(R.id.logPlayNext);
        logPlayPreviousFab = findViewById(R.id.logPlayPrevious);

        logPLaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                locationLogger.drawSeekedPoint(progress).locationDate.getTimeString();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        logPlayToggleFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlayingLog) {
                    logPlayToggleFab.setBackgroundColor(Color.GRAY);
                    logPlayToggleFab.setImageResource(android.R.drawable.ic_media_pause);
                    logPlayToggleFab.invalidate();
                    logPlayHandler.post(logPLayRunnable);
                    isPlayingLog = true;
                } else {
                    logPlayToggleFab.setBackgroundColor(accent);
                    logPlayToggleFab.setImageResource(android.R.drawable.ic_media_play);
                    logPlayToggleFab.invalidate();
                    logPlayHandler.removeCallbacks(logPLayRunnable);
                    isPlayingLog = false;
                }
            }
        });

        logPlayNextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //locationLogger.drawNextPoint();
                if (!isPlayingLog) {
                    //return;
                }
                switch (logPlayMode){
                    case LOG_PLAY_1x:
                        logPlayMode = LogPlayMode.LOG_PLAY_2x;
                        logPlayModeText.setText("2x");
                        break;
                    case LOG_PLAY_2x:
                        logPlayMode = LogPlayMode.LOG_PLAY_10x;
                        logPlayModeText.setText("10x");
                        break;
                    case LOG_PLAY_MINUS_1x:
                        logPlayMode= LogPlayMode.LOG_PLAY_1x;
                        logPlayModeText.setText("1x");
                        break;
                    case LOG_PLAY_MINUS_2x:
                        logPlayMode = LogPlayMode.LOG_PLAY_MINUS_1x;
                        logPlayModeText.setText("-1x");
                        break;
                    case LOG_PLAY_MINUS_10x:
                        logPlayMode = LogPlayMode.LOG_PLAY_MINUS_2x;
                        logPlayModeText.setText("-2x");
                        break;
                    default:
                        break;
                }
            }
        });

        logPlayPreviousFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //locationLogger.drawPrevioutPoint();
                if (!isPlayingLog) {
                    //return;
                }
                switch (logPlayMode){
                    case LOG_PLAY_1x:
                        logPlayMode = LogPlayMode.LOG_PLAY_MINUS_1x;
                        logPlayModeText.setText("-1x");
                        break;
                    case LOG_PLAY_2x:
                        logPlayMode = LogPlayMode.LOG_PLAY_1x;
                        logPlayModeText.setText("1x");
                        break;
                    case LOG_PLAY_10x:
                        logPlayMode = LogPlayMode.LOG_PLAY_2x;
                        logPlayModeText.setText("2x");
                        break;
                    case LOG_PLAY_MINUS_1x:
                        logPlayMode= LogPlayMode.LOG_PLAY_MINUS_2x;
                        logPlayModeText.setText("-2x");
                        break;
                    case LOG_PLAY_MINUS_2x:
                        logPlayMode = LogPlayMode.LOG_PLAY_MINUS_10x;
                        logPlayModeText.setText("-10x");
                        break;
                    default:
                        break;
                }
            }
        });


        initFragments();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    void restart(Context cnt, int period) {
        // intent 設定で自分自身のクラスを設定
        Intent mainActivity = new Intent(cnt, MainActivity.class);

        // PendingIntent , ID=0
        PendingIntent pendingIntent = PendingIntent.getActivity(cnt,
                0, mainActivity, PendingIntent.FLAG_CANCEL_CURRENT);

        // AlarmManager のインスタンス生成
        AlarmManager alarmManager = (AlarmManager) cnt.getSystemService(
                Context.ALARM_SERVICE);

        // １回のアラームを現在の時間からperiod（５秒）後に実行させる
        if (alarmManager != null) {
            long trigger = System.currentTimeMillis() + period;
            alarmManager.setExact(AlarmManager.RTC, trigger, pendingIntent);
        }
        // アプリ終了
        finish();
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        return false;
    }

    private boolean DATA_RECEIVE = false;
    private String receivedData = "";

    private SerialInputOutputManager mSerialIoManager;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final SerialInputOutputManager.Listener usbSerialListener =
            new SerialInputOutputManager.Listener() {
                @Override
                public void onRunError(Exception e) {
                    Log.d("USB", "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            String dataStr = HexDump.dumpHexString(data);

                            if (dataStr.contains("GGA") && dataStr.contains(",E,")) {
                                String delim = ",";
                                StringTokenizer stringTokenizer = new StringTokenizer(dataStr, delim);
                                int length = stringTokenizer.countTokens();
                                ArrayList<String> tokens = new ArrayList<>();
                                for (int i = 0; i < length; i++) {
                                    tokens.add(stringTokenizer.nextToken());
                                }

                                String latStr = tokens.get(2);
                                Double latDeg = Double.parseDouble(latStr.substring(0, 2));
                                Double latMin = Double.parseDouble(latStr.substring(2, 9));
                                //Double latitude = getRoundedDouble((latDeg + latMin / 60), 7);
                                Double latitude = (latDeg + latMin / 60);

                                String lonStr = tokens.get(4);
                                Double lonDeg = Double.parseDouble(lonStr.substring(0, 3));
                                Double lonMin = Double.parseDouble(lonStr.substring(3, 10));
                                //Double longitude = getRoundedDouble((lonDeg + lonMin / 60), 7);
                                Double longitude = (lonDeg + lonMin / 60);

                                lastLocation = new GeoPoint(latitude, longitude);

                                /////////////////////////////////////////////////////////////////////
                                if (USB_ONLY_OUTPUT == true) {
                                    Location location = new Location("dummy");
                                    location.setLatitude(latitude);
                                    location.setLongitude(longitude);
                                    locationService.gpsChanged(location);
                                }
                                /////////////////////////////////////////////////////////////////////

                                //Toast.makeText(MainActivity.this, latDeg+"度"+latMin+"分,"+lonDeg+"度"+lonMin+"分", Toast.LENGTH_SHORT).show();
                                Toast.makeText(MainActivity.this, latitude.toString() + "," + longitude.toString(), Toast.LENGTH_SHORT).show();
                                IMapController mapController = mMapView.getController();
                                mapController.animateTo(lastLocation);
                            }

                            if (dataStr.contains("RMC") && dataStr.contains(",E,")) {
                                String delim = ",";
                                StringTokenizer stringTokenizer = new StringTokenizer(dataStr, delim);
                                int length = stringTokenizer.countTokens();
                                ArrayList<String> tokens = new ArrayList<>();
                                for (int i = 0; i < length; i++) {
                                    tokens.add(stringTokenizer.nextToken());
                                }

                                String latStr = tokens.get(3);
                                Double latDeg = Double.parseDouble(latStr.substring(0, 2));
                                Double latMin = Double.parseDouble(latStr.substring(2, 9));
                                Double latitude = getRoundedDouble((latDeg + latMin / 60), 7);

                                String lonStr = tokens.get(5);
                                Double lonDeg = Double.parseDouble(lonStr.substring(0, 3));
                                Double lonMin = Double.parseDouble(lonStr.substring(3, 10));
                                Double longitude = getRoundedDouble((lonDeg + lonMin / 60), 7);

                                lastLocation = new GeoPoint(latitude, longitude);

                                Toast.makeText(MainActivity.this, latitude.toString() + "," + longitude.toString(), Toast.LENGTH_SHORT).show();
                                IMapController mapController = mMapView.getController();
                                mapController.animateTo(lastLocation);
                            } else {

                            }
                        }
                    });
                }
            };

    @Override
    public void onResume() {
        super.onResume();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "r/w permission is already granted");
            startLocationService();
            startBind();
        }

        if (USB_ENABLE) {
            if (MainActivity.USB_ONLY_OUTPUT) {
                //showAlertMessage(true, "USB_ONLY_MODE_ON");
            }
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
            if (availableDrivers.isEmpty()) {
                //Toast.makeText(MainActivity.this, "available driver empty", Toast.LENGTH_SHORT).show();
                return;
            }

            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {
                // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
                Toast.makeText(MainActivity.this, "connection failed", Toast.LENGTH_SHORT).show();
                return;
            }

            final UsbSerialPort port = driver.getPorts().get(0);
            try {
                port.open(connection);
                port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                mSerialIoManager = new SerialInputOutputManager(port, usbSerialListener);
                mExecutor.submit(mSerialIoManager);

                byte buffer[] = new byte[256];
                int numBytesRead = port.read(buffer, 1000);
                //Toast.makeText(MainActivity.this, buffer.toString(), Toast.LENGTH_SHORT).show();
                Log.d("USB", "Read " + numBytesRead + " bytes.");

            } catch (IOException e) {
                // Deal with error.
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mMapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onStart() {
        super.onStart();
        int battery = getBatteryPercent();
        if (battery < 30) {
            //showAlertMessage(true, "バッテリー残量低下 : " + battery + "%" + "\r\n記録中に電源が落ちる可能性があります");
        }
        Log.i("LocationService", "Bind on [MainActivity]");
        startBind();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("LocationService", "Bind off [MainActivity]");
        stopBind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("destroy", "destroy");
        stopLocationService();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Boolean checkAdministration() {
        Boolean isAdmin = parameter.isAdmin();
        if (isAdmin == false) {
            //alert; you don't have admin, try it?
            Toast.makeText(MainActivity.this, "この操作を行う権限がありません\r\n設定→ユーザー設定から権限を設定して下さい", Toast.LENGTH_SHORT).show();
        }
        return isAdmin;
    }

    private Boolean checkAdministration(Parameter parameter) {
        Boolean isAdmin = parameter.isAdmin();
        if (isAdmin == false) {
            //alert; you don't have admin, try it?
            Toast.makeText(MainActivity.this, "この操作を行う権限がありません\r\n設定→ユーザー設定から権限を設定して下さい", Toast.LENGTH_SHORT).show();
        }
        return isAdmin;
    }

    enum FRAGMENT_TYPE {
        read_area,
        read_history,
        setting,
        child_setting,
        setting_receive,
//        devices_scan
    }

    ReadAreaFragment readAreaFragment;
    ReadHistoryFragment readHistoryFragment;
    SettingFragment settingFragment;
    ChildSettingFragment childSettingFragment;
    SettingReceiveFragment settingReceiveFragment;
    //    DeviceScanFragment deviceScanFragment;
    android.support.v4.app.FragmentTransaction transaction;

    private void initFragments() {
        readAreaFragment = new ReadAreaFragment();
        readAreaFragment.setCallback(MainActivity.this);
        readHistoryFragment = new ReadHistoryFragment();
        readHistoryFragment.setCallback(this);
        settingFragment = new SettingFragment();
        childSettingFragment = new ChildSettingFragment();
        settingReceiveFragment = new SettingReceiveFragment();
//        deviceScanFragment = new DeviceScanFragment();
    }

    private void showFragment(FRAGMENT_TYPE type) {
        initFragments();

        transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.addToBackStack(null);

        Bundle bundle = new Bundle();
        ArrayList<Boolean> invalidArray;

        //temporary data to send to each fragments
        Parameter tempParameter = new Parameter();
        tempParameter.readParamFromCsv();

        switch (type) {
            case read_area:
                transaction.replace(R.id.fragment, readAreaFragment);
                break;

            case read_history:
                transaction.replace(R.id.fragment, readHistoryFragment);

                break;

            case setting:
                bundle = new Bundle();
                //bundle.putSerializable("PARAMETER", parameter);
                bundle.putParcelable("PARAMETER", parameter);
                settingFragment.setArguments(bundle);
                transaction.replace(R.id.fragment, settingFragment);
                break;

            case child_setting:
                invalidArray = new ArrayList<>();
                for (int areaNum = 0; areaNum <= dangerArea.areaDataArray.size() - 1; areaNum++) {
                    if (dangerArea.areaDataArray.get(areaNum).isInvalid) {
                        invalidArray.add(areaNum, true);
                    } else {
                        invalidArray.add(areaNum, false);
                    }
                }
                bundle.putParcelable("PARAMETER", tempParameter);
                bundle.putSerializable("INVALIDAREA", invalidArray);
                childSettingFragment.setArguments(bundle);
                transaction.replace(R.id.fragment, childSettingFragment);
                break;

            case setting_receive:
                transaction.replace(R.id.fragment, settingReceiveFragment);
                bundle = new Bundle();
                bundle.putParcelable("PARAMETER", parameter);
                settingReceiveFragment.setArguments(bundle);
                break;
//            case devices_scan:
//                invalidArray = new ArrayList<>();
//                for (int areaNum = 0; areaNum <= dangerArea.areaDataArray.size() - 1; areaNum++) {
//                    if (dangerArea.areaDataArray.get(areaNum).isInvalid) {
//                        invalidArray.add(areaNum, true);
//                    } else {
//                        invalidArray.add(areaNum, false);
//                    }
//                }
//                bundle.putSerializable("PARAMETER", parameter);
//                bundle.putSerializable("INVALIDAREA", invalidArray);
//                deviceScanFragment.setArguments(bundle);
//                transaction.replace(R.id.fragment, deviceScanFragment);
//                break;
        }
        transaction.commit();

        int bacstackCount = getSupportFragmentManager().getBackStackEntryCount();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        View v = getWindow().getDecorView().getRootView();

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_read_area && (checkAdministration())) {

            showFragment(FRAGMENT_TYPE.read_area);

//            ReadAreaFragment readAreaFragment = new ReadAreaFragment();
//            readAreaFragment.setCallback(MainActivity.this);
//            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right);
//            transaction.replace(R.id.fragment, readAreaFragment);
//            transaction.commit();

        } else if (id == R.id.nav_delete_area && (checkAdministration())) {
            requestReadWritePermission();
            new AlertDialog.Builder(this)
                    .setTitle("全領域を削除します")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //dangerArea.removeAll();
                            parameter.clearAreaData();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        } else if (id == R.id.nav_save_area && (checkAdministration())) {
            int savedAreaNum = 0;
            String path = MainActivity.PATH_MAIN_DIRECTORY;
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();
            Log.d("Files", "Size: " + files.length);
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().contains(".area")) {
                    savedAreaNum++;
                }
            }
            if (savedAreaNum > 9) {
                Toast.makeText(MainActivity.this, "保存できるのは10件までです", Toast.LENGTH_SHORT).show();
                return true;
            }
            requestReadWritePermission();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("名前を付けて保存");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String title = input.getText().toString();
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);
                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                    String formatDate = format.format(calendar.getTime());
                    dangerArea.outputAreaData(PATH_MAIN_DIRECTORY, title + ".area");
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();

        } else if (id == R.id.nav_confirm_log && (checkAdministration())) {

            requestReadWritePermission();
            getAreaDataListFromExternalStorage();
            showFragment(FRAGMENT_TYPE.read_history);

        } else if (id == R.id.nav_setting) {
            requestReadWritePermission();
            showFragment(FRAGMENT_TYPE.setting);

        } else if (id == R.id.nav_child_setting && (checkAdministration())) {
            requestReadWritePermission();
            showFragment(FRAGMENT_TYPE.child_setting);

        } else if (id == R.id.nav_setting_receive) {
            requestReadWritePermission();
            showFragment(FRAGMENT_TYPE.setting_receive);

        } else if (id == R.id.nav_download_map && (checkAdministration())) {

            final EditText input = new EditText(MainActivity.this);
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
            builder.setTitle("地図の保存");
            builder.setView(input);
            builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    double zoomLevel = mMapView.getZoomLevelDouble();
                    currentMapDownloader((int) zoomLevel, 0, PATH_MAP, input.getText().toString() + ".sqlite");
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(input.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            final android.support.v7.app.AlertDialog dialog = builder.create();
            dialog.show();

            ((android.support.v7.app.AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (TextUtils.isEmpty(s)) {
                        ((android.support.v7.app.AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        ((android.support.v7.app.AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            });

        } else if (id == R.id.nav_open_map && (checkAdministration())) {
            promptForFiles();
        } else if (id == R.id.nav_upload_data) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("PARAMETER", parameter);

            if (bleFileExplorer != null) {
                android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(bleFileExplorer).commit();
            }
            bleFileExplorer = new BleFileExplorer();
            bleFileExplorer.setCallback(MainActivity.this);
            bleFileExplorer.setArguments(bundle);
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right);
            transaction.replace(R.id.fragment, bleFileExplorer);
            transaction.commit();

        } else if (id == R.id.nav_help) {
//            showFragment(FRAGMENT_TYPE.devices_scan);
        }

        if (parameter.isAdmin() | (id == R.id.nav_setting) | (id == R.id.nav_setting_receive) | (id == R.id.nav_help)) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    private void promptForFiles() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.MULTI_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        //properties.root = new File(DialogConfigs.DEFAULT_DIR);
        //(Configuration.getInstance().getOsmdroidBasePath());
        properties.root = new File(PATH_MAP);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);

        Set<String> registeredExtensions = ArchiveFileFactory.getRegisteredExtensions();
        //api check
        if (Build.VERSION.SDK_INT >= 14)
            registeredExtensions.add("gpkg");
        if (Build.VERSION.SDK_INT >= 10)
            registeredExtensions.add("map");

        String[] ret = new String[registeredExtensions.size()];
        ret = registeredExtensions.toArray(ret);
        properties.extensions = ret;

        FilePickerDialog dialog = new FilePickerDialog(this, properties);
        dialog.setTitle("Select a File");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files is the array of the paths of files selected by the Application User.
                setProviderConfig(files);
            }
        });
        dialog.show();
    }

    private void setProviderConfig(String[] files) {
        if (files == null || files.length == 0)
            return;
        SimpleRegisterReceiver simpleRegisterReceiver = new SimpleRegisterReceiver(this);
        if (tileWriter != null)
            tileWriter.onDetach();

        if (Build.VERSION.SDK_INT < 10) {
            tileWriter = new TileWriter();
        } else {
            tileWriter = new SqlTileWriter();
        }

        tileSources.clear();
        List<MapTileModuleProviderBase> providers = new ArrayList<>();
        providers.add(new MapTileAssetsProvider(simpleRegisterReceiver, this.getAssets()));

        List<File> geopackages = new ArrayList<>();
        List<File> forgeMaps = new ArrayList<>();
        List<IArchiveFile> archives = new ArrayList<>();
        //this part seperates the geopackage and maps forge stuff since they are handled differently
        for (int i = 0; i < files.length; i++) {
            File archive = new File(files[i]);
            if (archive.getName().endsWith("gpkg")) {
                geopackages.add(archive);
            } else if (archive.getName().endsWith("map")) {
                forgeMaps.add(archive);
            } else {
                IArchiveFile temp = ArchiveFileFactory.getArchiveFile(archive);
                if (temp != null) {
                    Set<String> tileSources = temp.getTileSources();
                    Iterator<String> iterator = tileSources.iterator();
                    while (iterator.hasNext()) {

                        this.tileSources.add(FileBasedTileSource.getSource(iterator.next()));
                        archives.add(temp);
                    }
                }

            }
        }
        //setup the standard osmdroid-android library supported offline tile providers
        IArchiveFile[] archArray = new IArchiveFile[archives.size()];
        archArray = archives.toArray(archArray);
        final MapTileFileArchiveProvider mapTileFileArchiveProvider = new MapTileFileArchiveProvider(simpleRegisterReceiver, TileSourceFactory.DEFAULT_TILE_SOURCE, archArray);

        GeoPackageMapTileModuleProvider geopackage = null;
        GeoPackageProvider provider = null;
        //geopackages
        if (!geopackages.isEmpty()) {
            File[] maps = new File[geopackages.size()];
            maps = geopackages.toArray(maps);

            if (Build.VERSION.SDK_INT > 10) {
                GeoPackageManager manager = GeoPackageFactory.getManager(this);

                // Import database
                for (File f : maps) {
                    try {
                        boolean imported = manager.importGeoPackage(f);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                provider = new GeoPackageProvider(maps, this);
                geopackage = provider.geoPackageMapTileModuleProvider();
                providers.add(geopackage);
                List<GeopackageRasterTileSource> geotileSources = new ArrayList<>();
                geotileSources.addAll(geopackage.getTileSources());
                tileSources.addAll(geotileSources);
                //TODO add feature tiles here too
            }
        }
        MapsForgeTileModuleProvider moduleProvider = null;
        if (!forgeMaps.isEmpty()) {
            //fire up the forge maps...
            XmlRenderTheme theme = null;
            try {
                theme = new AssetsRenderTheme(this.getApplicationContext(), "renderthemes/", "rendertheme-v4.xml");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            File[] forge = new File[forgeMaps.size()];
            forge = forgeMaps.toArray(forge);
            MapsForgeTileSource fromFiles = MapsForgeTileSource.createFromFiles(forge, theme, "rendertheme-v4");
            tileSources.add(fromFiles);
            // Create the module provider; this class provides a TileLoader that
            // actually loads the tile from the map file.
            moduleProvider = new MapsForgeTileModuleProvider(simpleRegisterReceiver, fromFiles, tileWriter);
        }

        final MapTileApproximater approximationProvider = new MapTileApproximater();
        approximationProvider.addProvider(mapTileFileArchiveProvider);
        if (geopackage != null) {
            providers.add(geopackage);
            approximationProvider.addProvider(geopackage);
        }
        if (moduleProvider != null) {
            providers.add(moduleProvider);
            approximationProvider.addProvider(moduleProvider);
        }
        providers.add(mapTileFileArchiveProvider);
        providers.add(approximationProvider);
        MapTileModuleProviderBase[] providerArray = new MapTileModuleProviderBase[providers.size()];
        for (int i = 0; i < providers.size(); i++) {
            providerArray[i] = providers.get(i);
        }
        MapTileProviderArray obj = new MapTileProviderArray(TileSourceFactory.DEFAULT_TILE_SOURCE, simpleRegisterReceiver, providerArray);
        mMapView.setTileProvider(obj);
        //ok everything is setup, we now have 0 or many tile sources available, ask the user
    }

    private void promptForTileSource() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        //builderSingle.setIcon(R.drawable.icon);
        builderSingle.setTitle("Select Offline Tile source:-");

        final ArrayAdapter<ITileSource> arrayAdapter = new ArrayAdapter<ITileSource>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(tileSources);

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final ITileSource strName = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(MainActivity.this);
                builderInner.setMessage(strName.name());
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mMapView.setTileSource(strName);//new XYTileSource(strName, 0, 22, 256, "png", new String[0]));
                        //on tile sources that are supported, center the map an area that's within bounds
                        if (strName instanceof MapsForgeTileSource) {
                            final MapsForgeTileSource src = (MapsForgeTileSource) strName;
                            mMapView.post(new Runnable() {
                                @Override
                                public void run() {
                                    mMapView.getController().setZoom(src.getMinimumZoomLevel());
                                    mMapView.setMinZoomLevel((double) src.getMinimumZoomLevel());
                                    mMapView.setMaxZoomLevel((double) src.getMaximumZoomLevel());
                                    mMapView.invalidate();
                                    mMapView.zoomToBoundingBox(src.getBoundsOsmdroid(), true);

                                    mMapView.setBuiltInZoomControls(false);
                                    mMapView.setMultiTouchControls(true);
                                    IMapController mapController = mMapView.getController();
                                    mapController.setZoom(14.0);
                                    GeoPoint startPoint = new GeoPoint(35.698353, 139.773114);
                                    mapController.setCenter(startPoint);

                                }
                            });
                        } else if (strName instanceof GeopackageRasterTileSource) {
                            final GeopackageRasterTileSource src = (GeopackageRasterTileSource) strName;
                            mMapView.post(new Runnable() {
                                @Override
                                public void run() {
                                    mMapView.getController().setZoom(src.getMinimumZoomLevel());
                                    mMapView.setMinZoomLevel((double) src.getMinimumZoomLevel());
                                    mMapView.setMaxZoomLevel((double) src.getMaximumZoomLevel());
                                    mMapView.invalidate();
                                    mMapView.zoomToBoundingBox(src.getBounds(), true);

                                    mMapView.setBuiltInZoomControls(false);
                                    mMapView.setMultiTouchControls(true);
                                    IMapController mapController = mMapView.getController();
                                    mapController.setZoom(14.0);
                                    GeoPoint startPoint = new GeoPoint(35.698353, 139.773114);
                                    mapController.setCenter(startPoint);
                                }
                            });
                        }

                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        //builderSingle.show();
    }


    public class CustomPolygon extends Polygon {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
            Projection pj = mapView.getProjection();
            GeoPoint eventPos = (GeoPoint) pj.fromPixels((int) event.getX(), (int) event.getY());
            boolean tapped = contains(event);
            if (tapped) {
                if (mOnClickListener == null) {

                    final CustomPolygon customPolygon = this;
                    final String[] items;
                    if (dangerArea.draftPolygon.equals(customPolygon)) {     //下書きの時は削除だけ表示する
                        items = new String[]{"削除"};
                    } else {
                        items = new String[]{"削除", "編集", "有効化", "無効化"};
                    }
                    int defaultItem = 0; // デフォルトでチェックされているアイテム
                    final List<Integer> checkedItems = new ArrayList<>();
                    checkedItems.add(defaultItem);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("")
                            .setSingleChoiceItems(items, defaultItem, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkedItems.clear();
                                    checkedItems.add(which);
                                }
                            })
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!checkedItems.isEmpty()) {
                                        Log.d("checkedItem:", "" + checkedItems.get(0));

                                        switch (checkedItems.get(0)) {
                                            case 0:
                                                if (dangerArea.draftPolygon.equals(customPolygon)) {
                                                    dangerArea.cleanMarker(dangerArea.markerArray);
                                                    dangerArea.cleanDraftPolygon();
                                                    dangerArea.markerArray.clear();
                                                    dangerArea.geoPointArray.clear();
                                                    mMapView.invalidate();
                                                } else {
                                                    dangerArea.removeAreaDataUseSpecifiedPolygon(customPolygon);
                                                }
                                                break;
                                            case 1:
                                                ArrayList<GeoPoint> points = new ArrayList<>(customPolygon.getPoints());
                                                if (dangerArea.draftPolygon.getPoints().size() != 0) {
                                                    //編集中の領域がある旨をアラートで出す必要あり
                                                    dangerArea.cleanMarker(dangerArea.markerArray);
                                                    dangerArea.cleanDraftPolygon();
                                                    dangerArea.markerArray.clear();
                                                    dangerArea.geoPointArray.clear();
                                                    mMapView.invalidate();
                                                    //dangerArea.removeAreaDataUseSpecifiedPolygon(customPolygon);
                                                } else {
                                                    //dangerArea.removeAreaDataUseSpecifiedPolygon(customPolygon);
                                                }
                                                dangerArea.setPolygonToDraftPolygon(points);
                                                break;
                                            case 2:
                                                if (customPolygon.getStrokeColor() == Color.GREEN) {
                                                    dangerArea.toggleAreaInvalidUseSpecifiedPolygon(customPolygon);
                                                }
                                                break;
                                            case 3:
                                                if (customPolygon.getStrokeColor() != Color.GREEN) {
                                                    dangerArea.toggleAreaInvalidUseSpecifiedPolygon(customPolygon);
                                                }
                                                break;
                                        }
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();

//                    new AlertDialog.Builder(MainActivity.this)
//                            .setTitle("選択した領域データ削除します")
//                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    if(dangerArea.draftPolygon.equals(customPolygon)){
//                                        dangerArea.cleanMarker(dangerArea.markerArray);
//                                        dangerArea.cleanDraftPolygon();
//                                        dangerArea.markerArray.clear();
//                                        dangerArea.geoPointArray.clear();
//                                        mMapView.invalidate();
//                                    }
//                                    dangerArea.removeAreaDataUseSpecifiedPolygon(customPolygon);
//                                }
//                            })
//                            .setNegativeButton("Cancel", null)
//                            .show();

                    return onClickDefault(this, mapView, eventPos);
                } else {
                    return mOnClickListener.onClick(this, mapView, eventPos);
                }
            } else
                return tapped;
        }
    }

    boolean isEditMode = false;

    public void setOnMarkerClickListner(Marker marker) {
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {

                if (marker.equals(dangerArea.highlightedMarker)) {

                    for (int i = 0; i <= 99; i++) {
                        dangerArea.highlightedMarker.remove(dangerArea.mapview);
                        dangerArea.mapview.invalidate();
                    }
                    dangerArea.beforePoint = null;

                    return true;
                }

                double density = getApplicationContext().getResources().getDisplayMetrics().density;
                int size = (int) (density * 30);

                Bitmap bitmap;
                Bitmap resizeBitmap;
                Drawable blue = getResources().getDrawable(R.drawable.marker_blue);
                bitmap = ((BitmapDrawable) blue).getBitmap();
                resizeBitmap = Bitmap.createScaledBitmap(bitmap, size * 2, size * 2, false);
                blue = new BitmapDrawable(getResources(), resizeBitmap);
                dangerArea.highlightedMarker.setIcon(blue);
                dangerArea.highlightedMarker.setPosition(marker.getPosition());
                dangerArea.highlightedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                dangerArea.drawMarker(dangerArea.highlightedMarker);

                dangerArea.beforeMarker = marker;
                dangerArea.beforePoint = marker.getPosition();

                isEditMode = true;

                return true;
            }
        });
    }

    public class DangerArea {
        private MapView mapview;
        private ArrayList<AreaData> areaDataArray;
        private ArrayList<GeoPoint> geoPointArray;
        private ArrayList<Marker> markerArray;
        private CustomPolygon draftPolygon;

        private Marker beforeMarker;
        private GeoPoint beforePoint;
        private Marker highlightedMarker;


        DangerArea(MapView mv) {
            mapview = mv;
            areaDataArray = new ArrayList<>();
            geoPointArray = new ArrayList<>();
            markerArray = new ArrayList<>();
            draftPolygon = new CustomPolygon();

            beforeMarker = new Marker(mapview);
            highlightedMarker = new Marker(mapview);
            setOnMarkerClickListner(highlightedMarker);
        }

        //禁止領域とその近接領域のクラス
        public class AreaData {
            private Boolean isInvalid = false;
            private CustomPolygon areaPolygon;
            //private ArrayList<CustomPolygon> expandedPolygonArray;
            private CustomPolygon expandedPolygon;
            private CustomPolygon semiExpandedPolygon;

            //コンストラクタでフィールドへの代入と地図上への描画を行います。
            AreaData(final CustomPolygon polygon) {
                areaPolygon = polygon;
                //expandedPolygonArray = (ArrayList<CustomPolygon>) getExpandedPolygonArray(polygon, 0.00005);
                //expandedPolygon = generateExpandedPolygon(polygon,0.00005);
                //expandedPolygon = getFixedExpandedPolygon((ArrayList<GeoPoint>) polygon.getPoints(),0.001);   //size[m]
                expandedPolygon = getFixedExpandedPolygon(polygon, parameter.getCloseDistance());
                semiExpandedPolygon = getFixedExpandedPolygon(polygon, 20);
                //drawPolygon(semiExpandedPolygon);
                drawPolygon(expandedPolygon);
                drawPolygon(polygon);
            }

            private void toggleInvalid() {
                this.isInvalid = !this.isInvalid;
                if (this.isInvalid == true) {
                    this.areaPolygon.setFillColor(Color.argb(83, 0, 83, 0));
                    this.areaPolygon.setStrokeColor(Color.GREEN);
                    this.expandedPolygon.setFillColor(Color.argb(83, 0, 83, 0));
                    this.expandedPolygon.setStrokeColor(Color.GREEN);
                    mapview.invalidate();
                } else {
                    this.areaPolygon.setFillColor(Color.argb(83, 83, 0, 0));
                    this.areaPolygon.setStrokeColor(Color.RED);
                    this.expandedPolygon.setFillColor(Color.argb(83, 0, 0, 83));
                    this.expandedPolygon.setStrokeColor(Color.BLUE);
                    mapview.invalidate();
                }
            }
        }

        private void toggleAreaInvalidUseSpecifiedPolygon(CustomPolygon polygon) {
            for (int areaDataNum = 0; areaDataNum <= areaDataArray.size() - 1; areaDataNum++) {
                if (areaDataArray.get(areaDataNum).areaPolygon.equals(polygon)) {
                    areaDataArray.get(areaDataNum).toggleInvalid();
                    return;
                }
                //近接領域の判定
                if (areaDataArray.get(areaDataNum).expandedPolygon.equals(polygon)) {
                    areaDataArray.get(areaDataNum).toggleInvalid();
                    return;
                }
            }
        }

        private void removeAll() {
            mapview.getOverlays().remove(draftPolygon);
            mapview.getOverlays().removeAll(markerArray);
            for (int areaDataNum = 0; areaDataNum <= areaDataArray.size() - 1; areaDataNum++) {
                mapview.getOverlays().remove(areaDataArray.get(areaDataNum).areaPolygon);
                //mapview.getOverlays().removeAll(areaDataArray.get(areaDataNum).expandedPolygonArray);
                mapview.getOverlays().remove(areaDataArray.get(areaDataNum).expandedPolygon);
            }
            mapview.invalidate();
            cleanMarker(markerArray);
            cleanDraftPolygon();
            areaDataArray.clear();
            geoPointArray.clear();
            markerArray.clear();

            parameter.outputParamToCsv();
        }

        private void removeAllWithoutOutput() {
            mapview.getOverlays().remove(draftPolygon);
            mapview.getOverlays().removeAll(markerArray);
            for (int areaDataNum = 0; areaDataNum <= areaDataArray.size() - 1; areaDataNum++) {
                mapview.getOverlays().remove(areaDataArray.get(areaDataNum).areaPolygon);
                //mapview.getOverlays().removeAll(areaDataArray.get(areaDataNum).expandedPolygonArray);
                mapview.getOverlays().remove(areaDataArray.get(areaDataNum).expandedPolygon);
            }
            mapview.invalidate();
            cleanMarker(markerArray);
            cleanDraftPolygon();
            areaDataArray.clear();
            geoPointArray.clear();
            markerArray.clear();

            parameter.areaQty = 0;
            parameter.areaData.clear();
            //parameter.outputParamToCsv();
            reloadLocationService();
        }

        private void addPoint(GeoPoint point) {

//            Marker marker = new Marker(mapview);
//            marker.setPosition(point);
//            marker.setSnippet("");
//            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//            drawMarker(marker);
//            setOnMarkerClickListner(marker);
//
//            Bitmap bitmap;
//            Bitmap resizeBitmap;
//            Drawable gray = getResources().getDrawable(R.drawable.marker_gray);
//            bitmap = ((BitmapDrawable)gray).getBitmap();
//            resizeBitmap = Bitmap.createScaledBitmap(bitmap,200,200,false);
//            gray = new BitmapDrawable(getResources(),resizeBitmap);
//            marker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_CENTER);
//            marker.setIcon(gray);
            Marker marker = new Marker(mapview);
            marker = setMarker(point, ColorMarker.MARKER_GRAY);
            setOnMarkerClickListner(marker);

            if (geoPointArray.contains(beforePoint)) {
                for (int pointNum = 0; pointNum <= geoPointArray.size() - 1; pointNum++) {
                    if (geoPointArray.get(pointNum).equals(beforePoint)) {

                        geoPointArray.set(pointNum, point);
                        markerArray.set(pointNum, marker);

                    }
                }
                beforePoint = null;
            } else {
                geoPointArray.add(point);
                markerArray.add(marker);
            }

            for (int i = 0; i <= 99; i++) {
                dangerArea.highlightedMarker.remove(dangerArea.mapview);
                dangerArea.mapview.invalidate();
            }
            beforePoint = null;
            mapview.invalidate();
            cleanMarker(beforeMarker);

        }

        private void drawMarker(Marker marker) {
            mapview.getOverlays().add(marker);
            mapview.invalidate();
        }

        private Marker setMarker(GeoPoint point, ColorMarker colorMarker) {
            Marker marker = new Marker(mapview);
            marker.setPosition(point);

            Bitmap bitmap;
            Bitmap resizeBitmap;

            double density = getApplicationContext().getResources().getDisplayMetrics().density;
            int size = (int) (density * 30);

            switch (colorMarker) {
                case MARKER_RED:
                    Drawable red = getResources().getDrawable(R.drawable.marker_red);
                    bitmap = ((BitmapDrawable) red).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
                    red = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(red);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    break;
                case MARKER_BLUE:
                    Drawable blue = getResources().getDrawable(R.drawable.marker_blue);
                    bitmap = ((BitmapDrawable) blue).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size * 2, size * 2, false);
                    blue = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(blue);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    break;
                case MARKER_YELLOW:
                    Drawable yellow = getResources().getDrawable(R.drawable.marker_yellow);
                    bitmap = ((BitmapDrawable) yellow).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
                    yellow = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(yellow);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    break;
                case MARKER_GREEN:
                    Drawable green = getResources().getDrawable(R.drawable.marker_green);
                    bitmap = ((BitmapDrawable) green).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
                    green = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(green);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    break;
                case MARKER_GRAY:
                    Drawable gray = getResources().getDrawable(R.drawable.marker_gray);
                    bitmap = ((BitmapDrawable) gray).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size * 2, size * 2, false);
                    gray = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(gray);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    //setOnMarkerClickListner(marker);
                    break;

            }
            marker.setVisible(true);
            mapview.getOverlays().add(marker);
            mapview.invalidate();
            return marker;
        }

        private void drawMarker(ArrayList<Marker> markers) {
            mapview.getOverlays().addAll(markers);
            mapview.invalidate();
        }

        private void cleanMarker(Marker marker) {
            mapview.getOverlays().remove(marker);
            mapview.invalidate();
        }

        private void cleanMarker(ArrayList<Marker> markers) {
            mapview.getOverlays().removeAll(markers);
            mapview.invalidate();
        }

        private Boolean isPolygonDrawable() {
            if (geoPointArray.size() >= 3) {
                return true;
            }
            return false;
        }

        private Boolean isPolygonSet() {
            if (draftPolygon.getPoints().size() >= 3) {
                return true;
            }
            return false;
        }

        private void setDraftPolygon(ArrayList<GeoPoint> points) {
            CustomPolygon polygon = new CustomPolygon();
            polygon.setPoints(points);
            polygon.setFillColor(Color.argb(83, 83, 83, 83));
            polygon.setStrokeColor(Color.GRAY);
            polygon.setStrokeWidth(1F);
            //polygon.setSnippet("");
            draftPolygon = polygon;
        }

        private void cleanDraftPolygon() {
            mapview.getOverlays().remove(draftPolygon);
            mapview.invalidate();
        }

        private void drawDraftPolygon() {
            mapview.getOverlays().add(draftPolygon);
            mapview.invalidate();

            mapview.getOverlays().removeAll(markerArray);
            mapview.getOverlays().addAll(markerArray);
            mapview.invalidate();
        }

        private void drawPolygon(final CustomPolygon polygon) {
            mapview.getOverlays().add(polygon);
            mapview.invalidate();
        }

        private void drawPolygon(ArrayList<CustomPolygon> polygons) {
            mapview.getOverlays().addAll(polygons);
            mapview.invalidate();
        }

        private void undoDraftPolygon() {
            int max = geoPointArray.size() - 1;
            if (max < 0) {
                return;
            }
            geoPointArray.remove(max);
            mapview.getOverlays().remove(markerArray.get(max));
            markerArray.remove(max);

            cleanDraftPolygon();
            setDraftPolygon(geoPointArray);
            mapview.getOverlays().add(draftPolygon);
            mapview.invalidate();

            mapview.getOverlays().removeAll(markerArray);
            mapview.getOverlays().addAll(markerArray);
            mapview.invalidate();
        }

        private ArrayList<CustomPolygon> getExpandedPolygonArray(CustomPolygon polygon, double size) {
            ArrayList<CustomPolygon> exPolygonArray = new ArrayList<>();
            ArrayList<GeoPoint> exPointArray = (ArrayList<GeoPoint>) polygon.getPoints();

            for (int i = 0; i <= (exPointArray.size() - 2); i++) {
                double R = size;
                double Ax = exPointArray.get(i).getLongitude();
                double Ay = exPointArray.get(i).getLatitude();
                double Bx = exPointArray.get(i + 1).getLongitude();
                double By = exPointArray.get(i + 1).getLatitude();

                double p = Bx - Ax;
                double q = By - Ay;
                double alpha1, alpha2;
                double beta1, beta2;

                alpha1 = (q * R * Math.sqrt((Math.pow(p, 2.0) + Math.pow(q, 2.0) - 4.0 * Math.pow(R, 2.0)))) / (Math.pow(p, 2.0) + Math.pow(q, 2.0));
                alpha2 = -alpha1;
                beta1 = (-(p * R * (Math.sqrt(Math.pow(p, 2.0) + Math.pow(q, 2.0))))) / (Math.pow(p, 2.0) + Math.pow(q, 2.0));
                beta2 = -beta1;

                GeoPoint Ca1 = new GeoPoint((Ay + beta1), (Ax + alpha1));
                GeoPoint Ca2 = new GeoPoint((Ay + beta2), (Ax + alpha2));
                GeoPoint Cb1 = new GeoPoint((By + beta1), (Bx + alpha1));
                GeoPoint Cb2 = new GeoPoint((By + beta2), (Bx + alpha2));

                final ShapeAsPointsBuilder shapeBuilder = new ShapeAsPointsBuilder()
                        .CWA(Ca1, exPointArray.get(i), Ca2)
                        .CWA(Cb2, exPointArray.get(i + 1), Cb1);

                exPolygonArray.add(new CustomPolygon());
                exPolygonArray.get(i).setPoints(shapeBuilder.toList());
                exPolygonArray.get(i).setFillColor(Color.argb(83, 0, 0, 83));
                exPolygonArray.get(i).setStrokeColor(Color.argb(0, 0, 0, 0));
                exPolygonArray.get(i).setStrokeWidth(1f);
            }
            double R = size;
            double Ax = exPointArray.get(exPointArray.size() - 1).getLongitude();
            double Ay = exPointArray.get(exPointArray.size() - 1).getLatitude();
            double Bx = exPointArray.get(0).getLongitude();
            double By = exPointArray.get(0).getLatitude();

            double p = Bx - Ax;
            double q = By - Ay;
            double alpha1, alpha2;
            double beta1, beta2;

            alpha1 = (q * R * Math.sqrt((Math.pow(p, 2.0) + Math.pow(q, 2.0) - 4.0 * Math.pow(R, 2.0)))) / (Math.pow(p, 2.0) + Math.pow(q, 2.0));
            alpha2 = -alpha1;
            beta1 = (-(p * R * (Math.sqrt(Math.pow(p, 2.0) + Math.pow(q, 2.0))))) / (Math.pow(p, 2.0) + Math.pow(q, 2.0));
            beta2 = -beta1;

            GeoPoint Ca1 = new GeoPoint((Ay + beta1), (Ax + alpha1));
            GeoPoint Ca2 = new GeoPoint((Ay + beta2), (Ax + alpha2));
            GeoPoint Cb1 = new GeoPoint((By + beta1), (Bx + alpha1));
            GeoPoint Cb2 = new GeoPoint((By + beta2), (Bx + alpha2));

            int n = exPointArray.size() - 1;
            final ShapeAsPointsBuilder shapeBuilder = new ShapeAsPointsBuilder()
                    .CWA(Ca1, exPointArray.get(n), Ca2)
                    .CWA(Cb2, exPointArray.get(0), Cb1);

            exPolygonArray.add(new CustomPolygon());
            exPolygonArray.get(n).setPoints(shapeBuilder.toList());
            exPolygonArray.get(n).setFillColor(Color.argb(83, 0, 0, 83));
            exPolygonArray.get(n).setStrokeColor(Color.argb(0, 0, 0, 0));
            exPolygonArray.get(n).setStrokeWidth(1f);

            return exPolygonArray;
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

        //与えられたポリゴンをmergin[m]だけ拡張したポリゴンを返す
        private CustomPolygon getFixedExpandedPolygon(CustomPolygon polygon, double mergin) {
            CustomPolygon exPolygon = new CustomPolygon();
            ArrayList<GeoPoint> points = (ArrayList<GeoPoint>) polygon.getPoints();     //拡張された領域の各頂点
            ArrayList<GeoPoint> defaultPoints = new ArrayList<GeoPoint>(points);        //拡張される前の各頂点

            ArrayList<GeoPoint> outline1 = getExpandedPointArray(points, mergin);    //右or左回転方向で拡張

            Collections.reverse(points);

            ArrayList<GeoPoint> outline2 = getExpandedPointArray(points, mergin);    //逆方向で拡張

            double s1 = findAreaSize(outline1); //面積計算
            double s2 = findAreaSize(outline2); //面積計算

            //面積が広い方の領域を採用
            if (s1 > s2) {
                exPolygon = getFixedPolygon(outline1, defaultPoints, Clockwise.RIGHT);  //自己交差を修復して角をまるめる
                //exPolygon.setPoints(outline1);
            } else {
                exPolygon = getFixedPolygon(outline2, defaultPoints, Clockwise.LEFT);   //自己交差を修復して角をまるめる
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

            //マップ上での緯度経度をベクトルで扱いたいクラス
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

        private CustomPolygon getFixedPolygon(ArrayList<GeoPoint> points, ArrayList<GeoPoint> defaultPoints, Clockwise clockwise) {
            //ArrayList<GeoPoint> defaultPoints = new ArrayList<GeoPoint>(points);
            ArrayList<ArrayList<GeoPoint>> fixedPoints = new ArrayList<>();
            ArrayList<GeoPoint> roundedPoints;

//            for(int i = 0; i<=defaultPoints.size()-1; i++){
//                Marker marker = new Marker(mapview);
//                marker.setPosition(defaultPoints.get(i));
//                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                marker.setSnippet(Integer.toString(i));
//                mapview.getOverlays().add(marker);
//            }

            CustomPolygon fixedPolygon = new CustomPolygon();
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
            if (clockwise == Clockwise.RIGHT) {
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

            } else if (clockwise == Clockwise.LEFT) {

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

        //中点座標を求める
        private GeoPoint getMiddlePoint(GeoPoint point1, GeoPoint point2) {
            double midLatitude = 0;
            double midLongitude = 0;
            midLatitude = (point1.getLatitude() + point2.getLatitude()) / 2.0;
            midLongitude = (point1.getLongitude() + point2.getLongitude()) / 2.0;
            return new GeoPoint(midLatitude, midLongitude);
        }

        //交点座標を求める
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

        private Boolean containIntersectionPoint(ArrayList<GeoPoint> points) {
            if (points.size() <= 2) {
                return false;
            }
            for (int pointNum = 0; pointNum <= points.size() - 2; pointNum++) {
                if (getIntersection(points.get(pointNum), points.get(pointNum + 1), points.get(pointNum + 1), points.get(pointNum + 2)) == null) {
                    return true;
                }
            }
            if (getIntersection(points.get(points.size() - 1), points.get(0), points.get(0), points.get(1)) == null) {
                return true;
            }
            return false;
        }

        private void determineAreaData() {
            if (geoPointArray.size() < 3) {
                return;
            }
            CustomPolygon polygon = new CustomPolygon();
            polygon.setPoints(draftPolygon.getPoints());
            polygon.setFillColor(Color.argb(83, 83, 0, 0));
            polygon.setStrokeColor(Color.RED);
            polygon.setStrokeWidth(1F);
            areaDataArray.add(new AreaData(polygon));
            cleanDraftPolygon();
            cleanMarker(markerArray);
            int max = areaDataArray.size() - 1;
            geoPointArray.clear();
            markerArray.clear();
//            mapview.getOverlays().add(areaDataArray.get(max).areaPolygon);
//            mapview.getOverlays().addAll(areaDataArray.get(max).expandedPolygonArray);
            mapview.invalidate();

            parameter.setAreaData(dangerArea.areaDataArray);
            parameter.outputParamToCsv();
            //reloadLocationService();

        }

        private Boolean isPointInPolygon(GeoPoint p, CustomPolygon poly) {
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

        private Boolean isInsideDangerArea(GeoPoint point) {
            int size = areaDataArray.size() - 1;
            for (int i = 0; i <= size; i++) {
                if (isPointInPolygon(point, areaDataArray.get(i).areaPolygon) == true) {
                    return true;
                }
            }
            return false;
        }

        private Boolean isInsideExpandedDangerArea(GeoPoint point) {
            int size = areaDataArray.size() - 1;
            for (int i = 0; i <= size; i++) {
                if (isPointInPolygon(point, areaDataArray.get(i).expandedPolygon) == true) {
                    return true;
                }
            }
            return false;
        }

        private Boolean isInsideSemiExpandedDangerArea(GeoPoint point) {
            int size = areaDataArray.size() - 1;
            for (int i = 0; i <= size; i++) {
                if (isPointInPolygon(point, areaDataArray.get(i).semiExpandedPolygon) == true) {
                    return true;
                }
            }
            return false;
        }

        private LocationStatus getLocationStatus(GeoPoint point) {
            if (isInsideDangerArea(point)) {
                return LocationStatus.INSIDE;
            } else if (isInsideExpandedDangerArea(point)) {
                return LocationStatus.CLOSE;
            } else if (isInsideSemiExpandedDangerArea(point)) {
                return LocationStatus.SEMI_CLOSE;
            }
            return LocationStatus.OUTSIDE;
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

        private void addAreaData(ArrayList<ArrayList<GeoPoint>> areaData) {
            int areaDataSize = areaData.size();
            for (int areaDataNum = 1; areaDataNum <= areaDataSize; areaDataNum++) {
                CustomPolygon polygon = new CustomPolygon();
                polygon.setPoints(areaData.get(areaDataNum - 1));
                polygon.setFillColor(Color.argb(83, 83, 0, 0));
                polygon.setStrokeColor(Color.RED);
                polygon.setStrokeWidth(1F);
                //drawPolygon(polygon);
                areaDataArray.add(new AreaData(polygon));
            }
            parameter.setAreaData(dangerArea.areaDataArray);

            parameter.outputParamToCsv();//パラメータ受信時に何度も出力→LocationServiceリロードされてしまう
        }

        private void setPolygonToDraftPolygon(ArrayList<GeoPoint> points) {

            int index = 0;
            for (int areaNum = 0; areaNum <= areaDataArray.size() - 1; areaNum++) {
                if (areaDataArray.get(areaNum).areaPolygon.getPoints().equals(points) || areaDataArray.get(areaNum).expandedPolygon.getPoints().equals(points)) {     //指定エリアのポリゴンの頂点座標（NOT拡張エリア）を取得
                    //points = new ArrayList<>(areaDataArray.get(areaNum).areaPolygon.getPoints());
                    index = areaNum;
                    break;
                }
            }

            points = (ArrayList<GeoPoint>) areaDataArray.get(index).areaPolygon.getPoints();
            removeAreaDataUseSpecifiedPolygon(areaDataArray.get(index).areaPolygon);

            cleanMarker(markerArray);
            cleanDraftPolygon();
            geoPointArray.clear();

            for (int pointNum = 0; pointNum <= points.size() - 1; pointNum++) {
                addPoint(points.get(pointNum));
            }
            dangerArea.setDraftPolygon(dangerArea.geoPointArray);
            dangerArea.drawDraftPolygon();

        }

        //引数に渡したPolygonを持つAreaDataをremoveします。
        private void removeAreaDataUseSpecifiedPolygon(CustomPolygon polygon) {
            for (int areaDataNum = 0; areaDataNum <= areaDataArray.size() - 1; areaDataNum++) {
                if (areaDataArray.get(areaDataNum).areaPolygon.equals(polygon)) {
                    //領域の判定
                    mapview.getOverlays().remove(areaDataArray.get(areaDataNum).areaPolygon);
                    mapview.getOverlays().remove(areaDataArray.get(areaDataNum).expandedPolygon);
                    mapview.getOverlays().remove(areaDataArray.get(areaDataNum).semiExpandedPolygon);
                    mapview.invalidate();
                    areaDataArray.remove(areaDataNum);
                    parameter.setAreaData(areaDataArray);
                    parameter.outputParamToCsv();
                    //reloadLocationService();
                    return;
                }
                //近接領域の判定
                if (areaDataArray.get(areaDataNum).expandedPolygon.equals(polygon)) {
                    mapview.getOverlays().remove(areaDataArray.get(areaDataNum).areaPolygon);
                    mapview.getOverlays().remove(areaDataArray.get(areaDataNum).expandedPolygon);
                    mapview.getOverlays().remove(areaDataArray.get(areaDataNum).semiExpandedPolygon);
                    mapview.invalidate();
                    areaDataArray.remove(areaDataNum);
                    parameter.setAreaData(areaDataArray);
                    parameter.outputParamToCsv();
                    //reloadLocationService();
                    return;
                }
            }
            for (int i = 0; i <= 99; i++) {
                dangerArea.highlightedMarker.remove(dangerArea.mapview);
                dangerArea.mapview.invalidate();
            }
            beforePoint = null;
            mapview.invalidate();

            parameter.setAreaData(dangerArea.areaDataArray);
            parameter.outputParamToCsv();

            //reloadLocationService();
        }
    }

    static enum LocationStatus {
        OUTSIDE,
        SEMI_CLOSE,
        CLOSE,
        INSIDE
    }

    public class LocationDate {
        private int year;
        private int month;
        private int day;
        private int hour;
        private int minute;
        private int second;

        LocationDate(Calendar calendar) {
            this.year = calendar.get(Calendar.YEAR);
            this.month = calendar.get(Calendar.MONTH);
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
            string += year + "/";
            string += month + "/";
            string += day + " ";
            if (hour < 10) {
                string += "0" + hour + ":";
            } else {
                string += hour + ":";
            }
            if (minute < 10) {
                string += "0" + minute + ":";
            } else {
                string += minute + ":";
            }
            if (second < 10) {
                string += "0" + second;
            } else {
                string += second;
            }
            return string;
        }

        private String getTimeString() {
            String string = "";
            if (hour < 10) {
                string += "0" + hour + ":";
            } else {
                string += hour + ":";
            }
            if (minute < 10) {
                string += "0" + minute + ":";
            } else {
                string += minute + ":";
            }
            if (second < 10) {
                string += "0" + second;
            } else {
                string += second;
            }
            return string;
        }

        private int getTimeInSec() {
            int sec = second + minute * 60 + hour * 60 * 60;
            return sec;
        }
    }

    public class LogData {
        private LocationDate locationDate;
        private double latitude;
        private double longitude;
        private LocationStatus locationStatus;

        LogData(Calendar calendar, double lat, double lon, LocationStatus locationStatus) {
            locationDate = new LocationDate(calendar);
            this.latitude = lat;
            this.longitude = lon;
            this.locationStatus = locationStatus;
        }

    }

    public class LocationLogger {
        private ArrayList<LogData> logList;
        private ArrayList<Marker> locationMarkerList;
        private Polyline logLine = new Polyline(mMapView);
        private ArrayList<Marker> logMarker = new ArrayList<>();
        private List<Polygon> polygons = new ArrayList<>();
        private List<Polygon> exPolygons = new ArrayList<>();

        private List<LogData> logDataList = new ArrayList<>();
        private List<LogData> drawnLogDataList = new ArrayList<>();

        LocationLogger() {
            logList = new ArrayList<>();
            locationMarkerList = new ArrayList<>();
        }

        private void removeLogLine() {
            if (logLine != null && !logMarker.isEmpty()) {
                mMapView.getOverlayManager().remove(logLine);
                mMapView.getOverlayManager().removeAll(logMarker);
                mMapView.invalidate();
            }
        }

        private void drawLogLine(List<LogData> logList) {
            List<GeoPoint> geoPointList = new ArrayList<>();
            for (int logDataNum = 0; logDataNum <= logList.size() - 1; logDataNum++) {
                GeoPoint geoPoint = new GeoPoint(logList.get(logDataNum).latitude, logList.get(logDataNum).longitude);
                geoPointList.add(geoPoint);
                switch (logList.get(logDataNum).locationStatus) {
                    case OUTSIDE:
                        break;
                    case SEMI_CLOSE:
                        break;
                    case CLOSE:
                        break;
                    case INSIDE:
                        break;
                }
            }
            logLine.setPoints(geoPointList);
            logLine.setColor(Color.MAGENTA);
            logLine.setWidth(6F);
            mMapView.getOverlayManager().add(logLine);

            for (int i = 0; i <= geoPointList.size() - 1; i++) {
                logMarker.add(setLogMarker(geoPointList.get(i), LogMarker.MARKER_POINT, logList.get(i).locationDate.getString()));
            }
            logMarker.add(setLogMarker(geoPointList.get(0), LogMarker.MARKER_START, logList.get(0).locationDate.getString()));
            logMarker.add(setLogMarker(geoPointList.get(geoPointList.size() - 1), LogMarker.MARKER_END, logList.get(logList.size() - 1).locationDate.getString()));

            mMapView.invalidate();
            IMapController mapController = mMapView.getController();
            mapController.animateTo(new GeoPoint(logList.get(0).latitude, logList.get(0).longitude));
        }

        private LogData drawLogLineWithStatusMarker(List<LogData> logList) {
            List<GeoPoint> points = new ArrayList<>();
            for (int dataNum = 0; dataNum <= logList.size() - 1; dataNum++) {
                points.add(new GeoPoint(logList.get(dataNum).latitude, logList.get(dataNum).longitude));
            }
            logLine.setPoints(points);
            logLine.setColor(getResources().getColor(R.color.Orange));
            logLine.setWidth(6F);
            mMapView.getOverlayManager().add(logLine);

            for (int dataNum = 0; dataNum <= logList.size() - 1; dataNum++) {
                GeoPoint point = new GeoPoint(logList.get(dataNum).latitude, logList.get(dataNum).longitude);
                switch (logList.get(dataNum).locationStatus) {
                    case OUTSIDE:
                        logMarker.add(setLogMarker(point, LogMarker.MARKER_POINT_OUTSIDE, logList.get(dataNum).locationDate.getString()));
                        if (dataNum == logList.size() - 1) {
                            logMarker.add(setLogMarker(point, LogMarker.MARKER_OUTSIDE, logList.get(dataNum).locationDate.getString()));
                        }
                        break;
                    case SEMI_CLOSE:
                        logMarker.add(setLogMarker(point, LogMarker.MARKER_POINT_OUTSIDE, logList.get(dataNum).locationDate.getString()));
                        if (dataNum == logList.size() - 1) {
                            logMarker.add(setLogMarker(point, LogMarker.MARKER_OUTSIDE, logList.get(dataNum).locationDate.getString()));
                        }
                        break;
                    case CLOSE:
                        logMarker.add(setLogMarker(point, LogMarker.MARKER_POINT_CLOSE, logList.get(dataNum).locationDate.getString()));
                        if (dataNum == logList.size() - 1) {
                            logMarker.add(setLogMarker(point, LogMarker.MARKER_CLOSE, logList.get(dataNum).locationDate.getString()));
                        }
                        break;
                    case INSIDE:
                        logMarker.add(setLogMarker(point, LogMarker.MARKER_POINT_INSIDE, logList.get(dataNum).locationDate.getString()));
                        if (dataNum == logList.size() - 1) {
                            logMarker.add(setLogMarker(point, LogMarker.MARKER_INSIDE, logList.get(dataNum).locationDate.getString()));
                        }
                        break;
                }
            }
            mMapView.invalidate();
            IMapController mapController = mMapView.getController();
            mapController.animateTo(new GeoPoint(logList.get(logList.size() - 1).latitude, logList.get(logList.size() - 1).longitude));
            return logList.get(logList.size() - 1);
        }

        private LogData drawLogLineWithPersonMarker(List<LogData> logList) {
            List<GeoPoint> geoPointList = new ArrayList<>();
            for (int logDataNum = 0; logDataNum <= logList.size() - 1; logDataNum++) {
                GeoPoint geoPoint = new GeoPoint(logList.get(logDataNum).latitude, logList.get(logDataNum).longitude);
                geoPointList.add(geoPoint);
                switch (logList.get(logDataNum).locationStatus) {
                    case OUTSIDE:
                        break;
                    case SEMI_CLOSE:
                        break;
                    case CLOSE:
                        break;
                    case INSIDE:
                        break;
                }
            }
            logLine.setPoints(geoPointList);
            logLine.setColor(Color.MAGENTA);
            logLine.setWidth(6F);
            mMapView.getOverlayManager().add(logLine);

            for (int i = 0; i <= geoPointList.size() - 1; i++) {
                logMarker.add(setLogMarker(geoPointList.get(i), LogMarker.MARKER_POINT, logList.get(i).locationDate.getString()));
            }

            switch (logList.get(logList.size() - 1).locationStatus) {
                case OUTSIDE:
                    logMarker.add(setLogMarker(geoPointList.get(geoPointList.size() - 1), LogMarker.MARKER_INSIDE, logList.get(logList.size() - 1).locationDate.getString()));
                    break;
                case SEMI_CLOSE:
                    logMarker.add(setLogMarker(geoPointList.get(geoPointList.size() - 1), LogMarker.MARKER_OUTSIDE, logList.get(logList.size() - 1).locationDate.getString()));
                    break;
                case CLOSE:
                    logMarker.add(setLogMarker(geoPointList.get(geoPointList.size() - 1), LogMarker.MARKER_CLOSE, logList.get(logList.size() - 1).locationDate.getString()));
                    break;
                case INSIDE:
                    logMarker.add(setLogMarker(geoPointList.get(geoPointList.size() - 1), LogMarker.MARKER_INSIDE, logList.get(logList.size() - 1).locationDate.getString()));
                    break;
            }
            //logMarker.add(setLogMarker(geoPointList.get(geoPointList.size() - 1), LogMarker.MARKER_END, logList.get(logList.size() - 1).locationDate.getString()));

            mMapView.invalidate();
            IMapController mapController = mMapView.getController();
            //mapController.setZoom(22.0);
            mapController.animateTo(new GeoPoint(logList.get(logList.size() - 1).latitude, logList.get(logList.size() - 1).longitude));
            //mapController.animateTo(new GeoPoint(logList.get(logList.size() - 1).latitude, logList.get(logList.size() - 1).longitude), mMapView.getZoomLevelDouble(), (long) 1);
            return logList.get(logList.size() - 1);
        }


        private void setLogline(ArrayList<LogData> logList) {
            logDataList = logList;
            drawnLogDataList.clear();
            drawnLogDataList.add(logList.get(0));
            removeLogLine();
            //String currentTime = drawLogLineWithPersonMarker(drawnLogDataList).locationDate.getTimeString();
            String currentTime = drawLogLineWithStatusMarker(drawnLogDataList).locationDate.getTimeString();

            logPlayCurrentTimeText.setText(currentTime);
        }

        private LogData drawSeekedPoint(int logListNum){
            if (logListNum <= logDataList.size() - 1) {
                List<LogData> tempList = new ArrayList<>();
                for (int i = 0; i <= logListNum; i++) {
                    tempList.add(logDataList.get(i));
                }
                drawnLogDataList = new ArrayList<>(tempList);
                removeLogLine();
                //String currentTime = drawLogLineWithPersonMarker(drawnLogDataList).locationDate.getTimeString();
                String currentTime  = drawLogLineWithStatusMarker(drawnLogDataList).locationDate.getTimeString();
                logPlayCurrentTimeText.setText(currentTime);
                return logDataList.get(logListNum);
            } else {
                return null;
            }
        }

        private LogData getNextPoint() {
            if (!logDataList.isEmpty() && (drawnLogDataList.size() <= logDataList.size())) {
                LogData nextData = logDataList.get(drawnLogDataList.size() - 1);
                return nextData;
            } else {
                return null;
            }
        }

        private LogData getPreviousPoint() {
            if (!drawnLogDataList.isEmpty() && (drawnLogDataList.size() >= 2)) {
                LogData previousData = drawnLogDataList.get(drawnLogDataList.size() - 2);
                return previousData;
            } else {
                return null;
            }
        }

        private LogData drawNextPoint() {
            if (!logDataList.isEmpty() && (drawnLogDataList.size() <= logDataList.size())) {
                LogData drawnData = logDataList.get(drawnLogDataList.size()-1);
                drawnLogDataList.add(drawnData);
                removeLogLine();
                //String currentTime = drawLogLineWithPersonMarker(drawnLogDataList).locationDate.getTimeString();
                String currentTime  = drawLogLineWithStatusMarker(drawnLogDataList).locationDate.getTimeString();
                logPlayCurrentTimeText.setText(currentTime);
                logPLaySeekBar.incrementProgressBy(1);
                return drawnData;
            } else {
                return null;
            }
        }

        private LogData drawPrevioutPoint() {
            if (!drawnLogDataList.isEmpty() && (drawnLogDataList.size() >= 2)) {
                drawnLogDataList.remove(drawnLogDataList.size() - 1);
                LogData previousData = drawnLogDataList.get(drawnLogDataList.size() - 1);
                removeLogLine();
                //String currentTime = drawLogLineWithPersonMarker(drawnLogDataList).locationDate.getTimeString();
                String currentTime  = drawLogLineWithStatusMarker(drawnLogDataList).locationDate.getTimeString();
                logPlayCurrentTimeText.setText(currentTime);
                logPLaySeekBar.incrementProgressBy(-1);
                return previousData;
            } else {
                return null;
            }
        }

        private void removeLogArea() {
            if (!polygons.isEmpty() && !exPolygons.isEmpty()) {
                mMapView.getOverlayManager().removeAll(polygons);
                mMapView.getOverlayManager().removeAll(exPolygons);
                mMapView.invalidate();
            }
        }

        private void drawLogArea(ArrayList<ArrayList<GeoPoint>> areas) {
            polygons = new ArrayList<>();
            exPolygons = new ArrayList<>();
            for (int areaNum = 0; areaNum <= areas.size() - 1; areaNum++) {
                Polygon polygon = new Polygon();
                Polygon exPolygon = new Polygon();
                DangerArea da = new DangerArea(mMapView);

                polygon.setPoints(areas.get(areaNum));
                polygon.setFillColor(Color.argb(83, 83, 41, 83));
                polygon.setStrokeColor(Color.GRAY);
                polygon.setStrokeWidth(1F);
                polygons.add(polygon);

                CustomPolygon customPolygon = new CustomPolygon();
                customPolygon.setPoints(polygon.getPoints());

                exPolygon.setPoints(da.getFixedExpandedPolygon(customPolygon, parameter.getCloseDistance()).getPoints()); //領域拡張後のポイントだけ持ってくる（......）
                exPolygon.setFillColor(Color.argb(83, 255, 255, 0));
                exPolygon.setStrokeColor(Color.GRAY);
                exPolygon.setStrokeWidth(1F);
                exPolygons.add(exPolygon);
            }

            mMapView.getOverlayManager().addAll(polygons);
            mMapView.getOverlayManager().addAll(exPolygons);

            mMapView.invalidate();
        }

        private Marker setLogMarker(GeoPoint point, LogMarker logMarker) {
            Marker marker = new Marker(mMapView);
            marker.setPosition(point);
            Bitmap bitmap;
            Bitmap resizeBitmap;
            double density = getApplicationContext().getResources().getDisplayMetrics().density;
            int size = (int) (density * 30);

            switch (logMarker) {
                case MARKER_START:
                    Drawable start = getResources().getDrawable(R.drawable.marker_start);
                    bitmap = ((BitmapDrawable) start).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
                    start = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(start);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    break;
                case MARKER_END:
                    Drawable end = getResources().getDrawable(R.drawable.marker_end);
                    bitmap = ((BitmapDrawable) end).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
                    end = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(end);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    break;
                case MARKER_POINT:
                    Drawable yellow = getResources().getDrawable(R.drawable.marker_point);
                    bitmap = ((BitmapDrawable) yellow).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size / 2, size / 2, false);
                    yellow = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(yellow);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    break;
                case MARKER_WALK:
                    Drawable walk = getResources().getDrawable(R.drawable.walk);
                    bitmap = ((BitmapDrawable) walk).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size / 2, size / 2, false);
                    walk = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(walk);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    break;
            }
            marker.setVisible(true);
            mMapView.getOverlays().add(marker);
            mMapView.invalidate();
            return marker;
        }

        private Marker setLogMarker(GeoPoint point, LogMarker logMarker, String snippet) {
            Marker marker = new Marker(mMapView);
            marker.setPosition(point);
            Bitmap bitmap;
            Bitmap resizeBitmap;
            double density = getApplicationContext().getResources().getDisplayMetrics().density;
            int size = (int) (density * 30);

            switch (logMarker) {
                case MARKER_START:
                    Drawable start = getResources().getDrawable(R.drawable.marker_start);
                    bitmap = ((BitmapDrawable) start).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
                    start = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(start);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    break;
                case MARKER_END:
                    Drawable end = getResources().getDrawable(R.drawable.marker_end);
                    bitmap = ((BitmapDrawable) end).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
                    end = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(end);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    break;
                case MARKER_POINT:
                    Drawable yellow = getResources().getDrawable(R.drawable.marker_point);
                    bitmap = ((BitmapDrawable) yellow).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size / 4, size / 4, false);
                    yellow = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(yellow);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    break;
                case MARKER_POINT_OUTSIDE:
                    Drawable outpoint = getResources().getDrawable(R.drawable.marker_point_outside);
                    bitmap = ((BitmapDrawable) outpoint).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size / 4, size / 4, false);
                    outpoint = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(outpoint);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    break;
                case MARKER_POINT_CLOSE:
                    Drawable closepoint = getResources().getDrawable(R.drawable.marker_point_close);
                    bitmap = ((BitmapDrawable) closepoint).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size / 4, size / 4, false);
                    closepoint = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(closepoint);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    break;
                case MARKER_POINT_INSIDE:
                    Drawable inpoint = getResources().getDrawable(R.drawable.marker_point_inside);
                    bitmap = ((BitmapDrawable) inpoint).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size / 4, size / 4, false);
                    inpoint = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(inpoint);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    break;
                case MARKER_OUTSIDE:
                    //Toast.makeText(MainActivity.this, "log_outside", Toast.LENGTH_SHORT).show();
                    Drawable outside = getResources().getDrawable(R.drawable.marker_green);
                    bitmap = ((BitmapDrawable) outside).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
                    outside = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(outside);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    break;
                case MARKER_CLOSE:
                    //Toast.makeText(MainActivity.this, "log_close", Toast.LENGTH_SHORT).show();
                    Drawable close = getResources().getDrawable(R.drawable.marker_yellow);
                    bitmap = ((BitmapDrawable) close).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
                    close = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(close);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    break;
                case MARKER_INSIDE:
                    //Toast.makeText(MainActivity.this, "log_inside", Toast.LENGTH_SHORT).show();
                    Drawable inside = getResources().getDrawable(R.drawable.marker_red);
                    bitmap = ((BitmapDrawable) inside).getBitmap();
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
                    inside = new BitmapDrawable(getResources(), resizeBitmap);
                    marker.setIcon(inside);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    break;
            }
            marker.setVisible(true);
            marker.setSnippet(snippet);
            mMapView.getOverlays().add(marker);
            mMapView.invalidate();
            return marker;
        }
    }

    static public Double getRoundedDouble(Double val, int decimalDegit) {
        BigDecimal bigDecimal = new BigDecimal(Double.toString(val));
        bigDecimal = bigDecimal.setScale(decimalDegit, BigDecimal.ROUND_HALF_UP);
        val = Double.parseDouble(bigDecimal.toString());
        return val;
    }

    public ArrayList<String> getAreaDataListFromExternalStorage() {
        ArrayList<String> areaDataList = new ArrayList<>();

        String path = PATH_MAIN_DIRECTORY;
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);
        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].getName());
            if (files[i].getName().contains("AreaData")) {
                areaDataList.add(files[i].getName());
            }
        }
        return areaDataList;
    }

    public class Parameter implements Parcelable {

        private String userID = "user";  //ユーザーID
        private String groupID = "group";    //グループID

        private boolean admin = false;

        public int areaQty = 0;    //禁止領域数
        public ArrayList<ArrayList<GeoPoint>> areaData;    //禁止領域データ
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


        //private int paramSize;

        public Parameter() {
            this.areaData = new ArrayList<>();
            this.jukiList = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                readParamFromCsv();
            }
        }

        public void readParamFromCsv() {
            if (isExistParamCsvData() == false) {
                outputParamToCsv();
                return;
            }
            ArrayList<String> titleList = new ArrayList<>();
            ArrayList<String> dataList = new ArrayList<>();
            int nowListNum = 0;
            try {
                FileInputStream fileInputStream = new FileInputStream(PATH_PARAMETER);
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
            } catch (Exception e) {
                e.printStackTrace();
            }

            userID = dataList.get(0);
            nowListNum++;
            groupID = dataList.get(1);
            nowListNum++;
            admin = Boolean.parseBoolean(dataList.get(2));
            nowListNum++;
            areaQty = Integer.parseInt(dataList.get(3));
            nowListNum++;

            ArrayList<Integer> coordQtyList = new ArrayList<>();
            for (int areaNum = 1; areaNum <= areaQty; areaNum++) {
                String qty = dataList.get(nowListNum);
                nowListNum++;
                coordQtyList.add(Integer.parseInt(qty));
            }

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
                pointlist.clear();
            }

            enterAlert = Boolean.parseBoolean(dataList.get(nowListNum));
            nowListNum++;
            closeAlert = Boolean.parseBoolean(dataList.get(nowListNum));
            nowListNum++;
            jukiAlert = Boolean.parseBoolean(dataList.get(nowListNum));
            nowListNum++;
            vibrationOn = Boolean.parseBoolean(dataList.get(nowListNum));
            nowListNum++;
            closeVolume = Integer.parseInt(dataList.get(nowListNum));
            nowListNum++;
            enterVolume = Integer.parseInt(dataList.get(nowListNum));
            nowListNum++;
            jukiVolume = Integer.parseInt(dataList.get(nowListNum));
            nowListNum++;
            loggingOn = Boolean.parseBoolean(dataList.get(nowListNum));
            nowListNum++;

            String time = dataList.get(nowListNum);
            StringTokenizer stringTokenizer = new StringTokenizer(time, ":");
            startHour = Integer.parseInt(stringTokenizer.nextToken());
            startMinute = Integer.parseInt(stringTokenizer.nextToken());
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
            endHour = Integer.parseInt(stringTokenizer.nextToken());
            endMinute = Integer.parseInt(stringTokenizer.nextToken());
            nowListNum++;

            closeDistance = Integer.parseInt(dataList.get(nowListNum));
            nowListNum++;
            jukiDistance = Integer.parseInt(dataList.get(nowListNum));
            nowListNum++;

            enterLogIntvl = Integer.parseInt(dataList.get(nowListNum));
            nowListNum++;
            closeLogIntvl = Integer.parseInt(dataList.get(nowListNum));
            nowListNum++;
            semiCloseLogIntvl = Integer.parseInt(dataList.get(nowListNum));
            nowListNum++;
            jukiCloseLogIntvl = Integer.parseInt(dataList.get(nowListNum));
            nowListNum++;
            normalLogIntvl = Integer.parseInt(dataList.get(nowListNum));
            nowListNum++;
            jukiQty = Integer.parseInt(dataList.get(nowListNum));
            nowListNum++;
            for (int jukiNum = 1; jukiNum <= jukiQty; jukiNum++) {
                jukiList.add(dataList.get(nowListNum));
                nowListNum++;
            }
            //paramSize = nowListNum;

        }

        public void outputParamToCsv() {
            File folder = new File(PATH_MAIN_DIRECTORY);
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (success) {
                // Do something on success
            } else {
                // Do something else on failure
            }
            ArrayList<String> outputDataStringArray = new ArrayList<>();
            outputDataStringArray.add("UserID," + userID);
            outputDataStringArray.add("GroupNo.," + groupID);
            outputDataStringArray.add("Administrator," + Boolean.toString(admin));
            outputDataStringArray.add("AreaQty," + Integer.toString(areaQty));
            for (int areaNum = 0; areaNum <= areaData.size() - 1; areaNum++) {
                outputDataStringArray.add("Area" + Integer.toString(areaNum) + "PointQty," + Integer.toString(areaData.get(areaNum).size()));
            }
            for (int areaNum = 0; areaNum <= areaData.size() - 1; areaNum++) {
                for (int coordNum = 0; coordNum <= areaData.get(areaNum).size() - 1; coordNum++) {
                    String latitude = Double.toString(getRoundedDouble(areaData.get(areaNum).get(coordNum).getLatitude(), 7));
                    String longitude = Double.toString(getRoundedDouble(areaData.get(areaNum).get(coordNum).getLongitude(), 7));
                    outputDataStringArray.add("Area" + Integer.toString(areaNum) + "-" + Integer.toString(coordNum) + "," + latitude + "/" + longitude);
                }
            }
            outputDataStringArray.add("EnterAlert," + Boolean.toString(enterAlert));
            outputDataStringArray.add("CloseAlert," + Boolean.toString(closeAlert));
            outputDataStringArray.add("JukiAlert," + Boolean.toString(jukiAlert));
            outputDataStringArray.add("VibrationOn," + Boolean.toString(vibrationOn));
            outputDataStringArray.add("CloseVolume," + Integer.toString(closeVolume));
            outputDataStringArray.add("EnterVolume," + Integer.toString(enterVolume));
            outputDataStringArray.add("JukiVolume," + Integer.toString(jukiVolume));
            outputDataStringArray.add("LoggingOn," + Boolean.toString(loggingOn));
            String startTime = Integer.toString(startHour) + ":" + Integer.toString(startMinute) + ":" + "00";
            outputDataStringArray.add("StartTime," + startTime);
            String startLunchTime = Integer.toString(startLunchHour) + ":" + Integer.toString(startLunchMinute) + ":" + "00";
            outputDataStringArray.add("StartLunchTime," + startLunchTime);
            String endLunchTime = Integer.toString(endLunchHour) + ":" + Integer.toString(endLunchMinute) + ":" + "00";
            outputDataStringArray.add("EndLunchTime," + endLunchTime);
            String endTime = Integer.toString(endHour) + ":" + Integer.toString(endMinute) + ":" + "00";
            outputDataStringArray.add("EndTime," + endTime);
            outputDataStringArray.add("CloseDistance," + Integer.toString(closeDistance));
            outputDataStringArray.add("JukiDistance," + Integer.toString(jukiDistance));

            outputDataStringArray.add("EnterLogIntvl," + Integer.toString(enterLogIntvl));
            outputDataStringArray.add("CloseLogIntvl," + Integer.toString(closeLogIntvl));
            outputDataStringArray.add("SemiCloseLogIntvl," + Integer.toString(semiCloseLogIntvl));
            outputDataStringArray.add("JukiCloseLogIntvl," + Integer.toString(jukiCloseLogIntvl));
            outputDataStringArray.add("NormalLogIntvl," + Integer.toString(normalLogIntvl));
            outputDataStringArray.add("JukiQty," + Integer.toString(jukiQty));
            for (int jukiNum = 0; jukiNum <= jukiQty - 1; jukiNum++) {
                outputDataStringArray.add("Juki" + Integer.toString(jukiNum) + "," + jukiList.get(jukiNum));
            }
            try {
                FileWriter fw = new FileWriter(PATH_PARAMETER, false);
                PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

                for (int i = 0; i <= outputDataStringArray.size() - 1; i++) {
                    pw.print(outputDataStringArray.get(i));
                    pw.println();
                }
                pw.close();
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            reloadLocationService();
        }

        public boolean isExistParamCsvData() {
            File file = new File(PATH_PARAMETER);
            Toast.makeText(MainActivity.this, "parameter:" + Boolean.toString(file.exists()), Toast.LENGTH_SHORT).show();
            return file.exists();
        }

        //getter
        public String getUserID() {
            return userID;
        }

        public String getGroupID() {
            return groupID;
        }

        public boolean isAdmin() {
            return this.admin;
        }

        public int getAreaQty() {
            return areaQty;
        }

        public ArrayList<ArrayList<GeoPoint>> getAreaData() {
            return areaData;
        }

        public boolean isEnterAlertOn() {
            return enterAlert;
        }

        public boolean isCloseAlertOn() {
            return closeAlert;
        }

        public boolean isJukiAlertOn() {
            return jukiAlert;
        }

        public boolean isVibrationOn() {
            return vibrationOn;
        }

        public int getCloseVolume() {
            return closeVolume;
        }

        public int getEnterVolume() {
            return enterVolume;
        }

        public int getJukiVolume() {
            return jukiVolume;
        }

        public boolean isLoggingOn() {
            return loggingOn;
        }

        public int getStartHour() {
            return startHour;
        }

        public int getStartMinute() {
            return startMinute;
        }

        public int getStartLunchHour() {
            return startLunchHour;
        }

        public int getStartLunchMinute() {
            return startLunchMinute;
        }

        public int getEndLunchHour() {
            return endLunchHour;
        }

        public int getEndLunchMinute() {
            return endLunchMinute;
        }

        public int getEndHour() {
            return endHour;
        }

        public int getEndMinute() {
            return endMinute;
        }

        public int getCloseDistance() {
            return closeDistance;
        }

        public int getJukiDistance() {
            return jukiDistance;
        }

        public int getNormalLogIntvl() {
            return normalLogIntvl;
        }

        public int getSemiCloseLogIntvl() {
            return semiCloseLogIntvl;
        }

        public int getCloseLogIntvl() {
            return closeLogIntvl;
        }

        public int getEnterLogIntvl() {
            return enterLogIntvl;
        }

        public int getJukiCloseLogIntvl() {
            return jukiCloseLogIntvl;
        }

        public int getJukiQty() {
            return jukiQty;
        }

        public ArrayList<String> getJukiList() {
            return jukiList;
        }

        //setter
        public void setUserID(String id) {
            //setUserIdText(id);
            this.userID = id;
            //Toast.makeText(MainActivity.this, id, Toast.LENGTH_SHORT).show();
        }

        public void setGroupID(String id) {
            //setGroupIdText(id);
            this.groupID = id;
            //Toast.makeText(MainActivity.this, id, Toast.LENGTH_SHORT).show();
        }

        public void setAdmin(boolean bool) {
            this.admin = bool;
        }

        public void setAreaData(ArrayList<DangerArea.AreaData> areaData) {
            ArrayList<ArrayList<GeoPoint>> areaList = new ArrayList<>();
            for (int areaNum = 0; areaNum <= areaData.size() - 1; areaNum++) {
                areaList.add((ArrayList) areaData.get(areaNum).areaPolygon.getPoints());
            }
            this.areaData = areaList;
            this.areaQty = areaData.size();
        }

        public void addAreaDataParam(ArrayList<GeoPoint> points) {

            ArrayList<DangerArea.AreaData> temp = new ArrayList<>();
            temp.addAll(dangerArea.areaDataArray);

            this.areaData.add(points);
            this.areaQty = areaData.size();

            dangerArea.removeAll();
            dangerArea.areaDataArray.clear();
            dangerArea.addAreaData(this.areaData);
        }

        public void setEnterAlert(boolean bool) {
            this.enterAlert = bool;
        }

        public void setCloseAlert(boolean bool) {
            this.closeAlert = bool;
        }

        public void setJukiAlert(boolean bool) {
            this.jukiAlert = bool;
        }

        public void setVibration(boolean bool) {
            this.vibrationOn = true;
        }

        public void setCloseVolume(int volume) {
            if (volume > 10 || volume < 0) {
                return;
            }
            this.closeVolume = volume;
        }

        public void setEnterVolume(int volume) {
            if (volume > 10 || volume < 0) {
                return;
            }
            this.enterVolume = volume;
        }

        public void setJukiVolume(int volume) {
            if (volume > 10 || volume < 0) {
                return;
            }
            this.jukiVolume = volume;
        }

        public void setLoggingOn(boolean bool) {
            this.loggingOn = bool;
        }

        public void setStartHour(int hour) {
            if (hour > 23 || hour < 0) {
                return;
            }
            this.startHour = hour;
        }

        public void setStartMinute(int minute) {
            if (minute > 59 || minute < 0) {
                return;
            }
            this.startMinute = minute;
        }

        public void setStartLunchHour(int hour) {
            if (hour > 23 || hour < 0) {
                return;
            }
            this.startLunchHour = hour;
        }

        public void setStartLunchMinute(int minute) {
            if (minute > 59 || minute < 0) {
                return;
            }
            this.startLunchMinute = minute;
        }

        public void setEndLunchHour(int hour) {
            if (hour > 23 || hour < 0) {
                return;
            }
            this.endLunchHour = hour;
        }

        public void setEndLunchMinute(int minute) {
            if (minute > 59 || minute < 0) {
                return;
            }
            this.endLunchMinute = minute;
        }

        public void setEndHour(int hour) {
            if (hour > 23 || hour < 0) {
                return;
            }
            this.endHour = hour;
        }

        public void setEndMinute(int minute) {
            if (minute > 59 || minute < 0) {
                return;
            }
            this.endMinute = minute;
        }

        public void setCloseDistance(int distance) {
            if (distance > 0) {
                this.closeDistance = distance;
            }
        }

        public void setJukiDistance(int distance) {
            if (distance > 0) {
                this.jukiDistance = distance;
            }
        }

        public void setNormalLogIntvl(int second) {
            if (second < 0) {
                return;
            }
            this.normalLogIntvl = second;
        }

        public void setSemiCloseLogIntvl(int second) {
            if (second < 0) {
                return;
            }
            this.semiCloseLogIntvl = second;
        }

        public void setCloseLogIntvl(int second) {
            if (second < 0) {
                return;
            }
            this.closeLogIntvl = second;
        }

        public void setEnterLogIntvl(int second) {
            if (second < 0) {
                return;
            }
            this.enterLogIntvl = second;
        }

        public void setJukiCloseLogIntvl(int second) {
            if (second < 0) {
                return;
            }
            this.jukiCloseLogIntvl = second;
        }

        public void setJukiList(ArrayList<String> dataList) {
            this.jukiList = dataList;
            this.jukiQty = dataList.size();
        }

        public void clearAreaData() {
            //dangerArea.areaDataArray.clear();
            Log.d("BLE", "clearAreaData");
            dangerArea.removeAll();
            areaData = new ArrayList<ArrayList<GeoPoint>>();
            areaQty = 0;
            outputParamToCsv();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.userID);
            dest.writeString(this.groupID);
            dest.writeByte(this.admin ? (byte) 1 : (byte) 0);
            dest.writeInt(this.areaQty);
            dest.writeList(this.areaData);
            dest.writeByte(this.enterAlert ? (byte) 1 : (byte) 0);
            dest.writeByte(this.closeAlert ? (byte) 1 : (byte) 0);
            dest.writeByte(this.jukiAlert ? (byte) 1 : (byte) 0);
            dest.writeByte(this.vibrationOn ? (byte) 1 : (byte) 0);
            dest.writeInt(this.closeVolume);
            dest.writeInt(this.enterVolume);
            dest.writeInt(this.jukiVolume);
            dest.writeByte(this.loggingOn ? (byte) 1 : (byte) 0);
            dest.writeInt(this.startHour);
            dest.writeInt(this.startMinute);
            dest.writeInt(this.startLunchHour);
            dest.writeInt(this.startLunchMinute);
            dest.writeInt(this.endLunchHour);
            dest.writeInt(this.endLunchMinute);
            dest.writeInt(this.endHour);
            dest.writeInt(this.endMinute);
            dest.writeInt(this.closeDistance);
            dest.writeInt(this.jukiDistance);
            dest.writeInt(this.normalLogIntvl);
            dest.writeInt(this.semiCloseLogIntvl);
            dest.writeInt(this.closeLogIntvl);
            dest.writeInt(this.enterLogIntvl);
            dest.writeInt(this.jukiCloseLogIntvl);
            dest.writeInt(this.jukiQty);
            dest.writeStringList(this.jukiList);
        }

        protected Parameter(Parcel in) {
            this.userID = in.readString();
            this.groupID = in.readString();
            this.admin = in.readByte() != 0;
            this.areaQty = in.readInt();
            this.areaData = new ArrayList<ArrayList<GeoPoint>>();
            in.readList(this.areaData, ArrayList.class.getClassLoader());
            this.enterAlert = in.readByte() != 0;
            this.closeAlert = in.readByte() != 0;
            this.jukiAlert = in.readByte() != 0;
            this.vibrationOn = in.readByte() != 0;
            this.closeVolume = in.readInt();
            this.enterVolume = in.readInt();
            this.jukiVolume = in.readInt();
            this.loggingOn = in.readByte() != 0;
            this.startHour = in.readInt();
            this.startMinute = in.readInt();
            this.startLunchHour = in.readInt();
            this.startLunchMinute = in.readInt();
            this.endLunchHour = in.readInt();
            this.endLunchMinute = in.readInt();
            this.endHour = in.readInt();
            this.endMinute = in.readInt();
            this.closeDistance = in.readInt();
            this.jukiDistance = in.readInt();
            this.normalLogIntvl = in.readInt();
            this.semiCloseLogIntvl = in.readInt();
            this.closeLogIntvl = in.readInt();
            this.enterLogIntvl = in.readInt();
            this.jukiCloseLogIntvl = in.readInt();
            this.jukiQty = in.readInt();
            this.jukiList = in.createStringArrayList();
        }

        public final Parcelable.Creator<Parameter> CREATOR = new Parcelable.Creator<Parameter>() {
            @Override
            public Parameter createFromParcel(Parcel source) {
                return new Parameter(source);
            }

            @Override
            public Parameter[] newArray(int size) {
                return new Parameter[size];
            }
        };
    }

    public void readAreaDataCsv(String filename) {
        dangerArea.removeAll();
        ArrayList<String> readDataStringList = new ArrayList<>();
        ArrayList<GeoPoint> geoPointList = new ArrayList<>();
        ArrayList<ArrayList<GeoPoint>> readAreaDataList = new ArrayList<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(PATH_MAIN_DIRECTORY + "/" + filename);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = "";
            line = reader.readLine();   //1行目は省く
            line = reader.readLine();   //...
            int areaNum = 0;
            int lastAreaNum = 0;
            while ((line = reader.readLine()) != null) {
                StringTokenizer stringTokenizer =
                        new StringTokenizer(line, ",");

                areaNum = Integer.parseInt(stringTokenizer.nextToken());
                double latitude = Double.parseDouble(stringTokenizer.nextToken());
                double longitude = Double.parseDouble(stringTokenizer.nextToken());

                if (lastAreaNum != areaNum) {
                    readAreaDataList.add(new ArrayList<GeoPoint>(geoPointList));
                    geoPointList.clear();
                }

                geoPointList.add(new GeoPoint(latitude, longitude));
                lastAreaNum = areaNum;

            }
            readAreaDataList.add(geoPointList);
            reader.close();
            inputStreamReader.close();
            fileInputStream.close();

            dangerArea.addAreaData(readAreaDataList);
            IMapController mapController = mMapView.getController();
            mapController.animateTo(readAreaDataList.get(0).get(0));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAreaDataCsv(String filename) {
        String deletefunctionhere = filename;
        String path = PATH_MAIN_DIRECTORY + "/" + filename;
        File file = new File(path);
        if (file.exists()) {
            String deleteCmd = "rm -r " + path;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readHistoryDataCsv(String filename) {
        Integer closeDistance = 0;
        ArrayList<ArrayList<GeoPoint>> areaPointsList = new ArrayList<>();
        ArrayList<LogData> logDataList = new ArrayList<>();
        String dateString = "";
        dateString = filename;

        try {
            File file = new File(PATH_MAIN_DIRECTORY + "/" + filename);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line = "";   //Approach Distance[m]
            line = bufferedReader.readLine();
            closeDistance = Integer.parseInt(bufferedReader.readLine());

            line = bufferedReader.readLine();   //Area Coordinates[dig.]
            int areaNum = 0;
            while (!(line = bufferedReader.readLine()).contains("Date")) {
                areaPointsList.add(new ArrayList<GeoPoint>());
                String[] areaPoints = line.split(",");
                for (int num = 0; num <= (areaPoints.length / 2) - 1; num++) {
                    Double lat = Double.parseDouble(areaPoints[num * 2]);
                    Double lon = Double.parseDouble(areaPoints[num * 2 + 1]);
                    areaPointsList.get(areaNum).add(new GeoPoint(lat, lon));
                }
                areaNum++;
            }

            line = bufferedReader.readLine();   //Date,Time,Latitude[dig.],Longitude[dig.],Status
            while ((line = bufferedReader.readLine()) != null) {
                String logLine[] = line.split(",");
                String date[] = logLine[0].split("/");
                String time[] = logLine[1].split(":");
                int year = Integer.parseInt(date[0]);
                int month = Integer.parseInt(date[1]);
                int day = Integer.parseInt(date[2]);
                int hour = Integer.parseInt(time[0]);
                int minute = Integer.parseInt(time[1]);
                int second = Integer.parseInt(time[2]);
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);
                calendar.set(year, month, day, hour, minute, second);

                Double lat = Double.parseDouble(logLine[2]);
                Double lon = Double.parseDouble(logLine[3]);

                int status = Integer.parseInt(logLine[4]);
                LocationStatus locationStatus = LocationStatus.OUTSIDE;
                switch (status) {
                    case 0:
                        locationStatus = LocationStatus.OUTSIDE;
                        break;
                    case 1:
                        locationStatus = LocationStatus.CLOSE;
                        break;
                    case 2:
                        locationStatus = LocationStatus.INSIDE;
                        break;
                }
                logDataList.add(new LogData(calendar, lat, lon, locationStatus));
            }

            fileReader.close();
            bufferedReader.close();
            bufferedReader.close();

            locationLogger.removeLogLine();
            locationLogger.removeLogArea();

            locationLogger.drawLogArea(areaPointsList);
            locationLogger.drawLogLine(logDataList);

            logRemoveButton.setText(dateString);
            logRemoveButton.setBackgroundColor(Color.argb(255, 255, 255, 0));
            logRemoveButton.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteHistoryDataCsv(String filename) {
        String deletefunctionhere = filename;
        String path = PATH_MAIN_DIRECTORY + "/" + filename;
        File file = new File(path);
        if (file.exists()) {
            String deleteCmd = "rm -r " + path;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void playHistoryDataCsv(String filename) {
        targetIconFadeout();

        fabSetMarker.setVisibility(View.INVISIBLE);
        fabUndo.setVisibility(View.INVISIBLE);
        fabNowLocation.setVisibility(View.INVISIBLE);

        logPlayLayout.setVisibility(View.VISIBLE);
        logPlayLayout.setBackgroundTintMode(PorterDuff.Mode.ADD);
        logPlayLayout.setBackgroundColor(getResources().getColor(R.color.LightGoldenrodYellow));

        logPlayToggleFab.setVisibility(View.VISIBLE);
        logPlayNextFab.setVisibility(View.VISIBLE);
        logPlayPreviousFab.setVisibility(View.VISIBLE);

        Integer closeDistance = 0;
        ArrayList<ArrayList<GeoPoint>> areaPointsList = new ArrayList<>();
        ArrayList<LogData> logDataList = new ArrayList<>();
        String dateString = "";
        dateString = filename;

        try {
            File file = new File(PATH_MAIN_DIRECTORY + "/" + filename);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line = "";   //Approach Distance[m]
            line = bufferedReader.readLine();
            closeDistance = Integer.parseInt(bufferedReader.readLine());

            line = bufferedReader.readLine();   //Area Coordinates[dig.]
            int areaNum = 0;
            while (!(line = bufferedReader.readLine()).contains("Date")) {
                areaPointsList.add(new ArrayList<GeoPoint>());
                String[] areaPoints = line.split(",");
                for (int num = 0; num <= (areaPoints.length / 2) - 1; num++) {
                    Double lat = Double.parseDouble(areaPoints[num * 2]);
                    Double lon = Double.parseDouble(areaPoints[num * 2 + 1]);
                    areaPointsList.get(areaNum).add(new GeoPoint(lat, lon));
                }
                areaNum++;
            }

            line = bufferedReader.readLine();   //Date,Time,Latitude[dig.],Longitude[dig.],Status
            while ((line = bufferedReader.readLine()) != null) {
                String logLine[] = line.split(",");
                String date[] = logLine[0].split("/");
                String time[] = logLine[1].split(":");
                int year = Integer.parseInt(date[0]);
                int month = Integer.parseInt(date[1]);
                int day = Integer.parseInt(date[2]);
                int hour = Integer.parseInt(time[0]);
                int minute = Integer.parseInt(time[1]);
                int second = Integer.parseInt(time[2]);
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);
                calendar.set(year, month, day, hour, minute, second);

                Double lat = Double.parseDouble(logLine[2]);
                Double lon = Double.parseDouble(logLine[3]);

                int status = Integer.parseInt(logLine[4]);
                LocationStatus locationStatus = LocationStatus.OUTSIDE;
                switch (status) {
                    case 0:
                        locationStatus = LocationStatus.OUTSIDE;
                        break;
                    case 1:
                        locationStatus = LocationStatus.SEMI_CLOSE;
                        break;
                    case 2:
                        locationStatus = LocationStatus.CLOSE;
                        break;
                    case 3:
                        locationStatus = LocationStatus.INSIDE;
                        break;
                }
                logDataList.add(new LogData(calendar, lat, lon, locationStatus));
            }
            fileReader.close();
            bufferedReader.close();
            bufferedReader.close();


            locationLogger.removeLogLine();
            locationLogger.removeLogArea();

            locationLogger.drawLogArea(areaPointsList);

            logPlayMode = LogPlayMode.LOG_PLAY_1x;
            logPlayModeText.setText("1x");

            logPLaySeekBar.setProgress(0);
            locationLogger.setLogline(logDataList);

            logPLaySeekBar.setMin(0);
            logPLaySeekBar.setMax(logDataList.size() - 1);
            logPlayCurrentTimeText.setText(logDataList.get(0).locationDate.getTimeString());
            logPlayTotalTimeText.setText(logDataList.get(logDataList.size() - 1).locationDate.getTimeString());

            logPLaySeekBar.invalidate();
            logPlayCurrentTimeText.invalidate();
            logPlayTotalTimeText.invalidate();

            logRemoveButton.setText(dateString);
            logRemoveButton.setBackgroundColor(getResources().getColor(R.color.LightGoldenrodYellow));
            logRemoveButton.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void targetIconFadeout() {
        // 透明度を1から0に変化
        AlphaAnimation alphaFadeout = new AlphaAnimation(1.0f, 0.0f);
        // animation時間 msec
        alphaFadeout.setDuration(1000);
        // animationが終わったそのまま表示にする
        alphaFadeout.setFillAfter(true);
        imageView.startAnimation(alphaFadeout);
    }

    private void targetIconFadein() {
// 透明度を0から1に変化
        AlphaAnimation alphaFadeIn = new AlphaAnimation(0.0f, 1.0f);
        // animation時間 msec
        alphaFadeIn.setDuration(1000);
        // animationが終わったそのまま表示にする
        alphaFadeIn.setFillAfter(true);
        imageView.startAnimation(alphaFadeIn);
    }

    public class SearchGeocode extends AsyncTask<String, Integer, List<Address>> {

        Context context = MainActivity.this;
        CustomNominatim coderNominatim = new CustomNominatim(context, "OsmdroidTest1022");
        ProgressDialog progressDialog;
        String countryTitleString;

        protected List<Address> doInBackground(String... countryTitle) {

            int i = 0;
            publishProgress(i);

            countryTitleString = Arrays.toString(countryTitle);
            coderNominatim.setService(GeocoderNominatim.NOMINATIM_SERVICE_URL);

            List<Address> geoResults = null;
            try {
                geoResults = coderNominatim.getFromLocationName(keyword, 1);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Geocoding error! Internet available?", Toast.LENGTH_SHORT).show();
            }
            return geoResults;

        }

        protected void onProgressUpdate(Integer... progress) {

            Log.i("GEOCODING", progress.toString());

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(true);
            progressDialog.setMessage("Loading ...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();

        }

        protected void onPostExecute(List<Address> geoResults) {
            super.onPostExecute(geoResults);

            progressDialog.hide();

            if (geoResults.size() == 0) { //if no address found, display an error
                Toast.makeText(context, countryTitleString + " - Country not found.", Toast.LENGTH_SHORT).show();
            } else {
                Address address = geoResults.get(0);
                Log.i("GEOCODING", Double.toString(address.getLatitude()) + "," + Double.toString(address.getLongitude()));
                Bundle extras = address.getExtras();
                org.osmdroid.util.BoundingBox bb = extras.getParcelable("boundingbox");

                IMapController mapController = mMapView.getController();
                //mapController.animateTo(new GeoPoint(bb.getCenterLatitude(),bb.getCenterLongitude()));
                mapController.animateTo(new GeoPoint(address.getLatitude(), address.getLongitude()));

                //mMapView.zoomToBoundingBox(bb);

                //makeToast(countryTitle);
            }
        }
    }

    private void setUserIdText(String text) {
        final String id = text;
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userIdText.setText("USER ID : " + id);
            }
        });
    }

    private void setGroupIdText(String text) {
        final String id = text;
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupIdText.setText("GROUP : " + id);
            }
        });
    }

    public void startLocationService() {
        if (!isLocationServiceRunning) {
            Intent startIntent = new Intent(getApplicationContext(), LocationService.class);
            startIntent.setAction("START_FOREGROUND_ACTION");
            startForegroundService(startIntent);
            isLocationServiceRunning = true;
        }
    }

    public void stopLocationService() {
        if (isLocationServiceRunning) {
            Intent stopIntent = new Intent(getApplicationContext(), LocationService.class);
            stopIntent.setAction("STOP_FOREGROUND_ACTION");
            startForegroundService(stopIntent);
            isLocationServiceRunning = false;
        }
    }

    public void startBind() {
        Log.i("", "startBind");
        if (!isBindStarted) {
            Intent reloadIntent = new Intent(getApplicationContext(), LocationService.class);
            bindService(reloadIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            isBindStarted = true;
        }
    }

    public void stopBind() {
        Log.i("", "stopBind");
        if (isBindStarted) {
            unbindService(serviceConnection);
            isBindStarted = false;
        }
    }

    private LocationService locationService;
    private boolean bound = false;

    public void reloadLocationService() {
        if (bound) {
            Log.d("BLE", "reload");
            locationService.reloadParameter();
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.MyBinder binder = (LocationService.MyBinder) service;
            locationService = binder.getService();
            locationService.setCallback(MainActivity.this);
            //locationService.reloadParameter();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationService.setCallback(null);
            bound = false;
        }
    };

    private void currentMapDownloader(final int maxLevel, final int minLevel, String path, String name) {
        File folder = new File(PATH_MAP);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            // Do something on success
        } else {
            // Do something else on failure
        }

        File dir = new File(PATH_MAP);
        File[] list = dir.listFiles();
        if (list != null) {
            for (int listNum = 0; listNum <= list.length - 1; listNum++) {
                if (list[listNum].toString() == name) {
                    Log.i("currentMapDownloader", "same name exists");
                    //return;
                }
            }
        }
        Log.i("currentMapDownloader", "[" + path + "/" + name + "]");

        BoundingBox boundingBox = mMapView.getBoundingBox();
        try {
            SqliteArchiveTileWriter writer = new SqliteArchiveTileWriter(path + "/" + name);
            org.osmdroid.tileprovider.cachemanager.CacheManager manager = new org.osmdroid.tileprovider.cachemanager.CacheManager(mMapView, writer);
            manager.downloadAreaAsync(MainActivity.this, boundingBox, minLevel, maxLevel, new org.osmdroid.tileprovider.cachemanager.CacheManager.CacheManagerCallback() {
                @Override
                public void onTaskComplete() {
                    Toast.makeText(getApplicationContext(), "map download finish", Toast.LENGTH_SHORT).show();
                    Log.i("currentMapDownloader", "Complete");
                }

                @Override
                public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
                    Log.i("currentMapDownloader", "Progress : " + Integer.toString(progress) + ", ZoomLevel : " + Integer.toString(currentZoomLevel));
                }

                @Override
                public void downloadStarted() {
                    Toast.makeText(getApplicationContext(), "map download start", Toast.LENGTH_SHORT).show();
                    Log.i("currentMapDownloader", "Start maxLevel = " + Integer.toString(maxLevel) + ", minLevel = " + Integer.toString(minLevel));
                }

                @Override
                public void setPossibleTilesInArea(int total) {
                    Log.i("currentMapDownloader", "setPossibleTilesInArea");
                }

                @Override
                public void onTaskFailed(int errors) {
                    Log.i("currentMapDownloader", "Failed");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int fetchAccentColor() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        int color = typedValue.data;
        return color;
    }

    private int fetchPrimaryColor() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int color = typedValue.data;
        return color;
    }


}


