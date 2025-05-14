package com.example.go;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.track.TraceAnimationListener;
import com.baidu.mapapi.map.track.TraceOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.List;

/*
    百度地图应用，包含定位信息和地图显示
    一般需要打开定位服务，选择高精度定位模式，有网络连接
    需要在清单文件里使用百度云服务（参见清单文件service标签）
    需要创建应用（模块）的Key，并写入清单文件（参见清单文件meta标签）
*/
public class GoActivity extends AppCompatActivity {

    LocationClient mLocationClient;  //定位客户端
    MapView mapView;  //Android Widget地图控件
    BaiduMap baiduMap;
    boolean isFirstLocate = true;

    List<LatLng> distancePoints = new ArrayList<LatLng>();//位置点集合---存储所有点数据
    double distance = 0.0;  //存储距离


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果没有定位权限，动态请求用户允许使用该权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            requestLocation();
        }

        Button btn1 = (Button) findViewById(R.id.button);
        Button btn2 = (Button) findViewById(R.id.button2);
        Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer);

        btn1.setEnabled(true);
        btn2.setEnabled(false);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //开始计时
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                //按钮状态
                btn1.setEnabled(false);
                btn2.setEnabled(true);
                //每五秒记录轨迹
//                while (SystemClock.elapsedRealtime()%5000==0){
                recordTrace();
//                }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chronometer.stop();
                //返回跑步时间
                long recordingTime = SystemClock.elapsedRealtime() - chronometer.getBase();
                int hours = 0, minutes = 0, seconds = 0;
                hours = (int) (recordingTime / 3600000);
                recordingTime -= hours * 3600000L;
                minutes = (int) (recordingTime / 60000);
                recordingTime -= minutes * 60000L;
                seconds = (int) recordingTime / 1000;
                Toast.makeText(GoActivity.this, "这次跑了" + hours + "时" + minutes + "分" + seconds + "秒", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(GoActivity.this , InfoActivity.class);
                startActivity(intent);

                //按钮状态
                btn1.setEnabled(true);
                btn2.setEnabled(false);

                baiduMap.snapshot(new BaiduMap.SnapshotReadyCallback() {
                    @Override public void onSnapshotReady(Bitmap bitmap) {
//                        bundle.putParcelable("map",bitmap);
//                        ImageView imageView = findViewById(R.id.imageView);
//                        imageView.setImageBitmap(bitmap);
//                        DataBase db = null;
//                        db.createData(bitmap);
                        StringAndBitmap stringAndBitmap = null;
//                        dbhelper db = null;
//                        db.put(stringAndBitmap.bitmapToString(bitmap));
                    }
                });
            }
        });

    }


    private void recordTrace() {
        LocationManager locationManager;
        String locationProvider = null;
        double longitude = -112;
        double latitude = 37;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location!=null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
        LatLng p1 = new LatLng(longitude,latitude);//起点
        LatLng p2 = new LatLng(longitude,latitude);//中间点
        LatLng p3 = new LatLng(location.getLongitude(), location.getLongitude());//终点

        //计算距离
        distance += DistanceUtil. getDistance(p1, p3);

        //绘制轨迹
        List<LatLng> points = new ArrayList<LatLng>();
        points.add(p1);
        points.add(p2);
        points.add(p3);
        //设置折线的属性
        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(10)
                .color(0xAAFF0000)
                .points(points);
        //在地图上绘制折线
        //mPloyline 折线对象
        Overlay mPolyline = baiduMap.addOverlay(mOverlayOptions);

//        //构建Marker图标
//        BitmapDescriptor bitmap = BitmapDescriptorFactory
//                .fromResource(R.drawable.point);
//        //构建MarkerOption，用于在地图上添加Marker
//        OverlayOptions option = new MarkerOptions()
//                .position(p1)
//                .zIndex(9)  //设置Marker所在层级
//                .icon(bitmap);
//        //在地图上添加Marker，并显示
//        baiduMap.addOverlay(option);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "没有定位权限！", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    requestLocation();
                }
        }
    }


    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {  //初始化

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_go);

        mapView = findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();

        //注册LocationListener监听器
        mLocationClient = new LocationClient(this);
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        // 打开gps
        option.setOpenGps(true);
        //设置扫描时间间隔
        option.setScanSpan(1000);
        //设置定位模式，三选一
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        /*option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);*/
        //设置需要地址信息
        option.setIsNeedAddress(true);
        //保存定位参数
        mLocationClient.setLocOption(option);
        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
    }

    //内部类，百度位置监听器
    private class MyLocationListener  implements BDLocationListener {
//        @Override
//        public void onReceiveLocation(BDLocation location) {
//            //mapView 销毁后不在处理新接收的位置
//            if (location == null || mapView == null){
//                return;
//            }
//            MyLocationData locData = new MyLocationData.Builder()
//                    .accuracy(location.getRadius())
//                    // 此处设置开发者获取到的方向信息，顺时针0-360
//                    .direction(location.getDirection()).latitude(location.getLatitude())
//                    .longitude(location.getLongitude()).build();
//            baiduMap.setMyLocationData(locData);
//        }
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if(bdLocation.getLocType()==BDLocation.TypeGpsLocation || bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo(bdLocation);
            }
        }
    }

    private void navigateTo(BDLocation bdLocation) {
        if(isFirstLocate){
            LatLng ll = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        mapView = null;
        baiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
    }

//    //菜单
//    /*创建menu*/
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        MenuInflater inflater = getMenuInflater();
//        //设置menu界面为res/menu/menu.xml
//        inflater.inflate(R.menu.menu, menu);
//        return true;
//    }
//    /*处理菜单事件*/
//    @SuppressLint("NonConstantResourceId")
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        //得到当前选中的MenuItem的ID,
//        int item_id = item.getItemId();
//
//        if (item_id == R.id.record) {/* 新建一个Intent对象 */
//            Intent intent = new Intent();
//            /* 指定intent要启动的类 */
//            intent.setClass(MainActivity.this, MainActivity2.class);
//            /* 启动一个新的Activity */
//            startActivity(intent);
//            /* 关闭当前的Activity */
//            MainActivity.this.finish();
//        }
//        return true;
//    }
}