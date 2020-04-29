package com.techsalt.reto.services;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.techsalt.reto.R;
import com.techsalt.reto.crop_slider.CropImageView;
import com.techsalt.reto.helper.GoogleTranslate;
import com.techsalt.reto.helper.ImageTransmogrifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.techsalt.reto.views.activity.MainActivity.tvEnglish;
import static com.techsalt.reto.views.activity.MainActivity.tvHindi;

public class Reto extends Service implements FloatingViewListener {

    public static final String EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area";
    public static final int NOTIFICATION_ID = 9083150;
    private FloatingViewManager mFloatingViewManager;
    Context context = Reto.this;
    DisplayMetrics metrics;
    public LayoutInflater inflater;
    public WindowManager windowManager;
    public static CircleImageView iconView = null;
    public View view;
    ImageView crossArrow, tickArrow;
    Bitmap icon;
    CropImageView cropImageView;

    NotificationManager mNotificationManager;

    /*Screenshot Variables */
    static final int VIRT_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private MediaProjection projection;
    private VirtualDisplay vdisplay;
    final private HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(), android.os.Process.THREAD_PRIORITY_BACKGROUND);
    private Handler handler;
    private MediaProjectionManager mgr;
    private ImageTransmogrifier it;
    String timeStamp;
    AppOpsManager appOps;
    UsageStatsManager usm;

    public static Intent resultData = null;
    public static int resultCode = 0;

    String defaultHomePackageName, currentForegroundPackageName;

    public Reto() {
    }

    public int getStatusBarHeight() {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        int result = 0;
        int resourceId = cw.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = cw.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mgr = (MediaProjectionManager) this.getSystemService(MEDIA_PROJECTION_SERVICE);
            usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        }
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);


        metrics = new DisplayMetrics();
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mFloatingViewManager != null) {
            return START_REDELIVER_INTENT;
        }

       /* if (intent.getAction() == null) {
            resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 1337);
            resultData = intent.getParcelableExtra(EXTRA_RESULT_INTENT);
        }*/
        windowManager.getDefaultDisplay().getMetrics(metrics);
        inflater = LayoutInflater.from(this);
        iconView = (CircleImageView) inflater.inflate(R.layout.widget_chathead, null, false);
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (usageAccessGranted(context)) {
                        getCurrentAppForegound();
                        if (defaultHomePackageName.equalsIgnoreCase(currentForegroundPackageName)) {
                            Toast.makeText(context, "No App is in Foreground", Toast.LENGTH_SHORT).show();
                        } else {
                            createLayoutForServiceClass();
                        }
                    } else {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            }
        });

        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
        mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);
        mFloatingViewManager.setSafeInsetRect((Rect) intent.getParcelableExtra(EXTRA_CUTOUT_SAFE_AREA));
        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        options.overMargin = (int) (16 * metrics.density);
        mFloatingViewManager.addViewToWindow(iconView, options);
        startForeground(NOTIFICATION_ID, createNotification(this));

        return START_REDELIVER_INTENT;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean usageAccessGranted(Context context) {
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void createLayoutForServiceClass() {

        iconView.setVisibility(View.GONE);

        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        params.gravity = Gravity.END | Gravity.TOP;
        view = inflater.inflate(R.layout.area_selection, null);

        crossArrow = view.findViewById(R.id.iv_cross);
        tickArrow = view.findViewById(R.id.iv_tick);
        cropImageView = view.findViewById(R.id.CropImageView);

        icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.transparent_image);
        cropImageView.setImageBitmap(icon);

        crossArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("clicked", "Image is clicked");
                view.setVisibility(View.GONE);
                iconView.setVisibility(View.VISIBLE);
            }
        });

        tickArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCapture();
            }
        });

        windowManager.addView(view, params);
    }

    public void getCurrentAppForegound() {

        PackageManager localPackageManager = getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        defaultHomePackageName = localPackageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        Log.e("techsalt", defaultHomePackageName);

        if (Build.VERSION.SDK_INT >= 21) {
            long time = System.currentTimeMillis();
            List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (applist != null && applist.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : applist) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentForegroundPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
            Log.e("techsaltsolutions", "Current App in foreground is: " + currentForegroundPackageName);
        } else {

            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            currentForegroundPackageName = (manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
            Log.e("techsaltsolutions", "Current App in foreground is: " + currentForegroundPackageName);
        }

    }

    @Override
    public void onDestroy() {
        destroy();
        stopCapture();
        Log.e("onDestroyService", "onDestroyService");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void destroy() {
        if (mFloatingViewManager != null) {
            mFloatingViewManager.removeAllViewToWindow();
            mFloatingViewManager = null;
        }
    }

    private Notification createNotification(Context context) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getResources().getString(R.string.default_floatingview_channel_id));
        Notification notification = builder.setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.profile)
                .setContentTitle(this.getResources().getString(R.string.app_name))
                .setContentText(this.getResources().getString(R.string.running))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        return notification;
    }

    @Override
    public void onFinishFloatingView() {
        stopSelf();
    }

    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {

    }

    /*Screenshot Code*/
    public void processImage(final byte[] png) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

                File outputImage = new File(getExternalFilesDir(null), timeStamp + ".png");

                FileOutputStream fos = null;

                try {
                    fos = new FileOutputStream(outputImage);

                    fos.write(png);
                    fos.flush();
                    fos.getFD().sync();
                    fos.close();

                    MediaScannerConnection.scanFile(Reto.this,
                            new String[]{outputImage.getAbsolutePath()},
                            new String[]{"image/png"},
                            null);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Exception writing out screenshot", e);
                }
                hideViews();
            }
        });
        stopCapture();

        loadImageFromStorage(getExternalFilesDir(null), timeStamp);
    }

    private void hideViews() {
        //view.setVisibility(View.GONE);
        if (windowManager != null) {
            windowManager.removeView(view);
            windowManager = null;
        }

        iconView.setVisibility(View.VISIBLE);
    }

    private void loadImageFromStorage(File path, String imageName) {

        try {
            File f = new File(path, imageName + ".png");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));


            try {
                TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
                if (!textRecognizer.isOperational()) {
                    Toast.makeText(context, "Could not get the text", Toast.LENGTH_SHORT).show();
                } else {
                    Frame frame = new Frame.Builder().setBitmap(b).build();
                    SparseArray<TextBlock> items = textRecognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < items.size(); i++) {
                        TextBlock myItem = items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");
                    }
                    Log.e("convetedEnglishText", sb.toString());
                    tvEnglish.setText(sb.toString());

                    GoogleTranslate googleTranslate = new GoogleTranslate();
                    String result = googleTranslate.execute(sb.toString(), "en", "hi").get();

                    Log.e("TranslatedHindiText", result);
                    tvHindi.setText(result);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.e("filepath", f.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void stopCapture() {
        if (projection != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                projection.stop();
            }
            vdisplay.release();
            projection = null;
        }
    }

    public void startCapture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            projection = mgr.getMediaProjection(resultCode, resultData);
        }
        it = new ImageTransmogrifier(this);

        MediaProjection.Callback cb = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cb = new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    vdisplay.release();
                }
            };
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vdisplay = projection.createVirtualDisplay("com.techsalt.reto",
                    it.getWidth(), it.getHeight(),
                    getResources().getDisplayMetrics().densityDpi
                    , VIRT_DISPLAY_FLAGS, it.getSurface(), null, handler);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            projection.registerCallback(cb, handler);
        }
    }

    public WindowManager getWindowManager() {
        return (windowManager);
    }

    public Handler getHandler() {
        return (handler);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("onTaskRemoved", "onTaskRemoved");

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(Reto.this, Reto.class));
        } else {
            context.startService(new Intent(Reto.this, Reto.class));
        }
        super.onTaskRemoved(rootIntent);

    }

}
