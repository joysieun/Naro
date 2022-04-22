package org.techtown.naro;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FragmentHome extends Fragment {
    private View view;
    private Button button;
    private Button btn_using;
    TextView textView;
    DogDB dogDB;
    SQLiteDatabase DB;
    FirebaseAuth firebaseAuth;


    //test 해보기
    String searchname;
    String baseUrl = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query=";
    String seoul = "서울";
    double latitude=37.5908;
    double longitude=127.0624;
    SQLiteDatabase database;
    public FragmentHome(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home,container,false);
        button = (Button) view.findViewById(R.id.btn_cam);
        btn_using = (Button) view.findViewById(R.id.btn_using);

        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser googleuser = firebaseAuth.getCurrentUser();
        String email = googleuser.getEmail();
        dogDB = new DogDB(getActivity().getApplicationContext(),"Dog.db",null,2);
        Dog dog = new Dog();

        setDB(getActivity());


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DB = dogDB.getReadableDatabase();
                Cursor d = DB.rawQuery("SELECT petname, petage, petsex, petbirth, petface FROM User_dog WHERE  userid = '" +email+ "'ORDER BY userid",null);

                if(d.getCount() == 0 ){
                    Toast.makeText(getActivity(), "강아지정보부터 등록하시오",Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(getActivity(),Camera.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }

            }
        });
        return view;

    }

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
