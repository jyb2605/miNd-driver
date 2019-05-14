package com.mind.taxi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mind.taxi.R;
import com.wang.avi.AVLoadingIndicatorView;

public class LoadingActivity extends BaseActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static CallDialog call_dialog;
    AVLoadingIndicatorView avi;
    Thread tcp;
    Handler handler;
    static Point start;
    static Point end;
    boolean mLocationPermissionGranted;

    Location mLastLocation;
    Location mCurrentLocation;
    double speed = 0;
    TextView speed_view;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    static LoadingActivity activity;
    static Context mContext;

    static boolean loading_activity_running = true;

    void startAnim() {
        avi.show();
        // or avi.smoothToShow();
    }

    void stopAnim() {
        avi.hide();
        // or avi.smoothToHide();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == PermissionUtil.REQUEST_LOCATION) {
            if (PermissionUtil.verifyPermission(grantResults)) {
                mLocationPermissionGranted = true;
            } else {
                showRequestAgainDialog();
            }
        }
//        updateLocationUI();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        createLocationRequest();
    }


    private void showRequestAgainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("이 권한은 꼭 필요한 권한이므로, 설정에서 활성화부탁드립니다.");
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //취소
            }
        });
        builder.create();
    }

    @SuppressWarnings("MissingPermission")
    void getDeviceLocation() {
        if (PermissionUtil.checkPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mLocationPermissionGranted = true;
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this
            );
        } else {
            PermissionUtil.requestLocationFinePermissions(this);
        }
    }

    @Override
    protected void onResume() {
        if (mGoogleApiClient.isConnected()) {
            getDeviceLocation();
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        loading_activity_running = true;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
//        toolbar.setNavigationIcon(R.drawable.na);

        setSupportActionBar(toolbar);
//        toolbar.setNavigationIcon(R.drawable.btn_ham_selector);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.btn_ham_selector));

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        View headerView = navigationView.getHeaderView(0);
        TextView safe_point_view = (TextView) headerView.findViewById(R.id.safe_point_view);
        safe_point_view.setText("나의 점수 : " + HomeActivity.safe_point + "점");

        speed_view = (TextView) findViewById(R.id.speed_view);

        buildGoogleApiClient();


//        createDialogInstance();

        mContext = this;
        activity = this;

        handler = new Handler();


        avi = (AVLoadingIndicatorView) findViewById(R.id.avi);
        avi.setIndicator("BallPulseIndicator");


        startAnim();


    }

    public static void createDialogInstance(){
        call_dialog = new CallDialog(mContext, activity);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        loading_activity_running = false;

    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = mCurrentLocation;
        mCurrentLocation = location;


        if (this.mLastLocation != null) {
            float[] distance = new float[2]; // float 형태의 사이즈 2의 행렬 생성
            float actual_distance; //실제 거리 값을 담을 변수
            Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), mLastLocation.getLatitude(), mLastLocation.getLongitude(), distance);
            actual_distance = distance[0]; // m 단위


            speed = actual_distance / (mCurrentLocation.getTime() - mLastLocation.getTime());
            if ((int) (speed * 1800) != 0) {
                speed_view.setText((int) (speed * 1800) + "/90 km");
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getDeviceLocation();
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
