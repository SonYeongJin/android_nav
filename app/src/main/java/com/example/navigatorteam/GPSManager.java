package com.example.navigatorteam;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.skt.tmap.TMapView;

public class GPSManager implements LocationListener {
    private Context context;
    private LocationManager locationManager;
    private TMapView tMapView;
    private LocationUpdateListener locationUpdateListener;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public interface LocationUpdateListener {
        void onLocationUpdated(double latitude, double longitude);
    }

    public GPSManager(Context context, TMapView tMapView, LocationUpdateListener locationUpdateListener) {
        this.context = context;
        this.tMapView = tMapView;
        this.locationUpdateListener = locationUpdateListener;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // 위치 권한 확인 및 요청
        checkLocationPermission();
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        tMapView.removeAllTMapMarkerItem();
        setCurrentLocation();

        // 콜백 호출
        if (locationUpdateListener != null) {
            locationUpdateListener.onLocationUpdated(latitude, longitude);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청
            ActivityCompat.requestPermissions((AppCompatActivity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 권한이 부여되었으면 위치 정보 가져오기
            getLocation();
        }
    }

    public void getLocation() {
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
            } else {
                Toast.makeText(context, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void setCurrentLocation() {
        // 현재 위치를 TMapView에 설정하는 로직 구현
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 부여되면 위치 정보 가져오기
                getLocation();
            } else {
                Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
