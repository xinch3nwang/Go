package com.example.go;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class InfoActivity extends AppCompatActivity {
//    private MapView mMapView = null;
    int hour,min,sec;
    double dis;
    TextView tvDate,tvTime,tvDis,tvCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
//        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
//        SDKInitializer.initialize(this);
//        //获取地图控件引用
//        mMapView = (MapView) findViewById(R.id.bmapView);

        Intent intent=getIntent();
        Bundle data = intent.getBundleExtra("data");
        hour = data.getInt("hours");
        min = data.getInt("minutes");
        sec = data.getInt("seconds");
        dis = data.getDouble("distance");
        ImageView imageView = findViewById(R.id.imageView);
//        imageView.setImageBitmap(data.getParcelable("map"));
//        DataBase db = null;
//        StringAndBitmap stringAndBitmap = null;
//        ImageView imageView = findViewById(R.id.imageView);
//        dbhelper db = null;
        String mapStr = data.getString("map");
        imageView.setImageBitmap(StringAndBitmap.stringToBitmap(mapStr));//string为从数据库获取到图片的string形态

        tvDate = findViewById(R.id.textDate);
        tvTime = findViewById(R.id.textTime);
        tvDis = findViewById(R.id.textDis);
        tvCost = findViewById(R.id.textCost);
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
        String date = ft.format(dNow);
        tvDate.setText(Html.fromHtml("    跑步日期<br>    " +
                "<big><big><big>"+date+"</big></big></big>"));
        tvTime.setText(Html.fromHtml("    跑步时长<br>    " +
                "<big><big><big>"+hour+"时"+min+"分"+sec+"秒"+"</big></big></big>"));
        tvDis.setText(Html.fromHtml("    跑步距离<br>    " +
                "<big><big><big>"+dis+"米"+"</big></big></big>"));
        double kcal = 0;
        tvCost.setText(Html.fromHtml("    大概消耗<br>    " +
                "<big><big><big>"+60*dis*1.308+"卡路里"+"</big></big></big>"+
                "    相当于"+(int)(kcal/100)+"勺冰淇淋的热量"));
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
//        mMapView.onResume();
//    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
//        mMapView.onPause();
//    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
//        mMapView.onDestroy();
//    }

    //创建menu
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        //设置menu界面为res/menu/menu.xml
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    //处理菜单事件
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item)
    {
        //得到当前选中的MenuItem的ID,
        int item_id = item.getItemId();

        if (item_id == R.id.record) {
            Intent intent = new Intent();
            intent.setClass(InfoActivity.this, RecordActivity.class);
            startActivity(intent);
            InfoActivity.this.finish();
        }
        else{
            Intent intent = new Intent();
            intent.setClass(InfoActivity.this, MapActivity.class);
            startActivity(intent);
            InfoActivity.this.finish();
        }
        return true;
    }
}