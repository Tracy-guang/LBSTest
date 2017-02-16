package com.yueguang.lbstest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LocationClient locationClient;
    private TextView tvPosition;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocat=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationClient=new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MylocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        tvPosition= (TextView) findViewById(R.id.tvPosition);
        List<String> permissionList=new ArrayList<>();
        takePermission(permissionList);

        mapView= (MapView) findViewById(R.id.bmap);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
    }

    private void takePermission(List<String> permissionList) {

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            requestLocation();
        }
    }

    private void requestLocation() {
        initLocation();
        locationClient.start();
    }

    private void initLocation() {

        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(5000);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case 1:

                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                }else{

                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    public class MylocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

//            StringBuilder currentPosition=new StringBuilder();
//            currentPosition.append("纬度").append(bdLocation.getLatitude()).append("\n");
//            currentPosition.append("经度").append(bdLocation.getLongitude()).append("\n");
//            currentPosition.append("国家").append(bdLocation.getCountry());
//            currentPosition.append("省：").append(bdLocation.getProvince());
//            currentPosition.append("市：").append(bdLocation.getCity());
//            currentPosition.append("区：").append(bdLocation.getDistrict());
//            currentPosition.append("街道：").append(bdLocation.getStreet());
//            currentPosition.append("定位方式：");
//            if(bdLocation.getLocType()==BDLocation.TypeGpsLocation){
//                currentPosition.append("GPS");
//            }else if(bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
//                currentPosition.append("网络");
//            }else{
//                int locationType = bdLocation.getLocType();
//                currentPosition.append(locationType+"");
//            }
//            tvPosition.setText(currentPosition);

            if(bdLocation.getLocType()==BDLocation.TypeGpsLocation || bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo(bdLocation);
            }
        }
    }


    private void navigateTo(BDLocation bdLocation){

        if(isFirstLocat){
            double lat = bdLocation.getLatitude();
            double longi = bdLocation.getLongitude();
            LatLng latLng=new LatLng(lat,longi);
            MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(latLng);
            baiduMap.animateMapStatus(update);
            update= MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocat=false;
        }

        MyLocationData.Builder builder=new MyLocationData.Builder();
        builder.latitude(bdLocation.getLatitude());
        builder.longitude(bdLocation.getLongitude());
        MyLocationData locationData = builder.build();
        baiduMap.setMyLocationData(locationData);
    }




    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
}
