package com.example.go;

import static android.text.Html.fromHtml;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.qweather.plugin.view.HeContent;
import com.qweather.plugin.view.HorizonView;
import com.qweather.plugin.view.QWeatherConfig;
import com.qweather.sdk.bean.air.AirNowBean;
import com.qweather.sdk.bean.base.Code;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.HeConfig;
import com.qweather.sdk.view.QWeather;

import java.util.List;

public class WeatherTipsActivity extends AppCompatActivity {

    //定位
    private LocationManager locationManager;
    private String locationProvider = null;
    private double longitude;
    private double latitude;
    private String locationNow;
    //天气
    private static final String TAG = "";

    WeatherNowBean.NowBaseBean now;
    AirNowBean.NowBean airnow;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weathertips);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        //获取经纬度
        getLocation();
        //获取天气信息 now和airnow
        showWeather();

        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(WeatherTipsActivity.this , MapActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showWeather() {
        HeConfig.init("HE2111121434421600","1f0c90eb98994f0f9cbf44e45c0cf983");
        HeConfig.switchToDevService();
        Log.i(TAG, "getWeathering ");
        //一般天气
        QWeather.getWeatherNow(WeatherTipsActivity.this, locationNow, new QWeather.OnResultWeatherNowListener() {

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "getWeather onError: " + e);
            }

            @Override
            public void onSuccess(WeatherNowBean weatherBean) {
                Log.i(TAG, "getWeather onSuccess: " + new Gson().toJson(weatherBean));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK == weatherBean.getCode()) {
                    now = weatherBean.getNow();
                    layoutSet();
                }
                else {
                    //在此查看返回数据失败的原因
                    Code code = weatherBean.getCode();
                    Log.i(TAG, "failed code: " + code);
                }
            }
        });
        //空气质量
        QWeather.getAirNow(WeatherTipsActivity.this, "CN101190501"/*locationNow*/, Lang.ZH_HANS, new QWeather.OnResultAirNowListener() {

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "getAir onError: " + e);
            }

            @Override
            public void onSuccess(AirNowBean airNowBean) {
                Log.i(TAG, "getWeather onSuccess: " + new Gson().toJson(airNowBean));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK == airNowBean.getCode()) {
                    airnow = airNowBean.getNow();
                }
                else {
                    //在此查看返回数据失败的原因
                    Code code = airNowBean.getCode();
                    Log.i(TAG, "failed code: " + code);
                }
            }
        });

    }

    private void layoutSet() {
        TextView feelsLike = findViewById(R.id.textFeel);
        feelsLike.setText(Html.fromHtml("体感温度<br>" +
                "<big><big><big>"+now.getFeelsLike()+"</big></big></big>度"));
        TextView text = findViewById(R.id.textText);
        text.setText(Html.fromHtml("天气<br>" +
                "<big><big>"+now.getText()+"</big></big>"));
        TextView windScale = findViewById(R.id.textScale);
        windScale.setText(Html.fromHtml("风力等级<br>" +
                "<big><big><big>"+now.getWindScale()+"</big></big></big>级"));
        TextView windSpeed = findViewById(R.id.textSpeed);
        windSpeed.setText(Html.fromHtml("风速<br>" +
                "<big><big><big>"+now.getWindSpeed()+"</big></big></big>m/s"));
        TextView humidity = findViewById(R.id.textHumidity);
        humidity.setText(Html.fromHtml("湿度<br>" +
                "<big><big><big>"+now.getHumidity()+"</big></big></big>%"));
        TextView pressure = findViewById(R.id.textPressure);
        pressure.setText(Html.fromHtml("气压<br>" +
                "<big><big><big>"+now.getPressure()+"</big></big></big>百帕"));
        TextView aqi = findViewById(R.id.textAqi);
        aqi.setText(Html.fromHtml("空气指数<br>" +
                "<big><big><big>"+airnow.getAqi()+"</big></big></big>"));
        TextView category = findViewById(R.id.textCategory);
        category.setText(Html.fromHtml("空气质量<br>" +
                "<big><big>"+airnow.getCategory()+"</big></big>"));


        TextView tips1 = findViewById(R.id.textTips1);
        if(Integer.parseInt(now.getFeelsLike())<=13){
            tips1.setText("天气不暖 小心着凉  ");
        }
        else if(Integer.parseInt(now.getFeelsLike())<=26){
            tips1.setText("温度适宜 坚持运动  ");
        }
        else{
            tips1.setText("天气很热 注意补水  ");
        }

        TextView tips2 = findViewById(R.id.textTips2);
        if(Integer.parseInt(now.getWindScale())>3){
            tips2.setText("风不小哦  ");
        }
        else if(Integer.parseInt(now.getWindScale())==0){
            tips2.setText("风平浪静  ");
        }
        else{
            tips2.setText("微风拂面  ");
        }

        TextView tips3 = findViewById(R.id.textTips3);
        if(Integer.parseInt(now.getHumidity())<40||Integer.parseInt(now.getHumidity())>60){
            tips3.setText("完美情况:湿度50％～60％,气压>1000百帕 ");
        }
        else{
            tips3.setText("湿度适宜 气压最好1000百帕以上 ");
        }

        TextView tips4 = findViewById(R.id.textTips4);
        if(Integer.parseInt(airnow.getAqi())>99){
            tips4.setText("空气很差 千万别跑  ");
        }
        else if(Integer.parseInt(airnow.getAqi())<50){
            tips4.setText("空气很好 适合跑步  ");
        }
        else{
            tips4.setText("空气一般 可以跑步  ");
        }

        QWeatherConfig.init("8a124eca34f64b69a7db5f2034f500d0","CN101190501"/*,"location"*/);
        //横向布局
        HorizonView horizonView = findViewById(R.id.horizon_view);
        //取消默认背景
        horizonView.setDefaultBack(false);
        //设置布局的背景圆角角度，颜色，边框宽度，边框颜色
        horizonView.setStroke(1, Color.parseColor("#4D3F51B5"),1,Color.parseColor("#4D3F51B5"));
        //添加地址文字描述，第一个参数为文字大小，单位：sp ，第二个参数为文字颜色，默认白色
        horizonView.addLocation(14, Color.WHITE);
        //添加预警图标，参数为图标大小，单位：dp
        horizonView.addAlarmIcon(14);
        //添加预警文字
        horizonView.addAlarmTxt(14);
        //添加温度描述
        horizonView.addTemp(14, Color.WHITE);
        //添加天气图标
        horizonView.addWeatherIcon(14);
        //添加天气描述
        horizonView.addCond(14, Color.WHITE);
        //添加风向图标
        horizonView.addWindIcon(14);
        //添加风力描述
        horizonView.addWind(14, Color.WHITE);
        //添加文字：AQI
        horizonView.addAqiText(14, Color.WHITE);
        //添加空气质量描述
        horizonView.addAqiQlty(14);
        //添加空气质量数值描述
        horizonView.addAqiNum(14);
        //添加降雨图标
        horizonView.addRainIcon(14);
        //添加降雨描述
        horizonView.addRainDetail(14, Color.WHITE);
        //设置控件的对齐方式，默认居中
        horizonView.setViewGravity(HeContent.GRAVITY_CENTER);
        //设置控件的内边距，默认为0
        horizonView.setViewPadding(10,10,10,10);
        //显示控件
        horizonView.show();
    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
            Log.v("TAG", "定位方式GPS");
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
            Log.v("TAG", "定位方式Network");
        }else {
            Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
            return;
        }

        //获取上次的位置，一般第一次运行，此值为null
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location!=null){
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            locationNow = String.format("%.2f,%.2f", longitude, latitude);
//            Toast.makeText(this, locationNow,Toast.LENGTH_SHORT).show();
            Log.v("TAG", "获取上次的位置-经纬度："+longitude + "," + latitude);
        }else{
            //监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
            locationManager.requestLocationUpdates(locationProvider, 3000, 1,locationListener);
        }
    }

    public LocationListener locationListener = new LocationListener() {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {
        }
        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
        }
        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                //如果位置发生变化，重新显示地理位置经纬度
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                locationNow = String.format("%.2f,%.2f", longitude, latitude);
//                Toast.makeText(WeatherTipsActivity.this, location.getLongitude() + " " +
//                        location.getLatitude() + "", Toast.LENGTH_SHORT).show();
                Log.v("TAG", "监视地理位置变化-经纬度："+location.getLongitude()+"   "+location.getLatitude());
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if(grantResults.length==0){
                    return;
                }
                else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {  //危险权限
                    getLocation();
                } else {
                    Toast.makeText(this, "没有授予定位权限！", Toast.LENGTH_LONG).show();
                    finish();
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}