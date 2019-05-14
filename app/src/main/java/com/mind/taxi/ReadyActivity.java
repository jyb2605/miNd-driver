package com.mind.taxi;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mind.taxi.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ReadyActivity extends BaseActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
    ExitDialog exit_dialog;
    AVLoadingIndicatorView avi;

    Handler handler;

    Button ok;
    TextView start;
    private GoogleMap mMap;
    private PolylineOptions polylineOptions;

    Location mLastLocation;
    Location mCurrentLocation;
    double speed = 0;
    TextView speed_view;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    boolean mLocationPermissionGranted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);


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


        start = (TextView) findViewById(R.id.start);
        start.setText(LoadingActivity.start.name);
        speed_view = (TextView) findViewById(R.id.speed_view);


        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        buildGoogleApiClient();


        ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (SocketUtil.socket.isConnected()) {
                            try {
                                SocketUtil.output.writeUTF("c`3`");
                                SocketUtil.output.flush();
                                //003 : 탑승완료
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            startActivity(new Intent(ReadyActivity.this, DriveActivity.class));
                            finish();
                        }
                    }
                }).start();


            }
        });






        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(10);




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
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LoadingActivity.start.latlng.latitude, LoadingActivity.start.latlng.longitude),14));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LoadingActivity.start.latlng.latitude, LoadingActivity.start.latlng.longitude),14));


//        Log.e("진입?", "진입해라");

        HttpUtil httpUtil = new HttpUtil();
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
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mMap.addMarker(new MarkerOptions().position(point.latlng).title(name));
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
                .setMethod(httpUtil.HTTP_METHOD_GET)
                .execute();
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
