package org.techtown.naro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class DogInfo extends AppCompatActivity {

    private final int GET_GALLERY_IMAGE =200;
    private final int CROP_PICTURE=2;
    String TAG= "SE";
    Uri photoURI;
    EditText dog_name;
    EditText dog_sex;
    EditText dog_age;
    EditText dog_birth;
    ImageView pet_face;
    Button btn_savedoginfo;
    FirebaseAuth firebaseAuth;
    String dname;
    String dage;
    String dsex;
    byte[] data;
    String dbirth;
    ImageView dface;

    String email;
    DogDB dogDB;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dog_info);

        dog_name = (EditText) findViewById(R.id.dog_name);
        dog_sex = (EditText) findViewById(R.id.dog_sex);
        dog_age = (EditText) findViewById(R.id.dog_age);
        dog_birth = (EditText) findViewById(R.id.dog_birth);
        btn_savedoginfo = (Button) findViewById(R.id.btn_savedoginfo);
        pet_face = findViewById(R.id.pet_face);

        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser googleuser = firebaseAuth.getCurrentUser();
        email = googleuser.getEmail();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "권한설정완료");

            } else {
                Log.d(TAG, "권한설정 요청");
                ActivityCompat.requestPermissions(DogInfo.this, new String[]
                        {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            }
        }

        pet_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });

        btn_savedoginfo.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                data = imageViewToByte(pet_face);
                dname = dog_name.getText().toString();
                dsex = dog_sex.getText().toString();
                dage = dog_age.getText().toString();
                dbirth = dog_birth.getText().toString();

                if(dname.length() == 0 || dsex.length() == 0 || dage.length() == 0 || dbirth.length() == 0){
                    Toast.makeText(DogInfo.this, "작성을 완성해주세요",Toast.LENGTH_SHORT).show();
                }
                else{
                    dogDB = new DogDB(getApplicationContext(), "Dog.db", null, 2);
                    dogDB.insertdogdata(email, dname, dage, dsex, dbirth,data);
                    Intent i = new Intent(DogInfo.this, MainActivity.class);
                    startActivity(i);
                }


            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            switch (requestCode){
                case GET_GALLERY_IMAGE: {
                    if (requestCode == GET_GALLERY_IMAGE && data != null && data.getData() != null) {
                        photoURI = data.getData();
                        cropImage();

                    }
                }
                case CROP_PICTURE:{
                    Bitmap bitmap;
                    try{
                        photoURI = data.getData();
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                            ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),photoURI);
                            bitmap = ImageDecoder.decodeBitmap(source);
                            pet_face.setImageBitmap(bitmap);
                            Toast.makeText(this, "이미지추가 성공", Toast.LENGTH_SHORT).show();

                        }
                        else {

                            bitmap = MediaStore.Images.Media.getBitmap(DogInfo.this.getContentResolver(), photoURI);
                            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(bitmap, 128, 128);
                            ByteArrayOutputStream bs = new ByteArrayOutputStream();
                            thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, bs);
                            pet_face.setImageBitmap(thumbImage);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG,"ONREQUESTPERMISSIONRESULT");
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "permission");}
        else{
            //권한요청 동의하지 않을 경우
            showNoPermission();
        }
    }
    //동의하지 않을때 함수
    private void showNoPermission(){
        Toast.makeText(this, "권한 요청에 동의 후 사용가능 합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();

    }
    public static byte[] imageViewToByte(ImageView image){

        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public void cropImage(){

        this.grantUriPermission("com.android.camera",photoURI,Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //원본
        Intent i = new Intent("com.android.camera.action.CROP");
        i.setDataAndType(photoURI,"image/*");  //파일 연결
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(i,0);
        grantUriPermission(list.get(0).activityInfo.packageName,photoURI,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        int size = list.size();
        if(size == 0){
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }else {
            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();
            i.putExtra("outputX", 200);
            i.putExtra("outputY", 200);
            i.putExtra("aspectX", 1);
            i.putExtra("aspectY", 1);
            i.putExtra("crop", true);
            i.putExtra("scale",true);
            startActivityForResult(i, CROP_PICTURE);
        }
    }
}
