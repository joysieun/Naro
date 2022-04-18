package org.techtown.naro;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class HosMap extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    Double latitude;
    Double longitude;
    TextView name;
    TextView tel;
    TextView location;

    LatLng choose;
    String getname;
    Double latchoose;
    Double lonchoose;
    String baseUrl = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query=";
    String seoul = "서울";


    //현재위치 가져오기위한 변수
    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE =2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    };
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hos_map);
        //지도
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.more_map);
        mapFragment.getMapAsync(HosMap.this);
        if(!checkLocationServicesStatus()){
            showDialogForLocationServiceSetting();
        }else{
            checkRunTimePermission();
        }
        //현재위치 가져오기
        gpsTracker = new GpsTracker(this);
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();

        //id매핑
        name = findViewById(R.id.more_name);
        tel = findViewById(R.id.more_tel);
        location = findViewById(R.id.more_address);


        getname = getIntent().getStringExtra("hospitalname");
        String gettel = getIntent().getStringExtra("tel");
        String getaddress = getIntent().getStringExtra("hospitaladdress");
        String getonoff = getIntent().getStringExtra("onoff");
        latchoose = getIntent().getDoubleExtra("latitude",0);
        lonchoose = getIntent().getDoubleExtra("longitude",0);
        Test test = new Test();
        test.execute();
        name.setText(getname);
        tel.setText(gettel);
        location.setText(getaddress);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == PERMISSIONS.length) {
            boolean check_result = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if (check_result) {

            } else {
                //퍼미션 허락하지 않은 경우
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS[0]) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS[1])) {
                    Toast.makeText(this, "퍼미션거부", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "퍼미션거부", Toast.LENGTH_SHORT).show();

                }

            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void checkRunTimePermission(){
        //런타임 퍼미션
        //위치 퍼미션체크
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION);
        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            //허용
        }else{
            if(shouldShowRequestPermissionRationale(PERMISSIONS[0])){
                Toast.makeText(this,"접근권한 필요",Toast.LENGTH_SHORT).show();
                requestPermissions(PERMISSIONS,PERMISSIONS_REQUEST_CODE);

            }
            else{
                requestPermissions(PERMISSIONS,PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    //GPS활성화를 위한 메소드

    private void showDialogForLocationServiceSetting(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다. 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSsetting = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                startActivityForResult(callGPSsetting,GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case GPS_ENABLE_REQUEST_CODE:
                if(checkLocationServicesStatus()){
                    checkRunTimePermission();
                    return;
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    //현재위치랑 동물병원 위치 나타내기
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        LatLng current = new LatLng(latitude,longitude);
        googleMap.addMarker(new MarkerOptions().position(current).title("현재위치"));

        LatLng choose = new LatLng(latchoose,lonchoose);
        googleMap.addMarker(new MarkerOptions().position(choose).title(getname));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(choose,15));


    }
    private class Test extends AsyncTask<Void, Void, Void> {
        TextView onoff;
        ArrayList<String> tt = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                onoff = findViewById(R.id.more_onoff);
                Document doc = Jsoup.connect(baseUrl + seoul + getname).get();
                Elements title = doc.select("._1mAZf");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Element element : title) {
                            tt.add(element.text());
                        }
                        if (tt.size() == 0) {
                            tt.add("0");
                            tt.add("0");
                            tt.add("전화로 문의하세요");
                        }
                        if (tt.size()==2){
                            tt.add("전화로 문의하세요");
                        }
                        onoff.setText(tt.get(2));

                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
