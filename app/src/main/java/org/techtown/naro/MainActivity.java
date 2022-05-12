package org.techtown.naro;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;

public class MainActivity extends AppCompatActivity  {


    FragmentHos fragmentHos;
    FragmentMypage fragmentMypage;
    FragmentHome fragmentHome;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    Toolbar toolbar;
    DogDB dogDB;
    FirebaseAuth firebaseAuth;
    String email;
    SQLiteDatabase DB;
    ImageView infopage_img;
    TextView tv_petname, tv_petsex,tv_petage,tv_petbirth,tv_owner;
    String name;
    Button btn_doginfo;
    Button btn_modify;

    TextView textView;
    SQLiteDatabase database;



    private static final int LOCATION_PERMISSION_REQUEST_CODE =1000;
    private static final String[] PERMISSIONS={
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try {
            // 1번 isCheckDB 함수 : DB가 있는지 확인
            boolean bResult = isCheckDB();	// DB가 있는지?
            Log.d("MiniApp", "DB Check="+bResult);
            if(!bResult){	// DB가 없으면 복사
                // 2번 copyDB 함수 : DB를 local에서 device로 복사
                copyDB(this);
            }else{

            }
        } catch (Exception e) {
        }
        //db 조회
        String databaseName = "pethospital.db";
        createDatabase(databaseName);





        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);

        infopage_img = findViewById(R.id.infopage_img);
        tv_petage = findViewById(R.id.tv_petage);
        tv_petbirth=findViewById(R.id.tv_petbirth);
        tv_petname = findViewById(R.id.tv_petname);
        tv_petsex = findViewById(R.id.tv_petsex);
        tv_owner = findViewById(R.id.tv_owner);
        btn_doginfo = findViewById(R.id.btn_doginfo2);
        btn_modify = findViewById(R.id.btn_modify);


        fragmentHos = new FragmentHos();
        fragmentMypage = new FragmentMypage();
        fragmentHome = new FragmentHome();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.containers,fragmentHome).commit();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_menu_navi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());

        btn_doginfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DB = dogDB.getReadableDatabase();
                Cursor c = DB.rawQuery("SELECT petname, petage, petsex, petbirth, petface FROM User_dog WHERE  userid = '" +email+ "'ORDER BY userid",null);

                if(c.getCount() != 0 ){
                    Toast.makeText(MainActivity.this, "이미등록",Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), DogInfo.class);
                    startActivity(intent);
                }
            }
        });

        btn_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DogInfodetail.class);
                startActivity(intent);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser googleuser = firebaseAuth.getCurrentUser();
        email = googleuser.getEmail();
        firebaseAuth = FirebaseAuth.getInstance();
        name = googleuser.getDisplayName();
        dogDB = new DogDB(MainActivity.this,"Dog.db",null,2);
        Dog dog = new Dog();
        settingdoginfo(dog);




    }
    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch(item.getItemId()){
                case R.id.menu_home:
                    transaction.replace(R.id.containers,fragmentHome).commitAllowingStateLoss();
                    break;

                case R.id.menu_hospital:
                    transaction.replace(R.id.containers,fragmentHos).commitAllowingStateLoss();
                    break;


                case R.id.mypage:
                    DB = dogDB.getReadableDatabase();
                    Cursor d = DB.rawQuery("SELECT petname, petage, petsex, petbirth, petface FROM User_dog WHERE  userid = '" +email+ "'ORDER BY userid",null);

                    if(d.getCount() == 0 ){
                        Toast.makeText(MainActivity.this, "강아지정보부터 등록하시오",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        transaction.replace(R.id.containers,fragmentMypage).commitAllowingStateLoss();
                        break;
                    }

            }
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch(menuItem.getItemId()){
            case android.R.id.home:
                Intent back = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(back);
                return true;

            default:
                ((GoogleLogin)GoogleLogin.mContext).signOut();
                Intent in = new Intent(getApplicationContext(),GoogleLogin.class);
                startActivity(in);
                return true;
        }

    }
    public boolean settingdoginfo(Dog dog){

        DB = dogDB.getReadableDatabase();
        Cursor cursor = DB.rawQuery("SELECT petname, petage, petsex, petbirth, petface FROM User_dog WHERE  userid = '" +email+ "'ORDER BY userid",null);
        if(cursor.getCount() == 0){
            Toast.makeText(MainActivity.this,"등록부탁",Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            cursor.moveToFirst();
            dog.setPetname(cursor.getString(0));
            dog.setPetage(cursor.getString(1));
            dog.setPetsex(cursor.getString(2));
            dog.setPetbirth(cursor.getString(3));
            dog.setPetface(cursor.getBlob(4));
            tv_petname.setText(dog.getPetname());
            tv_petage.setText(dog.getPetage() + "살");
            tv_petsex.setText(dog.getPetsex());
            tv_petbirth.setText(dog.getPetbirth().split("-")[0] + "월" + dog.getPetbirth().split("-")[1] + "일");
            tv_owner.setText(name);
            Bitmap bt = BitmapFactory.decodeByteArray(dog.getPetface(), 0, dog.getPetface().length);
            infopage_img.setImageBitmap(bt);
            return true;
        }
    }

    //db 있는지 확인
    public boolean isCheckDB(){
        String filePath = "/data/data/org.techtown.naro/databases/pethospital.db";
        File file = new File(filePath);
        if (file.exists()) {

            return true;
        }
        return false;
    }
    public void copyDB(Context mContext){
        Log.d("MiniApp", "copyDB");
        AssetManager manager = mContext.getAssets();
        String folderPath = "/data/data/org.techtown.naro/databases";
        String filePath = "/data/data/org.techtown.naro/databases/pethospital.db";
        File folder = new File(folderPath);
        File file = new File(filePath);

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {

            InputStream is = manager.open("db/pethospital.db");
            BufferedInputStream bis = new BufferedInputStream(is);

            if (folder.exists()) {

            }else{

                folder.mkdirs();
            }

            if (file.exists()) {

                file.delete();
                file.createNewFile();
            }

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            int read = -1;
            byte[] buffer = new byte[1024];
            while ((read = bis.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer, 0, read);
            }

            bos.flush();

            bos.close();
            fos.close();
            bis.close();
            is.close();

        } catch (IOException e) {
            Log.e("ErrorMessage : ", e.getMessage());
        }
    }

    private void createDatabase(String name) {
        database = openOrCreateDatabase(name, MODE_PRIVATE, null);
    }



}
