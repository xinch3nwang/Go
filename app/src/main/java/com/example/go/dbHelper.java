package com.example.go;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;


public class dbHelper extends SQLiteOpenHelper {
    public dbHelper(Context context){
        super(context, "record.db", null,1);  //建立或打开库
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="create table record (date String primary key ," +
                "hour int," +
                "min int," +
                "sec int," +
                "dis double," +
                "map String);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS record");
        onCreate(db);
    }

}
