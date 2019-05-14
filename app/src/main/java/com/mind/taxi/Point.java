package com.mind.taxi;

import com.google.android.gms.maps.model.LatLng;

public class Point {

    String name;
    String address;
    LatLng latlng;
    String description;

    boolean visited =false;


    Point(String name, String address, LatLng latlng, String description) {
        this.name = name;
        this.address = address;
        this.latlng = latlng;
        this.description=description;
    }


}