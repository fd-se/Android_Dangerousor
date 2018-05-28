package com.example.dangerous.dangerousor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StartActivity extends AppCompatActivity {
    UserLoginTaskByToken mAuthTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        //todo: Migrate before April 11, 2019
        String iid = InstanceID.getInstance(this).getId();
        mAuthTask = new UserLoginTaskByToken(iid);
        mAuthTask.execute((Void) null);
    }

    class UserLoginTaskByToken extends AsyncTask<Void, Void, Boolean> {

        private final String token;
        private CheckLogin checkLogin;

        class CheckLogin {
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

        UserLoginTaskByToken(String token) {
            this.token = token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            StringBuilder response=null;
            try {
                // Simulate network access.
//                Thread.sleep(2000);
                URL url = new URL("http://111.231.100.212/login_token");
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
            checkLogin = gson.fromJson(response.toString(), CheckLogin.class);
            if(checkLogin==null)
                return false;
            //                Toast.makeText(LoginActivity.this, checkLogin.getContent(), Toast.LENGTH_LONG).show();
            return checkLogin.isSuccess();
//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }

            // TODO: register the new account here.
        }

        @Override
        protected void onProgressUpdate(Void... values){
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(StartActivity.this);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("account", checkLogin.getContent());
//                editor.putString("email", mEmail);
//                editor.putString("password", mPassword);
                editor.apply();
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

}
