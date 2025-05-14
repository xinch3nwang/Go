package com.example.go;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RecordActivity extends AppCompatActivity {

    TextView tv;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        tv = findViewById(R.id.tv);
        btn = findViewById(R.id.btnclr);

        dbHelper myhelper = new dbHelper(this);
        SQLiteDatabase db = myhelper.getWritableDatabase();

        try {
            String tvShow="";
            Cursor c = db.rawQuery("select * from record",null);
            //如果成功则显示
            if(c.getCount() > 0) {
                c.moveToFirst();
                while (c.moveToNext()){
                    for(int i=0; i < 5; i++){
                        tvShow += c.getString(i);
                        switch (i){
                            case 0:tvShow += "   时长：";break;
                            case 1:tvShow += "时";break;
                            case 2:tvShow += "分";break;
                            case 3:tvShow += "秒   距离：";break;
                            case 4:tvShow += "米";break;
                            default:continue;
                        }
                    }
                    tvShow += "\n";
                }
            }
            tv.setText(tvShow);
        } catch (Exception e) {
            Toast.makeText(RecordActivity.this, e+"无记录", Toast.LENGTH_LONG).show();
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String sql="drop table record";
                    db.execSQL(sql);
                }catch (Exception e) {
                    Toast.makeText(RecordActivity.this, "删除失败", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}