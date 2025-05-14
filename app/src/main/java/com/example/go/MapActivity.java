package com.example.go;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MapActivity extends AppCompatActivity {

    public BaiduMap mBaiduMap;
    LocationClient mLocationClient;  //定位客户端
    public MapView mMapView;  //Android Widget地图控件
    boolean isRunStart = false;
    boolean isFirstLoc = true;
    double lng,lat,prelng,prelat;

    int hours = 0, minutes = 0, seconds = 0;
    double distance = 0.0;
    String mapstr;

    SQLiteDatabase db;
    Date dNow = new Date( );
    SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
    String date = ft.format(dNow);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        Button btn1 = (Button) findViewById(R.id.button);
        Button btn2 = (Button) findViewById(R.id.button2);
        Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer);

        initMap();

        btn1.setEnabled(true);
        btn2.setEnabled(false);

        dbHelper myhelper = new dbHelper(this);
        db=myhelper.getWritableDatabase();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //开始计时
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                //按钮状态
                btn1.setEnabled(false);
                btn2.setEnabled(true);
                //显示轨迹
                mBaiduMap.clear();
                isRunStart = true;
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chronometer.stop();
                //返回跑步时间
                long recordingTime = SystemClock.elapsedRealtime() - chronometer.getBase();
                hours = (int) (recordingTime / 3600000);
                recordingTime -= hours * 3600000L;
                minutes = (int) (recordingTime / 60000);
                recordingTime -= minutes * 60000L;
                seconds = (int) recordingTime / 1000;

                //调出数据界面
                Intent intent = new Intent(MapActivity.this , InfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("hours",hours);
                bundle.putInt("minutes",minutes);
                bundle.putInt("seconds",seconds);
                bundle.putDouble("distance",distance);

                mBaiduMap.snapshot(new BaiduMap.SnapshotReadyCallback() {
                    @Override public void onSnapshotReady(Bitmap bitmap) {
                        mapstr = StringAndBitmap.bitmapToString(bitmap);
                        //传递轨迹
                        bundle.putString("map",mapstr);
                        try{
//                            myhelper.onCreate(db);
                            //添加进数据库
                            String sql="insert into record(date,hour,min,sec,dis,map) " +
                                    "values('"+date+"',"+hours+","+minutes+","+seconds+","+distance+",'"+mapstr+"');";
                            db.execSQL(sql);
                            //给用户提示添加成功
                            //Toast.makeText(MapActivity.this,"记录已保存",Toast.LENGTH_LONG).show();
                        } catch (Exception e){
                            //插入失败
                            //Toast.makeText(MapActivity.this,mapstr,Toast.LENGTH_LONG).show();
                        }
                    }
                });
                intent.putExtra("data",bundle);
                startActivity(intent);

                //按钮状态
                btn1.setEnabled(true);
                btn2.setEnabled(false);
            }
        });

    }


    //绘制轨迹
    private void drawTrace() {
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_tracing);
//        while (true){
            //定义Maker坐标点
            LatLng point = new LatLng(lat, lng);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap);
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);
//            //每5秒
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                break;
//            }
//        }
    }


    private void initMap() {
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //设置定位图标的显示方式
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
        //定位初始化
        mLocationClient = new LocationClient(this);
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);// 设置发起定位请求的间隔
        //设置locationClientOption
        mLocationClient.setLocOption(option);
        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

            //轨迹
            if(isRunStart)
                drawTrace();

            //测距
            if(isFirstLoc){
                prelng = lng = locData.longitude;
                prelat = lat = locData.latitude;
                isFirstLoc = false;
            }
            else{
                prelng = lng;
                prelat = lat;
                lng = locData.longitude;
                lat = locData.latitude;
            }
            LatLng p1 = new LatLng(prelng,prelat);//起点
            LatLng p2 = new LatLng(lng,lat);//中间点
            distance += DistanceUtil.getDistance(p1,p2);
        }
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

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
            intent.setClass(MapActivity.this, RecordActivity.class);
            startActivity(intent);
            MapActivity.this.finish();
        }
        else{
            Intent intent = new Intent();
            intent.setClass(MapActivity.this, WeatherTipsActivity.class);
            startActivity(intent);
            MapActivity.this.finish();
        }
        return true;
    }
    class dbHelper extends SQLiteOpenHelper {
        public dbHelper(Context context) {
            super(context, "record.db", null, 1);  //建立或打开库
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "create table record (date String primary key ," +
                    "hour int," +
                    "min int," +
                    "sec int," +
                    "dis double," +
                    "map String);";
            db.execSQL(sql);
            Toast.makeText(MapActivity.this, "ok", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS record");
            onCreate(db);
        }
    }
}


