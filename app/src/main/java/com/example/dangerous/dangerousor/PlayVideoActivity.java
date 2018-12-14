package com.example.dangerous.dangerousor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.dangerous.dangerousor.util.mMediaController;
import com.example.dangerous.dangerousor.view.MyVideoView;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class PlayVideoActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, mMediaController.MediaPlayerControl, TencentLocationListener {
    public static final String TAG = "PlayVideo";
    private MyVideoView videoView;
    private mMediaController controller;
    private String mVideoPath;
    private EditText editText;
    private Spinner topic_spinner;
    private String topic;
    private Button confirm;
    private Button cancel;
    private String token;
    private View upload;
    private View uploading;
    private ImageView uploadingImage;
    private Bitmap bmp;
    private String title;

    private TencentLocation mLocation;
    private String location;
    private TencentLocationManager locationManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        token = InstanceID.getInstance(this).getId();


        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };

            if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, 0);
            }
        }

        mVideoPath = getIntent().getExtras().getString("videoPath");

        setContentView(R.layout.now_playvideo);

        editText = findViewById(R.id.record_video_title);
        topic_spinner = findViewById(R.id.topic_spinner);//初始化Spinner
        confirm = findViewById(R.id.record_upload);
        cancel = findViewById(R.id.record_cancel);
        upload = findViewById(R.id.upload_include);
        uploading = findViewById(R.id.uploading_include);
        uploadingImage = findViewById(R.id.uploading);
        locationManager = TencentLocationManager.getInstance(this);


        topic_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if(pos==0){
                    topic = "";
                }
                else {
                    String[] topics = getResources().getStringArray(R.array.topicarr);
                    topic = topics[pos];
                    try {
                        topic = URLEncoder.encode(topic, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(mVideoPath);
                file.delete();
                finish();
                Intent intent = new Intent(PlayVideoActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = editText.getText().toString();
                if(!TextUtils.isEmpty(title)){
                    if(title.length()>10){
                        Toast.makeText(PlayVideoActivity.this, "Title should be no more than 10", Toast.LENGTH_LONG).show();
                    }
                    else{
                        try {
                            title = URLEncoder.encode(title, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(mVideoPath);
                        bmp = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                        TencentLocationRequest request = TencentLocationRequest.create();
                        int error = locationManager.requestLocationUpdates(request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME).setInterval(500).setAllowDirection(true), PlayVideoActivity.this);
                        if(error != 0){
                            location = "";
                        }

                        Toast.makeText(PlayVideoActivity.this, "开始上传视频", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


//        File sourceVideoFile = new File(mVideoPath);
        videoView = findViewById(R.id.videoView);
//        int screenW = getWindowManager().getDefaultDisplay().getWidth();
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) videoView.getLayoutParams();
//        params.width = screenW;
//        params.height = screenW * 4 / 3;
//        params.gravity = Gravity.TOP;
//        videoView.setLayoutParams(params);

//        videoView.setOnPreparedListener(this);
        controller = new mMediaController(this);
        videoView.setMediaController(controller);
        videoView.setBackgroundColor(Color.BLACK);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            videoView.setBackgroundColor(Color.TRANSPARENT);
                        }
                        return true;
                    }
                });
                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                mp.start();
                mp.setLooping(true);
            }
        });
        controller.setMediaPlayer(videoView);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            videoView.setVideoPath(mVideoPath);//   /storage/emulated/0/RecordVideo/VID_20180618_181338.mp4
            videoView.requestFocus();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer(videoView);
        controller.setAnchorView((ViewGroup) findViewById(R.id.fl_videoView_parent));
        controller.show();

    }

    @Override
    public void start() {
        videoView.start();
    }

    @Override
    public void pause() {
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    public int getDuration() {
        return videoView.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return videoView.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        videoView.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return videoView.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return videoView.canPause();
    }

    @Override
    public boolean canSeekBackward() {
        return videoView.canSeekBackward();
    }

    @Override
    public boolean canSeekForward() {
        return videoView.canSeekForward();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    @Override
    public void onBackPressed() {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        if (error == TencentLocation.ERROR_OK) {
            // 定位成功
            mLocation = location;
            // 更新 status
            try {
                this.location = URLEncoder.encode(location.getAddress(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            locationManager.removeUpdates(this);

            upload.setVisibility(View.GONE);
            uploading.setVisibility(View.VISIBLE);
            uploadingImage.setImageBitmap(bmp);
            videoView.stopPlayback();
            UploadTask mTask = new UploadTask(title);
            mTask.execute((Void) null);

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

    public class UploadTask extends AsyncTask<Void, Void, Boolean> {

        private String title;
        private CheckUpload checkUpload;

        class CheckUpload {
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

        UploadTask(String title) {
            this.title = title;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            StringBuilder response=null;
            String end = "\r\n";
            String twoHyphens = "--";
            String boundary = "******";
            try {
                // Simulate network access.
//                Thread.sleep(2000);
                URL url = new URL("http://" + Const.IP + "/upload");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Charset", "UTF-8");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(twoHyphens + boundary + end);
                out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""+ this.title + "+title+" + topic + "+topic+" + location + "+location+" + token + "+token+" + mVideoPath.replace("/storage/emulated/0/RecordVideo/", "") + "\"" + end);
                out.writeBytes("Content-Type:application/octet-stream;" + end);
                out.writeBytes(end);
                FileInputStream fis = new FileInputStream(mVideoPath);
                byte[] buffer = new byte[8192]; // 8k
                int count = 0;
                while ((count = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);

                }
                fis.close();
                out.writeBytes(end);
                out.writeBytes(twoHyphens + boundary + twoHyphens + end);
                out.flush();
                InputStream in = connection.getInputStream();
                BufferedReader reader;
                reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
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
            checkUpload = gson.fromJson(response.toString(), CheckUpload.class);
            if(checkUpload==null)
                return false;
            //                Toast.makeText(LoginActivity.this, checkLogin.getContent(), Toast.LENGTH_LONG).show();
            return checkUpload.isSuccess();
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
        protected void onPostExecute(final Boolean success) {

            if (success) {
                Toast.makeText(PlayVideoActivity.this, "上传成功", Toast.LENGTH_LONG).show();
                finish();
                Intent intent = new Intent(PlayVideoActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                uploading.setVisibility(View.GONE);
                upload.setVisibility(View.VISIBLE);
                videoView.setVideoPath(mVideoPath);//   /storage/emulated/0/RecordVideo/VID_20180618_181338.mp4
                videoView.requestFocus();
                videoView.start();
                Toast.makeText(PlayVideoActivity.this, "Net Work Error", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }


}
