package com.techsalt.reto.helper;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.projection.MediaProjection;

import java.net.URISyntaxException;

/**
 * Created by Hp on 4/5/2018.
 */

public class PrefData extends Application {

    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor editor;

    private static PrefData mInstance;
    private static String sharedPrefName = "Reto";

    public static String dataIntent="data_intent";
    public static String intentResultCode="resultCode";
    public static String screenshotPermission="screenshotPermission";
    static Intent parseIntent;




    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public PrefData() {
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }


    public static synchronized PrefData getInstance() {
        return mInstance;
    }

    public PrefData(Context con) {
        mSharedPreferences = con.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
    }

    public void clear() {
        editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static void clearPref() {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();


    }

    public static void clearKeyPref(String key) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        editor.remove(key);
        editor.apply();


    }

    public static String readStringPref(String key) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        return mSharedPreferences.getString(key, "");
    }

    public static void writeStringPref(String key, String data) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        editor = mSharedPreferences.edit();
        editor.putString(key, data);
        editor.apply();

    }

    public static void writeIntPref(String key, int data) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        editor = mSharedPreferences.edit();
        editor.putInt(key, data);
        editor.apply();

    }



   /* protected static void setScreenshotPermission(final Intent permissionIntent)
    {
        parseIntent = permissionIntent;
//screenshotPermission becomes null once the application is killed
    }

    public static MediaProjection getData()
    {
        return (mediaProjection);
    }*/


    public static Integer readIntPref(String key) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        return mSharedPreferences.getInt(key, 0);
    }

    public static boolean readBooleanPref(String key) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        return mSharedPreferences.getBoolean(key, false);
    }

    public static void writeBooleanPref(String key, boolean data) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        editor = mSharedPreferences.edit();
        editor.putBoolean(key, data);
        editor.apply();

    }

    public SharedPreferences getmSharedPreferences() {
        return mSharedPreferences;
    }

    public void setmSharedPreferences(SharedPreferences mSharedPreferences) {
        this.mSharedPreferences = mSharedPreferences;
    }

    public String getSharedPrefName() {
        return sharedPrefName;
    }

    public void setSharedPrefName(String sharedPrefName) {
        this.sharedPrefName = sharedPrefName;
    }

    public SharedPreferences.Editor getEditor() {
        return editor;
    }

    public void setEditor(SharedPreferences.Editor editor) {
        this.editor = editor;
    }

}
