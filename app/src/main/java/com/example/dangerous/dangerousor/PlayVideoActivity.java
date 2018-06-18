package com.example.dangerous.dangerousor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dangerous.dangerousor.util.mMediaController;
import com.example.dangerous.dangerousor.view.MyVideoView;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import java.io.File;

public class PlayVideoActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, mMediaController.MediaPlayerControl, TencentLocationListener {
    public static final String TAG = "PlayVideo";
    private MyVideoView videoView;
    private mMediaController controller;
    private String mVideoPath;
    private EditText editText;
    private Button confirm;
    private Button cancel;

    private TencentLocation mLocation;
    private String location;
    private TencentLocationManager locationManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(permissions, 0);
        }

        mVideoPath = getIntent().getExtras().getString("videoPath");

        setContentView(R.layout.now_playvideo);

        editText = findViewById(R.id.record_video_title);
        confirm = findViewById(R.id.record_upload);
        cancel = findViewById(R.id.record_cancel);
        locationManager = TencentLocationManager.getInstance(this);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(mVideoPath);
                file.delete();
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editText.getText().toString();
                if(!TextUtils.isEmpty(title)){
                    if(title.length()>10){
                        Toast.makeText(PlayVideoActivity.this, "Title should be no more than 10", Toast.LENGTH_LONG).show();
                    }
                    else{
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(mVideoPath);
                        Bitmap bmp = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                        TencentLocationRequest request = TencentLocationRequest.create();
                        int error = locationManager.requestLocationUpdates(request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME).setInterval(500).setAllowDirection(true), PlayVideoActivity.this);
                        if(error != 0){
                            location = null;
                        }
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
            this.location = location.getAddress();
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
