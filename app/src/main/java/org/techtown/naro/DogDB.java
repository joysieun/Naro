package org.techtown.naro;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;

public class DogDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Dogs.db";
    public static final String TABLE_NAME = "User_dog";

    public static final String COLUMN_ID ="_id";
    public static final String COLUMN_USER_ID = "userid";//사용자 아이디
    public static final String COLUMN_PET_NAME = "petname"; //반려동물 이름
    public static final String COLUMN_PET_AGE = "petage"; //반려동물나이
    public static final String COLUMN_PET_SEX = "petsex"; //반려동물성별
    public static final String COLUMN_PET_BIRTH = "petbirth"; //반려동물생일
    public static final String COLUMN_PET_FACE = "petface"; //반려동물 얼굴사진(프로필사진)

    FirebaseAuth firebaseAuth;

    public DogDB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+ TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, userid TEXT, petname TEXT,  petage TEXT, petsex TEXT, petbirth TEXT, petface BLOB );");




    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean insertdogdata(String userid, String petname, String petage, String petsex, String petbirth ,byte[] petface){
        SQLiteDatabase db2 = getWritableDatabase();
        ContentValues cv2 = new ContentValues();
        cv2.put(COLUMN_USER_ID, userid);
        cv2.put(COLUMN_PET_NAME, petname);
        cv2.put(COLUMN_PET_AGE, petage);
        cv2.put(COLUMN_PET_SEX, petsex);
        cv2.put(COLUMN_PET_BIRTH, petbirth);
        cv2.put(COLUMN_PET_FACE,petface);

        long result = db2.insert(TABLE_NAME,null,cv2);

        if(result == -1){
            return false;

        }
        else{
            return true;

        }

    }
    public void deletedata(String userid ){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM User_dog WHERE userid='" + userid + "'; ");
    }

    public void updateData(String owner, String name, String age, String sex, String birth, byte[] image){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_PET_NAME,name);
        cv.put(COLUMN_PET_AGE,age);
        cv.put(COLUMN_PET_SEX,sex);
        cv.put(COLUMN_PET_BIRTH,birth);
        cv.put(COLUMN_PET_FACE,image);
        long result = sqLiteDatabase.update(TABLE_NAME,cv, "userid=?",new String[]{owner});

    }
}
