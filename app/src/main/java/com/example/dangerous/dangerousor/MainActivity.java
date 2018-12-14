package com.example.dangerous.dangerousor;

//播放、录制模块

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dangerous.dangerousor.util.mMediaController;
import com.example.dangerous.dangerousor.view.MyVideoView;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static android.graphics.BitmapFactory.decodeByteArray;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MediaPlayer.OnPreparedListener, mMediaController.MediaPlayerControl {
    private UserLogoutTask mAuthTask = null;

    private final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码
    private boolean granted = false;
    private File sdCard;
    private File headpic;

    private Boolean first;
    private Boolean first2 = true;

    private TextView accountNickname;
    private TextView accountEmail;
    private ImageView accountBitmap;
    private ImageView accountBitmap2;
    private ImageView reviewframe;
    private MyVideoView videoView;
    private mMediaController controller;
    private ImageView authorBitmap;
    private TextView authorNick;
    private TextView authorTitle;
    private TextView authorLocation;
    private ImageButton favorBtn;
    private TextView favorCount;

    private Button nextVideo;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String nickName;
    private String eMail;
    private String bitMap;
    private Bitmap bitmap;
    private String token;
    private View content_main;
    private View change_pic;
    private View change_nick;
    private View change_pw;
    private View video_main;

    private String fileName;
    private String fileName2;
    private Boolean isLiked;
    private Boolean isLiked2;
    private Integer likeCount;


    private DownloadTask.DetailCheck detailCheck;

    @Override
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer(videoView);
        controller.setAnchorView((ViewGroup) findViewById(R.id.fl_videoView_parent2));
        controller.show();

    }

    @Override
    public boolean canSeekForward() {
        return videoView.canSeekForward();
    }

    @Override
    public boolean canSeekBackward() {
        return videoView.canSeekBackward();
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
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        first = true;

        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
            };

            if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, 0);
            }
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        accountNickname = headerView.findViewById(R.id.account_nickname);
        accountEmail = headerView.findViewById(R.id.account_email);
        accountBitmap = headerView.findViewById(R.id.account_bitmap);
        accountBitmap2 = findViewById(R.id.account_bitmap2);
        reviewframe = findViewById(R.id.reviewframe);
        videoView = findViewById(R.id.videocontent);
        content_main = findViewById(R.id.content_main);
        change_pic = findViewById(R.id.change_pic);
        change_nick = findViewById(R.id.change_nick);
        change_pw = findViewById(R.id.change_pw);
        video_main = findViewById(R.id.videomain);
        authorBitmap = findViewById(R.id.authorpic);
        authorNick = findViewById(R.id.authornick);
        authorTitle = findViewById(R.id.videotitle);
        authorLocation = findViewById(R.id.videoplace);
        favorBtn = findViewById(R.id.favorbtn);
        favorCount = findViewById(R.id.favorcount);

        nextVideo = findViewById(R.id.nextvideo);
        nickName = sharedPreferences.getString("account", "");
        eMail = sharedPreferences.getString("email", "");
        bitMap = sharedPreferences.getString("bitmap", "");
        token = InstanceID.getInstance(this).getId();
        accountNickname.setText(nickName);
        accountEmail.setText(eMail);
        sdCard = Environment.getExternalStorageDirectory();
        headpic = new File(sdCard, "Pictures" + "/head.jpg");

        if(!bitMap.equals(""))
        {
            try {
                bitMap = bitMap.replace(' ', '+');
                byte[] bytes = Base64.decode(bitMap, Base64.NO_PADDING);
                bitmap = decodeByteArray(bytes, 0, bytes.length);
                accountBitmap.setImageBitmap(bitmap);
                accountBitmap2.setImageBitmap(bitmap);
            } catch (Exception e){
                e.printStackTrace();
                bitMap = "";
                editor.putString("bitmap", "");
                editor.apply();
            }
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        Button buttonLocal = findViewById(R.id.buttonLocal);
        Button buttonCamera = findViewById(R.id.buttonCamera);
        Button change_pic_confirm = findViewById(R.id.change_pic_confirm);

        buttonLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentFromGallery = new Intent();
                // 设置文件类型
                intentFromGallery.setType("image/*");
                intentFromGallery.setAction(Intent.ACTION_PICK);
                startActivityForResult(intentFromGallery, 1);
            }
        });

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (Build.VERSION.SDK_INT >= 19) {
                        View decorView = getWindow().getDecorView();
                        decorView.setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    } else {
                        View decorView = getWindow().getDecorView();
                        int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
                        decorView.setSystemUiVisibility(option);
                    }
                    Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//开启相机应用程序获取并返回图片（capture：俘获）
                    intent2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(headpic));//指明存储图片或视频的地址URI
                    startActivityForResult(intent2, 2);//采用ForResult打开
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "相机无法启动，请先开启相机权限", Toast.LENGTH_LONG).show();
                }
            }
        });

        change_pic_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ByteArrayOutputStream baos = null;
                try {
                    if (bitmap != null) {
                        baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                        baos.flush();
                        baos.close();

                        byte[] bitmapBytes = baos.toByteArray();
                        byte[] encode = Base64.encode(bitmapBytes, Base64.NO_PADDING);
                        bitMap = new String(encode);

                        ChangeTask mTask = new ChangeTask(token, bitMap, "", "");
                        mTask.execute((Void) null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (baos != null) {
                            baos.flush();
                            baos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        final EditText new_nick = findViewById(R.id.new_nickname);

        Button new_nick_confirm = findViewById(R.id.change_nick_confirm);

        new_nick_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nickname = new_nick.getText().toString();
                if(TextUtils.isEmpty(nickname)){
                    Toast.makeText(MainActivity.this, "昵称不能为空", Toast.LENGTH_LONG).show();
                }
                else {
                    if (nickname.length() > 11) {
                        Toast.makeText(MainActivity.this, "昵称不能长于11", Toast.LENGTH_LONG).show();
                    }
                    else{
                        try {
                            nickname = URLEncoder.encode(nickname, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        ChangeTask mTask = new ChangeTask(token, "", nickname, "");
                        mTask.execute((Void) null);
                    }
                }
            }
        });

        final EditText new_pw = findViewById(R.id.new_pw);
        final EditText new_pw2 = findViewById(R.id.new_pw2);

        Button new_pw_confirm = findViewById(R.id.change_pw_confirm);

        new_pw_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pw = new_pw.getText().toString();
                String pw2 = new_pw2.getText().toString();
                if(TextUtils.isEmpty(pw)||TextUtils.isEmpty(pw2)){
                    Toast.makeText(MainActivity.this, "Password can't be empty", Toast.LENGTH_LONG).show();
                }
                else{
                    if(pw.length()<4||pw.length()>18){
                        Toast.makeText(MainActivity.this, "Password length should be between 5 and 18", Toast.LENGTH_LONG).show();
                    }
                    else{
                        if(!pw.equals(pw2)){
                            Toast.makeText(MainActivity.this, "Two Password should be same", Toast.LENGTH_LONG).show();
                        }
                        else{
                            ChangeTask mTask = new ChangeTask(token, "", "", pw);
                            mTask.execute((Void) null);
                        }
                    }
                }
            }
        });

        favorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                    后端获取该视频的点赞列表，比对用户token是否点赞过。
                    该用户没有点赞过的，加入点赞，否则取消点赞（返回一个值标识两种情况）。
                 */
                FavorTask mTask = new FavorTask(token, nickName, fileName);   // filename是当前播放视频？
                mTask.execute((Void) null);
            }
        });

        nextVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bitMap;
                try {
                    bitMap = detailCheck.getPic().replace(' ', '+');
                    byte[] bytes = Base64.decode(bitMap, Base64.NO_PADDING);
                    authorBitmap.setImageBitmap(decodeByteArray(bytes, 0, bytes.length));
                } catch (Exception e) {
                    e.printStackTrace();
                    bitMap = "";
                    byte[] bytes = Base64.decode(bitMap, Base64.NO_PADDING);
                    authorBitmap.setImageBitmap(decodeByteArray(bytes, 0, bytes.length));
                }
                authorNick.setText(detailCheck.getAuthor());
                authorTitle.setText(detailCheck.getTitle());
                authorLocation.setText(detailCheck.getPlace());
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    videoView.stopPlayback();
                    videoView.setVideoPath(Environment.getExternalStorageDirectory().toString() + "/DangerousorDownload/" + fileName2);//   /storage/emulated/0/RecordVideo/VID_20180618_181338.mp4
                    videoView.start();
                    videoView.requestFocus();
                }
                nextVideo.setVisibility(View.GONE);
                DownloadTask mTask = new DownloadTask(token);
                mTask.execute((Void) null);
            }
        });

        reviewframe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reviewframe.setVisibility(View.GONE);
                File file = new File(Environment.getExternalStorageDirectory().toString() + "/DangerousorDownload");
                if (file.exists()) {
                    String[] tempList = file.list();
                    File temp;
                    for (String aTempList : tempList) {
                        temp = new File(Environment.getExternalStorageDirectory().toString() + "/DangerousorDownload/" + aTempList);
                        if (temp.isFile()) {
                            temp.delete();
                        }
                    }
                }
                DownloadTask downloadTask = new DownloadTask(token);
                downloadTask.execute((Void) null);
                Toast.makeText(MainActivity.this, "开始缓存，请稍候", Toast.LENGTH_SHORT).show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //具有权限
                granted = true;
            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA}, GET_PERMISSION_REQUEST);
                granted = false;
            }
        }

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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "获取图片出现错误", Toast.LENGTH_SHORT).show();
        }
        else{
            switch(requestCode) {

                case 1:
//                    this.cropPhoto(Uri.parse(Objects.requireNonNull(data.getData()).toString().replace("content", "file")));
                    this.cropPhoto(data.getData());
                    break;

                /*
                 *  case 2 代表从拍摄照片的intent返回之后
                 *  完成拍摄照片之后，立刻打开系统自带的裁剪图片的intent
                 */
                case 2:
                    this.cropPhoto(Uri.fromFile(headpic));
                    break;

                /*
                 *  case 3 代表从裁剪照片的intent返回之后
                 *  完成裁剪照片后，就要将图片生成bitmap对象，然后显示在界面上面了
                 */
                case 3:
                    try {
                        /*
                         *  将图片转换成Bitmap对象
                         */
                        Bitmap bitmap_ = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(Uri.fromFile(headpic)));

                        int width = bitmap_.getWidth();
                        int height = bitmap_.getHeight();
                        // 设置想要的大小
                        int newWidth = 140;
                        int newHeight = 140;
                        // 计算缩放比例
                        float scaleWidth = ((float) newWidth) / width;
                        float scaleHeight = ((float) newHeight) / height;
                        Matrix matrix = new Matrix();
                        matrix.postScale(scaleWidth, scaleHeight);
                        bitmap = Bitmap.createBitmap(bitmap_, 0, 0, width, height, matrix, true);

                        Toast.makeText(this, Uri.fromFile(headpic).toString(), Toast.LENGTH_SHORT).show();

                        /*
                         *  在界面上显示图片
                         */
                        accountBitmap2.setImageBitmap(bitmap);
                    } catch(FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }
        }
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
        if (id == R.id.action_record) {
            Intent intent = new Intent(MainActivity.this, RecordVideo.class);
//            finish();
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_changepic) {
            videoView.pause();
            content_main.setVisibility(View.GONE);
            change_pw.setVisibility(View.GONE);
            change_nick.setVisibility(View.GONE);
            change_pic.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_changenick) {
            videoView.pause();
            content_main.setVisibility(View.GONE);
            change_pw.setVisibility(View.GONE);
            change_nick.setVisibility(View.VISIBLE);
            change_pic.setVisibility(View.GONE);
        } else if (id == R.id.nav_changepw) {
            videoView.pause();
            content_main.setVisibility(View.GONE);
            change_pw.setVisibility(View.VISIBLE);
            change_nick.setVisibility(View.GONE);
            change_pic.setVisibility(View.GONE);
        } else if (id == R.id.nav_manage) {
            videoView.start();
            content_main.setVisibility(View.VISIBLE);
            change_pw.setVisibility(View.GONE);
            change_nick.setVisibility(View.GONE);
            change_pic.setVisibility(View.GONE);
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
        if (requestCode == GET_PERMISSION_REQUEST) {
            int size = 0;
            if (grantResults.length >= 1) {
                int writeResult = grantResults[0];
                //读写内存权限
                boolean writeGranted = writeResult == PackageManager.PERMISSION_GRANTED;//读写内存权限
                if (!writeGranted) {
                    size++;
                }
                //录音权限
                int recordPermissionResult = grantResults[1];
                boolean recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!recordPermissionGranted) {
                    size++;
                }
                //相机权限
                int cameraPermissionResult = grantResults[2];
                boolean cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!cameraPermissionGranted) {
                    size++;
                }
                if (size == 0) {
                    granted = true;
                    onResume();
                }else{
                    Toast.makeText(this, "请到设置-权限管理中开启" + size, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
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
                URL url = new URL("http://" + Const.IP + "/logout");
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

    private void cropPhoto(Uri imageUri){
        /*
         *  准备打开系统自带的裁剪图片的intent
         */
        Intent intent = new Intent("com.android.camera.action.CROP"); //打开系统自带的裁剪图片的intent
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("scale", true);

        /*
         *  设置裁剪区域的宽高比例
         */
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        /*
         *  设置裁剪区域的宽度和高度
         */
        intent.putExtra("outputX", 140);
        intent.putExtra("outputY", 140);

        /*
         *  指定裁剪完成以后的图片所保存的位置
         */
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(headpic));
        Toast.makeText(this, "剪裁图片", Toast.LENGTH_SHORT).show();

        /*
         *  以广播方式刷新系统相册，以便能够在相册中找到刚刚所拍摄和裁剪的照片
         */
        Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentBc.setData(Uri.fromFile(headpic));
        this.sendBroadcast(intentBc);

        /*
         *  以forResult模式启动系统自带的裁剪图片的intent
         */
        startActivityForResult(intent, 3); //设置裁剪参数显示图片至ImageView
    }

    public class ChangeTask extends AsyncTask<Void, Void, Boolean> {

        private final String token;
        private final String bitmap;
        private final String nickname;
        private final String password;
        private Check check;

        class Check {
            private boolean success;

            public boolean isSuccess() {
                return success;
            }

            public void setSuccess(boolean success) {
                this.success = success;
            }
        }

        ChangeTask(String token, String bitmap, String nickname, String password) {
            this.token = token;
            this.bitmap = bitmap;
            this.nickname = nickname;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            StringBuilder response = null;
            try {
                // Simulate network access.
//                Thread.sleep(2000);
                URL url = new URL("http://" + Const.IP + "/change");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setDoOutput(true);
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(String.format("token=%s&bitmap=%s&nickname=%s&password=%s", token, bitmap, nickname, password));
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

            if (response == null) {
                return false;
            }
            Gson gson = new Gson();
            check = gson.fromJson(response.toString(), Check.class);
            return check != null && check.isSuccess();
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                if(!bitmap.equals("")) {
                    editor.putString("bitmap", bitmap);
                    editor.apply();
                    MainActivity.this.accountBitmap.setImageBitmap(MainActivity.this.bitmap);
                    MainActivity.this.change_pic.setVisibility(View.GONE);
                    MainActivity.this.content_main.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "头像修改成功", Toast.LENGTH_LONG).show();
                }
                if(!nickname.equals("")){
                    editor.putString("nickname", nickname);
                    editor.apply();
                    MainActivity.this.nickName = nickname;
                    MainActivity.this.accountNickname.setText(nickname);
                    MainActivity.this.change_nick.setVisibility(View.GONE);
                    MainActivity.this.content_main.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "昵称修改成功", Toast.LENGTH_LONG).show();
                }
                if(!password.equals("")){
                    editor.putString("password", password);
                    editor.apply();
                    MainActivity.this.content_main.setVisibility(View.VISIBLE);
                    MainActivity.this.change_pw.setVisibility(View.GONE);
                    MainActivity.this.content_main.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "密码修改成功", Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    public class DownloadTask extends AsyncTask<Void, Void, Boolean> {

        private final String token;
        private Check check;
        private DetailCheck detailCheck1;
        private DetailCheck detailCheck2;

        class Check {
            private String content;
            private boolean success;
            // 是否被当前用户点赞过
            private Boolean like;
            private Integer count;

            public Integer getCount() {
                return count;
            }

            public void setCount(Integer count) {
                this.count = count;
            }

            public Boolean isLike() { return like; }
            public void setLiked(boolean like) { this.like = like; }

            public boolean isSuccess() {
                return success;
            }

            public void setSuccess(boolean success) {
                this.success = success;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }
        }

        class DetailCheck{
            private String pic;
            private String author;
            private String title;
            private String place;

            public String getPic() {
                return pic;
            }

            public void setPic(String pic) {
                this.pic = pic;
            }

            public String getAuthor() {
                String temp = author;
                try {
                    temp = URLDecoder.decode(temp, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return temp;
            }

            public void setAuthor(String author) {
                this.author = author;
            }

            public String getTitle() {
                String temp = title;
                try {
                    temp = URLDecoder.decode(temp, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return temp;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getPlace() {
                String temp = place;
                try {
                    temp = URLDecoder.decode(temp, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return temp;
            }

            public void setPlace(String place) {
                this.place = place;
            }
        }

        public class FileUtils {
            private String path = Environment.getExternalStorageDirectory().toString() + "/DangerousorDownload";

            public FileUtils() {
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }
            }

            public File createFile(String FileName) {
                return new File(path, FileName);
            }

            public boolean isExist(String FileName){
                return new File(path, FileName).exists();
            }
        }

        DownloadTask(String token) {
            this.token = token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            StringBuilder response = null;
            try {
                // Simulate network access.
//                Thread.sleep(2000);
                URL url = new URL("http://" + Const.IP + "/videoname/" + this.token);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestProperty("Charset", "UTF-8");
                InputStream in = connection.getInputStream();
                BufferedReader reader;
                reader = new BufferedReader(new InputStreamReader(in));
                response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (response == null) {
                return false;
            }
            Gson gson = new Gson();
            check = gson.fromJson(response.toString(), Check.class);
            fileName = check.getContent();
            isLiked = check.isLike();
            likeCount = check.getCount();

            if(!check.isSuccess())
                return false;

            response = null;
            try {
                // Simulate network access.
//                Thread.sleep(2000);
                URL url = new URL("http://" + Const.IP + "/videodetail/" + fileName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestProperty("Charset", "UTF-8");
                InputStream in = connection.getInputStream();
                BufferedReader reader;
                reader = new BufferedReader(new InputStreamReader(in));
                response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (response == null) {
                return false;
            }
            gson = new Gson();
            detailCheck1 = gson.fromJson(response.toString(), DetailCheck.class);

            FileUtils fileUtils = new FileUtils();

            if(!first && fileUtils.isExist(fileName))
                return check != null && check.isSuccess();
            try {
                // Simulate network access.
//                Thread.sleep(2000);
                URL url = new URL("http://" + Const.IP + "/video/" + fileName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestProperty("Charset", "UTF-8");
                InputStream in = connection.getInputStream();
                FileOutputStream fileOutputStream;
                fileOutputStream = new FileOutputStream(fileUtils.createFile(fileName));
                byte[] buf = new byte[1024];
                int ch;
                while ((ch = in.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, ch);//将获取到的流写入文件中
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            if(first){
                response = null;
                try {
                    // Simulate network access.
//                Thread.sleep(2000);
                    URL url = new URL("http://" + Const.IP + "/videoname/" + this.token);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestProperty("Charset", "UTF-8");
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

                if (response == null) {
                    return false;
                }
                gson = new Gson();
                check = gson.fromJson(response.toString(), Check.class);
                fileName2 = check.getContent();
                isLiked2 = check.isLike();

                Log.i("What is filename", fileName + " " + fileName2);

                response = null;
                try {
                    // Simulate network access.
//                Thread.sleep(2000);
                    URL url = new URL("http://" + Const.IP + "/videodetail/" + fileName2);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestProperty("Charset", "UTF-8");
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

                if (response == null) {
                    return false;
                }
                gson = new Gson();
                detailCheck2 = gson.fromJson(response.toString(), DetailCheck.class);

                try {
                    // Simulate network access.
//                Thread.sleep(2000);
                    URL url = new URL("http://" + Const.IP + "/video/" + fileName2);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestProperty("Charset", "UTF-8");
                    InputStream in = connection.getInputStream();
                    FileOutputStream fileOutputStream;
                    fileOutputStream = new FileOutputStream(fileUtils.createFile(fileName2));
                    byte[] buf = new byte[1024];
                    int ch;
                    while ((ch = in.read(buf)) != -1) {
                        fileOutputStream.write(buf, 0, ch);//将获取到的流写入文件中
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                first = false;
            }
            return check != null && check.isSuccess();
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                if(first2) {
                    Toast.makeText(MainActivity.this, "缓存完成，开始播放", Toast.LENGTH_SHORT).show();
                    favorCount.setText(Integer.toString(likeCount)+"人赞过");
                    reviewframe.setVisibility(View.GONE);
                    video_main.setVisibility(View.VISIBLE);
                    String bitMap;
                    try {
                        bitMap = detailCheck1.getPic().replace(' ', '+');
                        byte[] bytes = Base64.decode(bitMap, Base64.NO_PADDING);
                        authorBitmap.setImageBitmap(decodeByteArray(bytes, 0, bytes.length));
                    } catch (Exception e) {
                        e.printStackTrace();
                        bitMap = "";
                        byte[] bytes = Base64.decode(bitMap, Base64.NO_PADDING);
                        authorBitmap.setImageBitmap(decodeByteArray(bytes, 0, bytes.length));
                    }
                    authorNick.setText(detailCheck1.getAuthor());
                    authorTitle.setText(detailCheck1.getTitle());
                    authorLocation.setText(detailCheck1.getPlace());

                    // 点赞按钮状态
                    if (!isLiked) {
                        favorBtn.setImageResource(R.drawable.btn_favorite);
                    }
                    else {
                        favorBtn.setImageResource(R.drawable.btn_favored);
                    }

                    nextVideo.setVisibility(View.VISIBLE);
                    detailCheck = detailCheck2;
                    isLiked = isLiked2;
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        videoView.setVideoPath(Environment.getExternalStorageDirectory().toString() + "/DangerousorDownload/" + fileName);//   /storage/emulated/0/RecordVideo/VID_20180618_181338.mp4
                        videoView.start();
                        videoView.requestFocus();
                    }
                    first2 = false;
                }
                else{
                    nextVideo.setVisibility(View.VISIBLE);
                    detailCheck = detailCheck1;
                    fileName2 = fileName;
                    isLiked2 = isLiked;
                }
            }
            else{
                Toast.makeText(MainActivity.this, "Network Error Or No Video On Server", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }



    public class FavorTask extends AsyncTask<Void, Void, Boolean> {

        private final String token;
        private final String nickname;
        private final String filename;
        private Check check;

        class Check {
            private String content;
            private boolean success;
            // 是否被当前用户点赞过
            private Boolean like;

            public void setCount(Integer count) {
                this.count = count;
            }

            public Integer getCount() {

                return count;
            }

            private Integer count;

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

            public boolean isLike() {
                return like;
            }

            public void setLiked(boolean like) { this.like = like; }

        }

        FavorTask(String token, String nickname, String filename) {
            this.token = token;
            this.nickname = nickname;
            this.filename = filename;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            StringBuilder response = null;
            try {
                // Simulate network access.
//                Thread.sleep(2000);
                URL url = new URL("http://" + Const.IP + "/mod_like");   // mod_like接口
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setDoOutput(true);
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(String.format("token=%s&nickname=%s&filename=%s", token, nickname, filename));
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

            if (response == null) {
                return false;
            }
            Gson gson = new Gson();
            check = gson.fromJson(response.toString(), Check.class);
            isLiked = check.isLike();
            Log.i("like","isLiked is" + isLiked.toString());
            likeCount = check.getCount();
            return check != null && check.isSuccess();
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {

                favorCount.setText(Integer.toString(likeCount)+"人赞过");

                // 该用户没有点赞过本视频
                if(!isLiked){

                    favorBtn.setImageResource(R.drawable.btn_favored);
                    Toast.makeText(MainActivity.this, "已点赞", Toast.LENGTH_LONG).show();
                }
                else{
                    favorBtn.setImageResource(R.drawable.btn_favorite);
                    Toast.makeText(MainActivity.this, "已取消点赞", Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

}


