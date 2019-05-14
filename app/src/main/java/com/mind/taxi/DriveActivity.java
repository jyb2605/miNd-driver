package com.mind.taxi;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mind.taxi.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DriveActivity extends BaseActivity implements LocationListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    ExitDialog exit_dialog;
    AVLoadingIndicatorView avi;

    Handler handler;


    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    boolean mLocationPermissionGranted;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;
    Location mLastLocation;
    CameraPosition mCameraPosition;
    private static final String CAMERA_POSITION = "camera_position";
    private static final String LOCATION = "location";
    double speed = 0;
    TextView speed_view;
    TextView destination;

    PolylineOptions polylineOptions;

    ArrayList<Point> points_list = new ArrayList<>();
    ArrayList<Marker> markers_list = new ArrayList<>();

    ArrayList<Polygon> circles_list = new ArrayList<>();


    TextToSpeech tts;

    TextView safe_point_view;

    int speed_limit=60;

    int i;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION);
        }


        tts = new TextToSpeech(DriveActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }

            }
        });


        circlesListInit();

        safe_point_view = (TextView) findViewById(R.id.safe_point);

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


        exit_dialog = new ExitDialog(this);
        handler = new Handler();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        speed_view = (TextView) findViewById(R.id.speed_view);

        buildGoogleApiClient();
        mGoogleApiClient.connect();


        destination = (TextView) findViewById(R.id.end);
        destination.setText(LoadingActivity.end.name);


        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(10);


        HttpUtil httpUtil = new HttpUtil();
//        Log.e("URL", "https://apis.skplanetx.com/tmap/routes?callback=&version=1" +
//                "&reqCoordType=WGS84GEO" +
//                "&resCoordType=WGS84GEO" +
//                "&appKey=ad198215-d00d-3aed-a5e9-652d17dfa1ea" +
//                "&endName=목적지" +
//                "&endX="+ LoadingActivity.end.latlng.longitude +
//                "&endY="+ LoadingActivity.end.latlng.latitude +
//                "&startName=출발지" +
//                "&startX=" + LoadingActivity.start.latlng.longitude +
//                "&startY=" + LoadingActivity.start.latlng.latitude);

        httpUtil.setUrl("https://apis.skplanetx.com/tmap/routes?callback=&version=1" +
                "&reqCoordType=WGS84GEO" +
                "&resCoordType=WGS84GEO" +
                "&appKey=ad198215-d00d-3aed-a5e9-652d17dfa1ea" +
                "&endName=목적지" +
                "&endX=" + LoadingActivity.end.latlng.longitude +
                "&endY=" + LoadingActivity.end.latlng.latitude +
                "&startName=출발지" +
                "&startX=" + LoadingActivity.start.latlng.longitude +
                "&startY=" + LoadingActivity.start.latlng.latitude)
                .setCallback(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
//                        Log.e("RESPONSE", response.body().string());

                        double longitude;
                        double latitude;
                        JSONObject jObject = null;   // JSONArray 생성
                        try {
                            jObject = new JSONObject(response.body().string());
                            JSONArray features = jObject.getJSONArray("features");
                            for (int i = 0; i < features.length(); i++) {
                                jObject = features.getJSONObject(i);
                                JSONObject geometry = jObject.getJSONObject("geometry");
                                JSONObject properties = jObject.getJSONObject("properties");

                                final String description = properties.getString("description");
                                String type = geometry.getString("type");
                                JSONArray coordinates = geometry.getJSONArray("coordinates");
//                                Log.e("coordinates", String.valueOf(coordinates));


                                final String name = properties.getString("name");


                                if (type.equals("Point")) {
                                    longitude = coordinates.getDouble(0);
                                    latitude = coordinates.getDouble(1);
//                                        Log.e("coordinates", String.valueOf(longitude) + "," + String.valueOf(latitude));

                                    final Point point = new Point(name, "", new LatLng(latitude, longitude), description);
                                    points_list.add(point);


                                    final double finalLatitude = latitude;
                                    final double finalLongitude = longitude;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Marker marker = mMap.addMarker(new MarkerOptions().position(point.latlng).title(name));
                                            markers_list.add(marker);
                                        }
                                    });
                                    polylineOptions.add(new LatLng(latitude, longitude));
                                } else {
                                    for (int j = 0; j < coordinates.length(); j++) {
                                        longitude = coordinates.getJSONArray(j).getDouble(0);
                                        latitude = coordinates.getJSONArray(j).getDouble(1);
//                                        Log.e("coordinates", String.valueOf(longitude) + "," + String.valueOf(latitude));
                                        polylineOptions.add(new LatLng(latitude, longitude));
                                    }
                                }
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mMap.addPolyline(polylineOptions);
                                }
                            });


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
//                .setData("startX", String.valueOf(LoadingActivity.start.latlng.longitude))
//                .setData("startY", String.valueOf(LoadingActivity.start.latlng.latitude))
//                .setData("endX", String.valueOf(LoadingActivity.end.latlng.longitude))
//                .setData("endY", String.valueOf(LoadingActivity.end.latlng.latitude))
//                .setData("startName", "출발지")
//                .setData("endName", "도착지")
                .setMethod(httpUtil.HTTP_METHOD_GET)
                .execute();


    }

    private void circlesListInit() {
        circles_list.add(new Polygon("교통 사고 다발지역", new LatLng(37.499780, 127.026933),  200, Color.argb(30, 255, 0, 0), 50));
        circles_list.add(new Polygon("어린이 보호 구역", new LatLng(37.504367, 127.024383),  100, Color.argb(30, 122, 162, 232), 30));

        circles_list.add(new Polygon("어린이 보호 구역", new LatLng(37.508445, 127.022656),  100, Color.argb(30, 255, 0, 0), 50));
        circles_list.add(new Polygon("스쿨존", new LatLng(37.511150, 127.021364),  100, Color.argb(30, 122, 162, 232), 50));

        circles_list.add(new Polygon("어린이 보호 구역", new LatLng(37.505422, 127.028397),  100, Color.argb(30, 255, 0, 0), 50));
        circles_list.add(new Polygon("스쿨존", new LatLng(37.509201, 127.032989),  100, Color.argb(30, 122, 162, 232), 50));

        circles_list.add(new Polygon("차량 속도 제한 구역", new LatLng(37.515689, 127.035878),  200, Color.argb(30, 255, 0, 0), 50));









                //String name, String address, LatLng latlng, String description

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == PermissionUtil.REQUEST_LOCATION) {
            if (PermissionUtil.verifyPermission(grantResults)) {
                mLocationPermissionGranted = true;
            } else {
                showRequestAgainDialog();
            }
        }
        updateLocationUI();
    }

    @SuppressWarnings("MissingPermission")
    private void updateLocationUI() {
        if (mMap == null) return;
        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }


        for(Polygon circle : circles_list){
            mMap.addCircle(new CircleOptions()
                    .center(circle.latlng)
                    .radius(circle.radius)
                    .strokeWidth(10)
                    .strokeColor(circle.color)
                    .fillColor(circle.color)
                    .clickable(true));
        }

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 16));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
        updateLocationUI();

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = mCurrentLocation;
        mCurrentLocation = location;

//        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));

        if (this.mLastLocation != null) {


            for (final Point point : points_list) {
                float[] distance = new float[2]; // float 형태의 사이즈 2의 행렬 생성
                float actual_distance; //실제 거리 값을 담을 변수
                Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), point.latlng.latitude, point.latlng.longitude, distance);
                actual_distance = distance[0]; // m 단위

                if (actual_distance < 50 && !point.visited) {
                    Toast.makeText(DriveActivity.this, point.name, Toast.LENGTH_SHORT).show();



                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (point.description.equals("도착"))
                            ttsGreater21("목적지에 도착 하였습니다.");
                        else
                            ttsGreater21(point.description + " 하세요. ");
                    } else {
                        if (point.description.equals("도착"))
                            ttsUnder20("목적지에 도착 하였습니다.");
                        else
                            ttsUnder20(point.description + " 하세요. ");
                    }
                    point.visited = true;
                }
            }


            speed_limit=60;
            //폴리곤
            for (final Polygon polygon : circles_list) {
                float[] distance = new float[2]; // float 형태의 사이즈 2의 행렬 생성
                float actual_distance; //실제 거리 값을 담을 변수
                Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), polygon.latlng.latitude, polygon.latlng.longitude, distance);
                actual_distance = distance[0]; // m 단위

                if (actual_distance < polygon.radius ) {
                    speed_limit = polygon.speed_limit;
                    if(!polygon.visited) {
                        Toast.makeText(DriveActivity.this, polygon.name, Toast.LENGTH_SHORT).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ttsGreater21(polygon.name + " 입니다. ");
                        } else {
                            ttsUnder20(polygon.name + " 입니다. ");
                        }
                        polygon.visited = true;
                    }
                }
            }













            float[] distance = new float[2]; // float 형태의 사이즈 2의 행렬 생성
            float actual_distance; //실제 거리 값을 담을 변수
            Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), mLastLocation.getLatitude(), mLastLocation.getLongitude(), distance);
            actual_distance = distance[0]; // m 단위

            Log.e("SAFEPOINT", String.valueOf(HomeActivity.safe_point));


            speed = actual_distance / (mCurrentLocation.getTime() - mLastLocation.getTime());
            if ((int) (speed * 1800) != 0) {
                speed_view.setText((int) (speed * 1800) + "/" + speed_limit +" km");
                if((int) (speed * 1800) >  speed_limit){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for(i=0; i<10; i++){
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                HomeActivity.safe_point--;

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        safe_point_view.setTextColor(Color.RED);
                                        safe_point_view.setText(String.valueOf(HomeActivity.safe_point));
                                    }
                                });
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    safe_point_view.setTextColor(Color.BLACK);

                                }
                            });

                        }
                    }).start();

                }else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for(i=0; i<2; i++){
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                HomeActivity.safe_point++;

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        safe_point_view.setText(String.valueOf(HomeActivity.safe_point));
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }


        }



//        if (mCurrentLocation.hasSpeed()) {
//            speed = mCurrentLocation.getSpeed();
//            speed_view.setText("Speed : " + speed * 3600 / 1000 + " km/h");
//            Log.e("Speed", speed + " km/h")
//        };


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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
    public void onConnected(@Nullable Bundle bundle) {
        getDeviceLocation();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(LOCATION, mCurrentLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
//        if(tts!=null)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId = this.hashCode() + "";
//        if(tts!=null)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
}
