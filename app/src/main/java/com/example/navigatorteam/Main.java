package com.example.navigatorteam;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import org.w3c.dom.Element;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.navigatorteam.databinding.ActivityMainBinding;
import com.skt.tmap.TMapData;
import com.skt.tmap.TMapInfo;
import com.skt.tmap.TMapPoint;
import com.skt.tmap.TMapView;
import com.skt.tmap.overlay.TMapMarkerItem;
import com.skt.tmap.overlay.TMapPolyLine;
import com.skt.tmap.poi.TMapPOIItem;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class Main extends AppCompatActivity implements GPSManager.LocationUpdateListener {
    private GPSManager gpsManager;
    private ImageView zoomInImage;
    private LinearLayout routeLayout;
    private TextView routeDistanceTextView;
    private TextView routeTimeTextView;
    private TextView routedescriptionTextView;
    private ImageView zoomOutImage;
    private TextView zoomLevelTextView;
    private ActivityMainBinding binding;
    private LinearLayout autoCompleteLayout;
    private EditText autoCompleteEdit;
    private TMapView tMapView;
    private ListView autoCompleteListView;
    private AutoCompleteListAdapter autoCompleteListAdapter;
    private TMapPoint nowpoint;

    @Override
    public void onLocationUpdated(double latitude, double longitude) {
        // 현재 위치가 업데이트되면 호출됩니다.
        nowpoint = new TMapPoint(latitude, longitude);

        tMapView.removeAllTMapMarkerItem();
        addMarker2(nowpoint, "현재 위치", "현 위치입니다.");
        tMapView.setCenterPoint(latitude, longitude);

        // 여기서 위치 정보를 사용하여 필요한 작업을 수행합니다.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        gpsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void addMarker2(TMapPoint point, String title, String subtitle) {
        TMapMarkerItem marker = new TMapMarkerItem();
        marker.setId("현재위치");
        marker.setTMapPoint(point);
        marker.setCanShowCallout(true);
        marker.setCalloutTitle(title);
        marker.setCalloutSubTitle(subtitle);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.poi_dot);
        marker.setIcon(icon);
        tMapView.addTMapMarkerItem(marker);
        tMapView.setCenterPoint(point.getLatitude(), point.getLongitude());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TMapView 초기화
        tMapView = new TMapView(this);
        binding.tmapViewContainer.addView(tMapView);

        // TMapView 설정
        tMapView.setSKTMapApiKey("pRNUlsEpce4d3mB0MUabnMDhHbLmdtlPrUYZI3i0");
        gpsManager = new GPSManager(this, tMapView, this);
        // 맵 로딩 완료 리스너 설정
        tMapView.setOnMapReadyListener(new TMapView.OnMapReadyListener() {
            @Override
            public void onMapReady() {
                // 맵 로딩 완료 후 처리할 작업
                Log.d("MainActivity", "TMapView is ready");
                gpsManager.getLocation();
                gpsManager.startLocationUpdates();
                initAutoComplete();
            }
        });
        zoomInImage = findViewById(R.id.zoomInImage);
        zoomInImage.setOnClickListener(onClickListener);
        zoomOutImage = findViewById(R.id.zoomOutImage);
        zoomOutImage.setOnClickListener(onClickListener);

        // 어댑터 초기화
        autoCompleteListAdapter = new AutoCompleteListAdapter(this);

        // 뷰 초기화
        zoomInImage = findViewById(R.id.zoomInImage);
        zoomOutImage = findViewById(R.id.zoomOutImage);
        autoCompleteLayout = findViewById(R.id.autoCompleteLayout);
        autoCompleteEdit = findViewById(R.id.autoCompleteEdit);
        autoCompleteListView = findViewById(R.id.autoCompleteListView);
        routeLayout = findViewById(R.id.routeLayout);
        routeDistanceTextView = findViewById(R.id.routeDistanceText);
        routeTimeTextView = findViewById(R.id.routeTimeText);
        routedescriptionTextView = findViewById(R.id.routedescriptionTextView);

        // 어댑터 설정
        autoCompleteListView.setAdapter(autoCompleteListAdapter);

        Button buttonWalkingRoute = findViewById(R.id.buttonWalkingRoute);
        buttonWalkingRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, WalkingRouteActivity.class);
                startActivity(intent);
            }
        });

        Button buttonHome = findViewById(R.id.buttonHome);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(zoomInImage)) {
                tMapView.mapZoomIn();
            } else if (v.equals(zoomOutImage)) {
                tMapView.mapZoomOut();
            }
        }
    };


    private void initAutoComplete() {
        autoCompleteLayout = findViewById(R.id.autoCompleteLayout);
        autoCompleteLayout.setVisibility(View.VISIBLE);
        autoCompleteEdit = findViewById(R.id.autoCompleteEdit);
        autoCompleteListView = findViewById(R.id.autoCompleteListView);
        autoCompleteListAdapter = new AutoCompleteListAdapter(this);
        autoCompleteListView.setAdapter(autoCompleteListAdapter);
        autoCompleteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String keyword = (String) autoCompleteListAdapter.getItem(position);

                findAllPoi(keyword);
                autoCompleteLayout.setVisibility(View.GONE);
            }
        });

        autoCompleteEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString();

                TMapData tMapData = new TMapData();

                tMapData.autoComplete(keyword, new TMapData.OnAutoCompleteListener() {
                    @Override
                    public void onAutoComplete(ArrayList<String> itemList) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                autoCompleteListAdapter.setItemList(itemList);
                            }
                        });

                    }
                });

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    public void findAllPoi(String strData) {
        TMapData tmapdata = new TMapData();
        tmapdata.findAllPOI(strData, new TMapData.OnFindAllPOIListener() {
            @Override
            public void onFindAllPOI(ArrayList<TMapPOIItem> poiItemList) {
                showPOIResultDialog(poiItemList);
            }
        });
    }

    private void showPOIResultDialog(final ArrayList<TMapPOIItem> poiItem) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (poiItem != null) {
                    CharSequence[] item = new CharSequence[poiItem.size()];
                    for (int i = 0; i < poiItem.size(); i++) {
                        item[i] = poiItem.get(i).name;
                    }
                    new AlertDialog.Builder(Main.this)
                            .setTitle("POI 검색 결과")
                            .setIcon(R.drawable.icon)
                            .setItems(item, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.dismiss();
                                    initAll();
                                    TMapPOIItem poi = poiItem.get(i);
                                    TMapMarkerItem marker = new TMapMarkerItem();
                                    marker.setId(poi.id);
                                    marker.setTMapPoint(poi.getPOIPoint());
                                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.poi_dot);
                                    marker.setIcon(icon);

                                    marker.setCalloutTitle(poi.getPOIName());
                                    marker.setCalloutSubTitle("주소:" + poi.getPOIAddress());

                                    marker.setCanShowCallout(true);

                                    marker.setAnimation(true);
                                    tMapView.addTMapMarkerItem(marker);

                                    tMapView.setCenterPoint(poi.getPOIPoint().getLatitude(), poi.getPOIPoint().getLongitude());
                                }
                            }).create().show();
                }

            }
        });

    }

    private void initAll() {

        tMapView.removeAllTMapMarkerItem2();
        tMapView.removeAllTMapMarkerItem();
        tMapView.removeAllTMapPolyLine();
        tMapView.removeAllTMapPolygon();
        tMapView.removeAllTMapCircle();
        tMapView.removeAllTMapPOIItem();
        tMapView.removeAllTMapOverlay();
        tMapView.removeTMapPath();
        tMapView.setPOIScale(TMapView.POIScale.NORMAL);
    }
}