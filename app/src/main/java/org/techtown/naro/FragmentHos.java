package org.techtown.naro;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;


public class FragmentHos extends Fragment{


    public SQLiteDatabase database;
    HospitalDB helper;
    String DBname = "myhospital";
    public Cursor cursor;
    Double latitude;
    Double longitude;
    private HosAdapter adapter;
    RecyclerView recyclerView;
    Location location;
    List<Hospital> dataList;
    TextView noDataText;
    double wantdist;

    //아이템 없을 때
    TextView empty;

    //네이버에서 실시간으로 정보가져오는변수
    String baseUrl = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query=";
    String seoul = "서울";
    final Bundle bundle = new Bundle();
    String searchname;
    String msg;
    String name;
    Handler handler;
    Boolean isEmpty;
    ArrayList<String> ING;





    private MapView mapView = null;

    private double lat, lan;
    String list;
    List<LatLng> latLngs = new ArrayList<>();

    //현재위치 가져오기 위한 변수들
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE =2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    };


    public FragmentHos() {

    }

    public static FragmentHos newInstance() {
        FragmentHos fragmentHos = new FragmentHos();
        return fragmentHos;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_hos, container, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        helper = new HospitalDB(getActivity());
        database = helper.getWritableDatabase();


        recyclerView = rootView.findViewById(R.id.recycleview2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new HosAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),1));
        //외부db copy
        setDB(getActivity());


        if(!checkLocationServicesStatus()){
            showDialogForLocationServiceSetting();
        }else{
            checkRunTimePermission();
        }
        //현재위치 가져오기
        gpsTracker = new GpsTracker(getActivity());
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();

        check(latitude,longitude,1);
        return rootView;
    }

    //permission에 대한 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == PERMISSIONS.length){
            boolean check_result = true;
            for (int result : grantResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    check_result =false;
                    break;
                }
            }
            if(check_result){

            }else{
                //퍼미션 허락하지 않은 경우
                if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),PERMISSIONS[0]) ||
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),PERMISSIONS[1])){
                    Toast.makeText(getActivity().getApplicationContext(),"퍼미션거부",Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(),"퍼미션거부",Toast.LENGTH_SHORT).show();

                }

            }

        }
    }

    void checkRunTimePermission(){
        //런타임 퍼미션
        //위치 퍼미션체크
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION);
        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
        hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            //허용
        }else{
            if(shouldShowRequestPermissionRationale(PERMISSIONS[0])){
                Toast.makeText(getActivity().getApplicationContext(),"접근권한 필요",Toast.LENGTH_SHORT).show();
                requestPermissions(PERMISSIONS,PERMISSIONS_REQUEST_CODE);

            }
            else{
                requestPermissions(PERMISSIONS,PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    //GPS활성화를 위한 메소드

    private void showDialogForLocationServiceSetting(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity().getApplicationContext());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }




    //외부db 사용하기 위한 메소드들
    public static final String ROOT_DIR = "/data/data/org.techtown.naro/databases";

    public static void setDB(Context ctx) {
        File folder = new File(ROOT_DIR);
        if (folder.exists()) {

        } else {
            folder.mkdirs();
        }
        AssetManager assetManager = ctx.getResources().getAssets();
        //db파일 이름
        File outfile = new File(ROOT_DIR + "pethospital.db");
        InputStream is = null;
        FileOutputStream fo = null;
        long filesize = 0;
        try {
            is = assetManager.open("pethospital.db", AssetManager.ACCESS_BUFFER);
            filesize = is.available();
            if (outfile.length() <= 0) {
                byte[] tempdata = new byte[(int) filesize];
                is.read(tempdata);
                is.close();
                outfile.createNewFile();
                fo = new FileOutputStream(outfile);
                fo.write(tempdata);
                fo.close();
            }

        } catch (IOException e) {

        }

    }

    public void copyDB(Context mcontext){
        AssetManager manager = mcontext.getAssets();
        String folderPath = "/data/data/org.techtown.naro/databases";

    }
    public void check(double latitude, double longitude,double distance){
        adapter.removeAllItem();
        Cursor cursor = database.rawQuery("select name, tel, onoff, place1, Latitude,Longitude from myhospital",null);
        cursor.moveToFirst();

        int count=0;
        while(cursor.moveToNext()){
            Double lat = cursor.getDouble(4);
            Double lan = cursor.getDouble(5);
            String tel = cursor.getString(1);
            Hospital hospital = new Hospital();
            wantdist =distance(latitude,longitude,lat,lan,"kilometer");
            if(wantdist <= distance){
                hospital.setHosname(cursor.getString(0));
                hospital.setTel(tel);
                hospital.setHoslatitude(lat);
                hospital.setHoslongitude(lan);
                hospital.setHosaddress(cursor.getString(3));
                hospital.setHosonoff(String.format("%.2f",wantdist)+"km");
                adapter.addItem(hospital);
                count++;
            }

        }
        cursor.close();

    }


    //거리 계산메소드
    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == "kilometer") {
            dist = dist * 1.609344;
        } else if(unit == "meter"){
            dist = dist * 1609.344;
        }

        return (dist);
    }


    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }


    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }






}








