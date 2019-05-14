package com.mind.taxi;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by nul010 on 2017-05-11.
 */

public class PermissionUtil {
    public static final int REQUEST_LOCATION = 1;
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION

    };

    public static boolean checkPermissions(Activity activity, String permision){
        int permissionResult = ActivityCompat.checkSelfPermission(activity,permision);
        if (permissionResult == PackageManager.PERMISSION_GRANTED)  return true;
        else return false;
    }

    public static void requestLocationFinePermissions(Activity activity){
        ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION ,REQUEST_LOCATION);
    }

    public static boolean verifyPermission(int[] grantresults){
        if(grantresults.length < 1){
            return false;
        }
        for (int result : grantresults){
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


}
