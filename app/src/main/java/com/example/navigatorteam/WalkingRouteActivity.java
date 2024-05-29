package com.example.navigatorteam;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.Manifest;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navigatorteam.databinding.ActivityMainBinding;
import com.skt.tmap.TMapData;
import com.skt.tmap.TMapInfo;
import com.skt.tmap.TMapPoint;
import com.skt.tmap.TMapView;
import com.skt.tmap.overlay.TMapMarkerItem;
import com.skt.tmap.overlay.TMapPolyLine;
import com.skt.tmap.poi.TMapPOIItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class WalkingRouteActivity extends AppCompatActivity implements LocationListener {
    private ImageView zoomInImage;
    private LinearLayout routeLayout;
    private TextView routeDistanceTextView;
    private TextView routeTimeTextView;
    private TextView routedescriptionTextView;
    private ImageView zoomOutImage;
    private TextView zoomLevelTextView;
    private ActivityMainBinding binding;
    private LinearLayout autoCompleteLayout;
    private EditText autoCompleteEditStart;
    private EditText autoCompleteEditEnd;
    private TMapView tMapView;
    private ListView autoCompleteListViewStart;
    private ListView autoCompleteListViewEnd;
    private AutoCompleteListAdapter autoCompleteListAdapterStart;
    private AutoCompleteListAdapter autoCompleteListAdapterEnd;
    private TMapPoint startPoint;
    private TMapPoint endPoint;
    private TMapPoint nowposition;
    private Document pathDocument;
    private TMapPoint positon;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    @Override
    public void onLocationChanged(Location location) {
        // 위치가 변경될 때 호출됩니다.
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        // 위치 정보를 사용할 수 있습니다. 여기서는 토스트 메시지로 출력합니다.
        setCurrentLocation();
    }

    private void checkLocationPermission() {
        // 위치 권한이 부여되어 있는지 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 사용자에게 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 권한이 이미 부여되었으면 위치 정보 가져오기
            getLocation();
        }
    }

    private void getLocation() {
        // 위치 관리자 가져오기
        android.location.LocationManager locationManager = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            // GPS 또는 네트워크를 통해 마지막으로 알려진 위치 가져오기
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // 위치 정보를 사용할 수 있습니다. 여기서는 토스트 메시지로 출력합니다.

            } else {
                // 마지막으로 알려진 위치가 없는 경우, 새로운 위치 업데이트를 요청합니다.
                // 이 부분은 필요에 따라 구현하실 수 있습니다.
                Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 부여되면 위치 정보 가져오기
                getLocation();
            } else {
                // 사용자가 권한을 거부한 경우 메시지를 표시합니다.
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_walking_route);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 위치 권한 확인 및 요청
        checkLocationPermission();
        try {
            // GPS를 통해 위치 업데이트를 요청합니다.
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        } catch (SecurityException e) {
            e.printStackTrace();
        }

        tMapView = new TMapView(this);
        FrameLayout mapViewContainer = findViewById(R.id.tmapViewContainer); // 맵을 추가할 컨테이너 레이아웃 찾기
        mapViewContainer.addView(tMapView);
        // TMapView 설정
        tMapView.setSKTMapApiKey("pRNUlsEpce4d3mB0MUabnMDhHbLmdtlPrUYZI3i0");

        // 맵 로딩 완료 리스너 설정
        tMapView.setOnMapReadyListener(new TMapView.OnMapReadyListener() {
            @Override
            public void onMapReady() {
                // 맵 로딩 완료 후 처리할 작업
                Log.d("MainActivity", "TMapView is ready");
                initAutoComplete();
                showCurrentLocationMarker();
                getLocation();
                setNowposition();
            }
        });
        zoomInImage = findViewById(R.id.zoomInImage);
        zoomInImage.setOnClickListener(onClickListener);
        zoomOutImage = findViewById(R.id.zoomOutImage);
        zoomOutImage.setOnClickListener(onClickListener);

        // 뷰 초기화
        zoomInImage = findViewById(R.id.zoomInImage);
        zoomOutImage = findViewById(R.id.zoomOutImage);
        autoCompleteLayout = findViewById(R.id.autoCompleteLayout);
        autoCompleteEditStart = findViewById(R.id.autoCompleteEditStart);
        autoCompleteEditEnd = findViewById(R.id.autoCompleteEditEnd);
        autoCompleteListViewStart = findViewById(R.id.autoCompleteListViewStart);
        autoCompleteListViewEnd = findViewById(R.id.autoCompleteListViewEnd);
        routeLayout = findViewById(R.id.routeLayout);
        routeDistanceTextView = findViewById(R.id.routeDistanceText);
        routeTimeTextView = findViewById(R.id.routeTimeText);
        routedescriptionTextView = findViewById(R.id.routedescriptionTextView);

        // 어댑터 초기화
        autoCompleteListAdapterStart = new AutoCompleteListAdapter(this);
        autoCompleteListAdapterEnd = new AutoCompleteListAdapter(this);

        // 어댑터 설정
        autoCompleteListViewStart.setAdapter(autoCompleteListAdapterStart);
        autoCompleteListViewEnd.setAdapter(autoCompleteListAdapterEnd);
        initAutoComplete();

        autoCompleteListViewStart = findViewById(R.id.autoCompleteListViewStart);
        autoCompleteListViewEnd = findViewById(R.id.autoCompleteListViewEnd);
        Button ReButton = findViewById(R.id.Rebutton);
        Button findRouteButton = findViewById(R.id.findPathButton);
        Button WalikngRoutebutton = findViewById(R.id.WalikngRoutebutton);
        WalikngRoutebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // UI 업데이트 코드 작성
                        hideKeyboard();
                        tMapView.removeAllTMapMarkerItem();
                        autoCompleteLayout.setVisibility(View.GONE);
                        ReButton.setVisibility(View.VISIBLE);
                        findRouteButton.setVisibility(View.VISIBLE);
                    }
                });
                findPathAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, new TMapData.OnFindPathDataAllTypeListener() {
                    @Override
                    public void onFindPathDataAllType(Document doc) {
                        // 경로 안내가 완료되었음을 표시하고 길찾기 버튼을 활성화
                        pathDocument = doc;
                    }
                });
            }
        });
        ReButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoCompleteLayout.setVisibility(View.VISIBLE);
            }
        });
        findRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pathDocument != null) {
                    displayPathDetails(pathDocument);
                    centerPositon();
                }
            }
        });
        Button homebutton = findViewById(R.id.homebutton);
        homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WalkingRouteActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        Button search_placeButton = findViewById(R.id.search_place);
        search_placeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WalkingRouteActivity.this, Main.class);
                startActivity(intent);
            }
        });
        Button currentLocationButton = findViewById(R.id.currentLocationButton);
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 위치를 가져와 출발지로 설정하는 함수 호출
                setCurrentLocationAsStartPoint();
            }
        });
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
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
        autoCompleteLayout.setVisibility(View.VISIBLE);
        autoCompleteEditStart.addTextChangedListener(new TextWatcher() {
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
                                autoCompleteListViewStart.setVisibility(View.VISIBLE);
                                autoCompleteListAdapterStart.setItemList(itemList);
                            }
                        });
                    }
                });
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        autoCompleteEditEnd.addTextChangedListener(new TextWatcher() {
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
                                autoCompleteListAdapterEnd.setItemList(itemList);
                                autoCompleteListViewEnd.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        autoCompleteListViewStart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String keyword = (String) autoCompleteListAdapterStart.getItem(position);
                findAllPoi(keyword, true);
                autoCompleteListViewStart.setVisibility(View.GONE);

            }
        });

        autoCompleteListViewEnd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String keyword = (String) autoCompleteListAdapterEnd.getItem(position);
                findAllPoi(keyword, false);
                autoCompleteListViewEnd.setVisibility(View.GONE);

            }
        });
    }

    public void findAllPoi(String strData, boolean isStartPoint) {
        TMapData tmapdata = new TMapData();
        tmapdata.findAllPOI(strData, new TMapData.OnFindAllPOIListener() {
            @Override
            public void onFindAllPOI(ArrayList<TMapPOIItem> poiItemList) {
                showPOIResultDialog(poiItemList, isStartPoint);
            }
        });
    }


    private void showPOIResultDialog(final ArrayList<TMapPOIItem> poiItem, boolean isStartPoint) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (poiItem != null) {
                    CharSequence[] item = new CharSequence[poiItem.size()];
                    for (int i = 0; i < poiItem.size(); i++) {
                        item[i] = poiItem.get(i).name;
                    }
                    AlertDialog dialog = new AlertDialog.Builder(WalkingRouteActivity.this)
                            .setTitle("검색 결과입니다.")
                            .setIcon(R.drawable.ic_searchlist) // 포이 검색 결과 아이콘 수정해야 함
                            .setItems(item, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.dismiss();
                                    TMapPOIItem poi = poiItem.get(i);
                                    if (isStartPoint) {
                                        startPoint = poi.getPOIPoint();
                                        autoCompleteEditStart.setText(poi.getPOIName());
                                        autoCompleteListViewStart.setVisibility(View.GONE);
                                    } else {
                                        endPoint = poi.getPOIPoint();
                                        autoCompleteEditEnd.setText(poi.getPOIName());
                                        autoCompleteListViewEnd.setVisibility(View.GONE);
                                    }
                                    addMarker(poi);
                                }
                            }).create();
                    dialog.show();
                }
            }
        });
    }


    private void addMarker(TMapPOIItem poi) {
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
    private void findPathAllType(final TMapData.TMapPathType type, final TMapData.OnFindPathDataAllTypeListener listener) {
        TMapData data = new TMapData();
        data.findPathDataAllType(type, startPoint, endPoint, new TMapData.OnFindPathDataAllTypeListener() {
            @Override
            public void onFindPathDataAllType(Document doc) {
                tMapView.removeTMapPath();

                TMapPolyLine polyline = new TMapPolyLine();
                polyline.setID(type.name());
                polyline.setLineWidth(10);
                polyline.setLineColor(Color.RED);
                polyline.setLineAlpha(255);
                if (doc != null) {
                    NodeList list = doc.getElementsByTagName("Document");
                    Element item2 = (Element) list.item(0);
                    String totalDistance = getContentFromNode(item2, "tmap:totalDistance");
                    String totalTime = getContentFromNode(item2, "tmap:totalTime");

                    NodeList list2 = doc.getElementsByTagName("LineString");

                    for (int i = 0; i < list2.getLength(); i++) {
                        Element item = (Element) list2.item(i);
                        String str = getContentFromNode(item, "coordinates");
                        if (str == null) {
                            continue;
                        }

                        String[] str2 = str.split(" ");
                        for (int k = 0; k < str2.length; k++) {
                            try {
                                String[] str3 = str2[k].split(",");
                                TMapPoint point = new TMapPoint(Double.parseDouble(str3[1]), Double.parseDouble(str3[0]));
                                polyline.addLinePoint(point);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    tMapView.setTMapPath(polyline);

                    TMapInfo info = tMapView.getDisplayTMapInfo(polyline.getLinePointList());

                    tMapView.setZoomLevel(16);
                    tMapView.setCenterPoint(info.getPoint().getLatitude(), info.getPoint().getLongitude());

                    setPathText(totalDistance, totalTime);

                    // 새로 추가된 부분: 외부에서 전달된 리스너를 호출하여 처리 결과를 전달
                    listener.onFindPathDataAllType(doc);
                }
            }
        });
    }
    private void displayPathDetails(Document doc) {
        tMapView.removeAllTMapMarkerItem();
        if (doc != null) {
            NodeList placemarkList = doc.getElementsByTagName("Placemark");
            List<String> descriptionList = new ArrayList<>();

            for (int i = 0; i < placemarkList.getLength(); i++) {
                Element placemark = (Element) placemarkList.item(i);
                String description = getContentFromNode(placemark, "description");
                descriptionList.add(description + "\n");
            }
            List<String> evenIndexDescriptions = new ArrayList<>();
            for (int k = 0; k < descriptionList.size(); k++) {
                if (k % 2 == 0) { // 짝수 인덱스 확인
                    evenIndexDescriptions.add(descriptionList.get(k));
                }
            }
            routedescriptionTextView.setText(evenIndexDescriptions.toString());

        }
    }
    private void setPathText(String distance, String time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                routeLayout.setVisibility(View.VISIBLE);
                double km = Double.parseDouble(distance) / 1000;
                double roundedKm = Math.round(km * 10.0) / 10.0;
                routeDistanceTextView.setText("총 거리 : " + roundedKm + " km  ");

                int totalSec = Integer.parseInt(time);
                int day = totalSec / (60 * 60 * 24);
                int hour = (totalSec - day * 60 * 60 * 24) / (60 * 60);
                int minute = (totalSec - day * 60 * 60 * 24 - hour * 3600) / 60;
                String t;
                if (hour > 0) {
                    t = hour + "시간 " + minute + "분";
                } else {
                    t = minute + "분 ";
                }
                routeTimeTextView.setText("예상시간 : 약 " + t);

            }
        });
    }
    private String getContentFromNode(Element item, String tagName) {
        NodeList list = item.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            if (list.item(0).getFirstChild() != null) {
                return list.item(0).getFirstChild().getNodeValue();
            }
        }
        return null;
    }
    private void showCurrentLocationMarker() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                TMapPoint currentLocation = new TMapPoint(latitude, longitude);
                addMarker2(currentLocation, "현재 위치", "현재 위치입니다.");
            } else {
                Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
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
    }
    private void setCurrentLocationAsStartPoint() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            // GPS를 통해 현재 위치를 가져옵니다.
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                // 현재 위치를 출발지로 설정합니다.
                startPoint = new TMapPoint(location.getLatitude(), location.getLongitude());
                autoCompleteEditStart.setText("현재 위치");
                // 출발지 마커를 추가합니다.
                addMarker2(startPoint, "현재 위치", "출발지로 설정됨");
            } else {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private void setCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            // GPS를 통해 현재 위치를 가져옵니다.
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                // 현재 위치를 출발지로 설정합니다.
                nowposition = new TMapPoint(location.getLatitude(), location.getLongitude());
                // 출발지 마커를 추가합니다.
                addMarker2(nowposition, "현재 위치", "현 위치입니다.");
            } else {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private void setNowposition(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            // GPS를 통해 현재 위치를 가져옵니다.
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                // 현재 위치를 출발지로 설정합니다.
                nowposition = new TMapPoint(location.getLatitude(), location.getLongitude());
                // 출발지 마커를 추가합니다.
                tMapView.setCenterPoint(nowposition.getLatitude(), nowposition.getLongitude());
            } else {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private void centerPositon(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            // GPS를 통해 현재 위치를 가져옵니다.
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                // 현재 위치를 출발지로 설정합니다.
                nowposition = new TMapPoint(location.getLatitude(), location.getLongitude());
                // 출발지 마커를 추가합니다.
                tMapView.setCenterPoint(nowposition.getLatitude(), nowposition.getLongitude());
                tMapView.setZoomLevel(19);
            } else {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    }