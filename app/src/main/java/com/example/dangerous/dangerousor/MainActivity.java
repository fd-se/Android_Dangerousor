package com.example.dangerous.dangerousor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

public class MainActivity extends AppCompatActivity implements TencentLocationListener {
    private TencentLocation mLocation;
    private TextView textView;
    private TencentLocationManager locationManager;

    @SuppressLint({"ObsoleteSdkInt", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        textView = findViewById(R.id.text_view);
        locationManager = TencentLocationManager.getInstance(this);

        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(permissions, 0);
            }
        }


        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                TencentLocationRequest request = TencentLocationRequest.create();
                int error = locationManager.requestLocationUpdates(request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME).setInterval(500).setAllowDirection(true), MainActivity.this);
                textView.setText(String.format("%d", error));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //可在此继续其他操作。
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        if (error == TencentLocation.ERROR_OK) {
            // 定位成功
            mLocation = location;
            // 更新 status
            textView.setText("(纬度=" + location.getLatitude() + ",经度=" + location.getLongitude() + ",精度=" + location.getAccuracy() + "), 来源=" + location.getProvider() + ", 地址=" + location.getAddress());
        }
    }

    @Override
    public void onStatusUpdate(String name, int status, String desc) {
        // do your work
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        locationManager.removeUpdates(this);
    }
}
