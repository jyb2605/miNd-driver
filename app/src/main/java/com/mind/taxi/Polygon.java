package com.mind.taxi;

import com.google.android.gms.maps.model.LatLng;

public class Polygon {

    String name;
    LatLng latlng;
    int radius;
    int color;
    int speed_limit;

    boolean visited =false;


    Polygon(String name, LatLng latlng, int radius, int color, int speed_limit) {
        this.name = name;
        this.latlng = latlng;
        this.radius = radius;
        this.color=color;
        this.speed_limit = speed_limit;
    }


}