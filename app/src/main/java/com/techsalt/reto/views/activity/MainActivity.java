package com.techsalt.reto.views.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.techsalt.reto.R;
import com.techsalt.reto.services.Reto;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

import static com.techsalt.reto.services.Reto.NOTIFICATION_ID;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.show_demo)
    Button showDemo;
    public static TextView tvEnglish;
    public static TextView tvHindi;


    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private static final int CUSTOM_OVERLAY_PERMISSION_REQUEST_CODE = 101;

    private static final int REQUEST_SCREENSHOT = 59706;

    private MediaProjectionManager mgr;

    public static List<Intent> sendingIntent = new ArrayList<>();

    Intent intent;
    NotificationChannel defaultChannel;
    NotificationManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        tvEnglish=findViewById(R.id.tv_english);
        tvHindi=findViewById(R.id.tv_hindi);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String channelId = getString(R.string.default_floatingview_channel_id);
            final String channelName = getString(R.string.default_floatingview_channel_name);
            defaultChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(defaultChannel);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Reto.resultData == null) {
                startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_SCREENSHOT);
            }
        }

        showDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFloatingView(MainActivity.this, true, false);
            }
        });
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE) {
            showFloatingView(this, false, false);
        } else if (requestCode == CUSTOM_OVERLAY_PERMISSION_REQUEST_CODE) {
            showFloatingView(this, false, true);
        }

        if (requestCode == REQUEST_SCREENSHOT) {
            if (resultCode == RESULT_OK) {

                Reto.resultCode = resultCode;
                Reto.resultData = (Intent) data.clone();

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(MainActivity.this, "Permission to take screenshot is reired", Toast.LENGTH_SHORT).show();
                startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_SCREENSHOT);
            }
        }
    }

    @SuppressLint("NewApi")
    private void showFloatingView(Activity activity, boolean isShowOverlayPermission, boolean isCustomFloatingView) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startFloatingViewService(this, isCustomFloatingView);
            return;
        }
        if (Settings.canDrawOverlays(activity)) {
            startFloatingViewService(this, isCustomFloatingView);
            return;
        }

        if (isShowOverlayPermission) {
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
            startActivityForResult(intent, CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    private void startFloatingViewService(Activity activity, boolean isCustomFloatingView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (activity.getWindow().getAttributes().layoutInDisplayCutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER) {
                throw new RuntimeException("'windowLayoutInDisplayCutoutMode' do not be set to 'never'");
            }
            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                throw new RuntimeException("Do not set Activity to landscape");
            }
        }

        intent = new Intent(activity, Reto.class);
        intent.putExtra(Reto.EXTRA_CUTOUT_SAFE_AREA, FloatingViewManager.findCutoutSafeArea(activity));
        /*intent.putExtra(Reto.EXTRA_RESULT_CODE, intentResultCode);
        intent.putExtra(Reto.EXTRA_RESULT_INTENT, intentData);*/
        ContextCompat.startForegroundService(activity, intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("onDestroyActivity", "onDestroyActivity");

        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
            Log.e("onDestroyActivityCancel", "onDestroyActivityCancel");
        }

        Intent serviceIntent = new Intent(MainActivity.this, Reto.class);
        stopService(serviceIntent);

        //stopService(new Intent(Reto.MY_SERVICE));
        Log.e("onDestroyActivityStop", "onDestroyActivityStop");
        showFloatingView(MainActivity.this, true, false);
        Log.e("onDestroyActivityShow", "onDestroyActivityShow");
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(MainActivity.this, Reto.class));
        } else {
            startService(new Intent(MainActivity.this, Reto.class));
        }*/
    }
}