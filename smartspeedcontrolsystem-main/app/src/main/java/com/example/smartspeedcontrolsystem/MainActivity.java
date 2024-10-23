package com.example.smartspeedcontrolsystem;

import java.util.Iterator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;
import android.os.Handler; // Handler 클래스 import 추가

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.example.smartspeedcontrolsystem.LocationUtils;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private GoogleMap mMap;
    private Circle redCircle;
    private Circle yellowCircle;
    private double latitude2;
    private double longitude2;
    private List<Circle> drawnCircles = new ArrayList<>();
    private static final String TAG = "MainActivity";
    private LocationManager locationManager;
    private double currentLatitude, currentLongitude;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private List<CircleOptions> circles = new ArrayList<>();
    private double myRad = 35.169472;
    private double mylong = 128.995720;
    private List<LatLng> drawnCircleCenters = new ArrayList<>();
    private DatabaseReference mDatabase;
    private List<Marker> markers = new ArrayList<>();
    private DatabaseReference myRefLatitude;
    private DatabaseReference myRefLongitude;
    private Handler handler;
    private Runnable runnable;
    private static final long INTERVAL = 3000;
    public static String carlongitude ="128.995720";
    public static String carlatitude ="35.169472";
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //레이아웃 설정
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map); //레이아웃에 있는 사용할 id 값 받아오기
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // 위치 권한 확인 및 요청
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        handler = new Handler();
        checkLocationPermission();
        readCsvFile();
        mapFragment.getMapAsync(this);
    }


    //Fire Base RDS 업데이트
    private void writeData(String id, String data) {
        // "id"라는 키를 사용하여 데이터 쓰기
        mDatabase.child("").child(id).setValue(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("MainActivity", "Data write successful!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MainActivity", "Data write failed!", e);
                    }
                });
    }


    private void fetchData() {
        DatabaseReference conditionRef3 = mRootRef.child("latitude");
        DatabaseReference conditionRef4 = mRootRef.child("longitude");

        conditionRef3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double carLatitudeDouble = dataSnapshot.getValue(Double.class);
                if (carLatitudeDouble != null) {
                    carlatitude = carLatitudeDouble.toString();
                    Log.d("datachange" ,"kof"+carlatitude);
                    updateMap();
                } else {
                    // Handle the case where carLatitudeDouble is null
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        conditionRef4.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double carLongitudeDouble = dataSnapshot.getValue(Double.class);
                if (carLongitudeDouble != null) {
                    carlongitude = carLongitudeDouble.toString();
                    Log.d("datachange" ,"kof"+carlongitude);
                    updateMap();
                } else {
                    // Handle the case where carLongitudeDouble is null
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }
    private void updateMap() {
        if (carlatitude != null && carlongitude != null) {
            double latitude = Double.parseDouble(carlatitude);
            double longitude = Double.parseDouble(carlongitude);
            LatLng carLocation = new LatLng(latitude, longitude);

            // Clear only markers
            for (Marker marker : markers) {
                marker.remove();
            }
            markers.clear();

            // Create a Bitmap from the drawable
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.imagecar);
            Bitmap bitmap = bitmapDrawable.getBitmap();

            // Resize the Bitmap
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);

            // Create a BitmapDescriptor from the resized Bitmap
            BitmapDescriptor customIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

            // Add a marker with the custom icon
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(carLocation)
                    .title("Car Location")
                    .icon(customIcon));

            // Add the marker to the list
            markers.add(marker);

            // Move the camera to the marker
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 16));
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // 액티비티 종료 시 핸들러 콜백 제거
    }

//    private void fetchData() {
//        myRefLatitude.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                carlatitude = dataSnapshot.getValue(String.class);
//                Log.d(TAG, "Latitude is: " + carlatitude);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.w(TAG, "Failed to read latitude value.", error.toException());
//            }
//        });
//
//        myRefLongitude.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                carlongitude = dataSnapshot.getValue(String.class);
//                Log.d(TAG, "Longitude is: " + carlongitude);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.w(TAG, "Failed to read longitude value.", error.toException());
//            }
//        });
//    }

    // 위치 정보를 갱신하는 메서드
//    private void updateLocation() {
//        Intent intent = getIntent();
//
//
//        // 재귀 호출을 사용하여 위치 정보를 주기적으로 갱신
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                updateLocation(); // 위치 정보 갱신 함수 호출
//            }
//        }, 3000); // 3초마다 위치 정보를 갱신하도록 설정 (원하는 시간으로 변경 가능)
//    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    /*
     * onRequestPermissionsResult 현재 위치를 gps 를 가져오는코드  에뮬레이터 환경상 구현이 안됨
     * rccar gps 모듈 이나 휴대폰으로 연동후 구현
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허용 시 위치 정보 가져오기
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception: " + e.getMessage());
        }
    }

    private void readCsvFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = getResources().openRawResource(R.raw.output_lat_long); // CSV 파일 리소스 가져오기
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                try {
                    // 첫 번째 행(헤더)을 읽어서 버림
                    reader.readLine();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] data = line.split(","); // CSV 행을 쉼표(,)로 분리하여 데이터 추출
                        if (data.length >= 2) { // 최소한 주소 정보가 있어야 함 csv파일 참고
                            String latitudeStr = data[0];
                            String longitudeStr = data[1];
                            double latitude = Double.parseDouble(latitudeStr);
                            double longitude = Double.parseDouble(longitudeStr);
                            LatLng location = new LatLng(latitude, longitude);
                            if (location != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        calculateDistance(location.latitude, location.longitude);
                                    }
                                });
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading CSV file: " + e.getMessage());
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing InputStream: " + e.getMessage());
                    }
                }
                try {
                    Thread.sleep(3000); // 한차례 호출이 끝나면 3초 딜레이
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                fetchData();
                readCsvFile();
            }
        }).start();
    }


    private ArrayList<LatLng> circleCoordinates = new ArrayList<>(); // 좌표를 저장할 배열 추가

    private void addCircle(CircleOptions circleOptions) {
        if (mMap != null) {
            Circle circle = mMap.addCircle(circleOptions);
            Log.d(TAG, "Circle added at: ");

            if (circle != null) {
                LatLng center = circleOptions.getCenter();
                circleCoordinates.add(center); // 좌표 추가
            }
        }
    }

    private void removeCircle(Circle circle) {
        if (circle != null) {
            LatLng center = circle.getCenter();
            circle.remove();
            circleCoordinates.remove(center); // 좌표 배열에서 삭제
            Log.d(TAG, "Circle removed at: " + center.latitude + ", " + center.longitude);
        }
    }

    private void calculateDistance(double latitude, double longitude) {
        double EARTH_R = 6371000.0;
        double Rad = Math.PI / 180;
        double radLat1 = Rad * Double.parseDouble(carlatitude);
        double radLat2 = Rad * latitude;
        double radDist = Rad * (Double.parseDouble(carlongitude) - longitude);
        double distance = Math.sin(radLat1) * Math.sin(radLat2);
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
        double ret = EARTH_R * Math.acos(distance);
        double resultInMeters = Math.round(ret);

        Log.d("Distance", "Distance: " + resultInMeters + " meters" + carlatitude +" ||" + carlongitude);

        LatLng location = new LatLng(latitude, longitude);
        if (resultInMeters <= 200){
        writeData("ANDVAL","OK");
        }
        if (resultInMeters <= 1500) {
            // 반경 내에 있는 좌표를 저장할 배열 초기화
            ArrayList<LatLng> coordinatesToRemove = new ArrayList<>();

            // 100m 반경의 빨간 투명 반원
            CircleOptions redCircleOptions = new CircleOptions()
                    .center(location)
                    .radius(100) // 반경 설정
                    .fillColor(Color.argb(100, 255, 0, 0))
                    .strokeColor(Color.argb(100, 255, 0, 0));
            if (!isCircleDrawn(redCircleOptions)) {
                Circle redCircle = mMap.addCircle(redCircleOptions);
                Log.d(TAG, "Circle added at: ");
                drawnCircles.add(redCircle);
                coordinatesToRemove.add(location); // 저장된 좌표 추가
            }

            // 200m 반경의 노란색 반원
            CircleOptions yellowCircleOptions = new CircleOptions()
                    .center(location)
                    .radius(200) // 반경 설정
                    .fillColor(Color.argb(100, 255, 255, 0))
                    .strokeColor(Color.argb(100, 255, 255, 0));
            if (!isCircleDrawn(yellowCircleOptions)) {
                Circle yellowCircle = mMap.addCircle(yellowCircleOptions);
                Log.d(TAG, "Circle added at: ");
                drawnCircles.add(yellowCircle);
                coordinatesToRemove.add(location); // 저장된 좌표 추가
            }

            // 기존의 반원경 제거 로직 수정
            for (Iterator<Circle> iterator = drawnCircles.iterator(); iterator.hasNext(); ) {
                Circle circle = iterator.next();
                LatLng center = circle.getCenter();
                if (!isCircleInsideDistance(center, resultInMeters)) {
                    removeCircle(circle);
                    iterator.remove();
                    coordinatesToRemove.remove(center); // 저장된 좌표 제거
                }
            }
        }
    }

    // 반원경이 주어진 거리 내에 있는지 확인하는 보조 메서드 수정
    private boolean isCircleInsideDistance(LatLng center, double distance) {
        // 좌표 간의 거리 계산
        Location locationA = new Location("point A");
        locationA.setLatitude(Double.parseDouble(carlatitude));
        locationA.setLongitude(Double.parseDouble(carlongitude));
        Location locationB = new Location("point B");
        locationB.setLatitude(center.latitude);
        locationB.setLongitude(center.longitude);
        float distanceBetween = locationA.distanceTo(locationB);
        return distanceBetween <= 1500;
    }

    private boolean isCircleDrawn(CircleOptions circleOptions) {
        for (Circle circle : drawnCircles) {
            if (circle.getCenter().equals(circleOptions.getCenter()) && circle.getRadius() == circleOptions.getRadius()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng currentLatLng = new LatLng(latitude, longitude);
        // 위치 정보를 Firebase에 업데이트
        Log.e(TAG, "logview logview:2 " + currentLatLng +"logview");
        latitude2 =latitude;
        longitude2=longitude;
        // 현재 위치로 지도를 부드럽게 이동시킴
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
    }


    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        double lat = Double.parseDouble(carlatitude);
        double lng = Double.parseDouble(carlongitude);
        LatLng initialLatLng = new LatLng(lat, lng); //여기에 유저 gps 정보를 넣으면됨
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 13)); // 지정한 좌표로 이동 및 줌 설정


    }
}
