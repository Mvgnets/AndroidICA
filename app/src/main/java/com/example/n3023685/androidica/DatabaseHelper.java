package com.example.n3023685.androidica;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "placesHistory.db";
    public static final String TABLE_NAME = "placesTable";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "rowNum";
    public static final String COL_3 = "placeName";
    public static final String COL_4 = "latitude";
    public static final String COL_5 = "longitude";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (" + COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_2 + " INTEGER, " + COL_3 + " TEXT, " + COL_4 + " TEXT, " + COL_5 + " TEXT) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(int rowNum, String placeName, String latitude, String longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, rowNum);
        contentValues.put(COL_3, placeName);
        contentValues.put(COL_4, latitude);
        contentValues.put(COL_5, longitude);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery(" select * from " + TABLE_NAME, null);
        return res;
    }

    public Cursor getRow(int i) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery(" select * from " + TABLE_NAME + " where " + COL_1 + " = " + i, null);
        return res;
    }




}
