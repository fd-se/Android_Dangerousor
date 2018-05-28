package com.example.dangerous.dangerousor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TencentLocationListener {
    private UserLogoutTask mAuthTask = null;

    private TencentLocation mLocation;
    private TextView textView;
    private TencentLocationManager locationManager;
    private TextView accountNickname;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String nickName;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        accountNickname = headerView.findViewById(R.id.account_nickname);
        Button buttonLocate = findViewById(R.id.buttonLocate);
        textView = findViewById(R.id.text_view);
        locationManager = TencentLocationManager.getInstance(this);
        nickName = sharedPreferences.getString("account", "");
        token = InstanceID.getInstance(this).getId();
        accountNickname.setText(nickName);
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


        buttonLocate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                TencentLocationRequest request = TencentLocationRequest.create();
                int error = locationManager.requestLocationUpdates(request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME).setInterval(500).setAllowDirection(true), MainActivity.this);
                textView.setText(String.format("%d", error));
            }
        });


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

//        } else if (id == R.id.nav_share) {
//
        } else if (id == R.id.nav_logout) {
            mAuthTask = new UserLogoutTask(token);
            mAuthTask.execute((Void) null);

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    public class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {

        private final String token;
        private CheckLogout checkLogout;

        class CheckLogout {
            private String content;
            private boolean success;


            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public boolean isSuccess() {
                return success;
            }

            public void setSuccess(boolean success) {
                this.success = success;
            }
        }

        UserLogoutTask(String token) {
            this.token = token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            StringBuilder response=null;
            try {
                // Simulate network access.
//                Thread.sleep(2000);
                URL url = new URL("http://111.231.100.212/logout");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setDoOutput(true);
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(String.format("token=%s", token));
                InputStream in = connection.getInputStream();
                BufferedReader reader;
                reader = new BufferedReader(new InputStreamReader(in));
                response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(response==null){
                return false;
            }
            Gson gson = new Gson();
            checkLogout = gson.fromJson(response.toString(), CheckLogout.class);
            if(checkLogout==null)
                return false;
            //                Toast.makeText(LoginActivity.this, checkLogin.getContent(), Toast.LENGTH_LONG).show();
            return checkLogout.isSuccess();
//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                finish();
                editor.putString("account", null);
                editor.putString("email", null);
                editor.putString("password", null);
                editor.apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
//            } else {
//                if(checkLogin.getContent().equals("Wrong Password!"))
//                    mPasswordView.requestFocus();
//                else
//                    mEmailView.requestFocus();
//                Looper.prepare();
//                Toast.makeText(LoginActivity.this, checkLogin.getContent(), Toast.LENGTH_SHORT).show();
//                Looper.loop();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

}
