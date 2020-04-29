package com.techsalt.reto.helper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class MyApplication extends Application {
    public static final String TAG = MyApplication.class.getSimpleName();
    private static MyApplication mInstance;
    public static String SHARED_PREFERENCES_NAME = "shared_Reto";
    private static Intent screenshotPermission = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static void buildAlertMessageNoGps(final Context mContext) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        mContext.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        buildAlertMessageNoGps(mContext);
                        dialog.cancel();

                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static void clearPref() {
        SharedPreferences prefs = mInstance.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();


    }

    public static void clearKeyPref(String key) {
        SharedPreferences prefs = mInstance.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove(key);
        edit.commit();


    }

    public static String readStringPref(String key) {
        SharedPreferences prefs = mInstance.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        return prefs.getString(key, "");


    }

    public static void writeStringPref(String key, String data) {
        SharedPreferences prefs = mInstance.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(key, data);
        edit.commit();

    }

    public static boolean readBooleanPref(String key) {
        SharedPreferences prefs = mInstance.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        return prefs.getBoolean(key, false);
    }

    public static void writeBooleanPref(String key, boolean data) {
        SharedPreferences prefs = mInstance.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(key, data);
        edit.commit();

    }





}
