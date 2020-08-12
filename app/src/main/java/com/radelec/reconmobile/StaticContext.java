package com.radelec.reconmobile;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import java.io.File;

public class StaticContext extends Application {

    private static Context context;
    private static File filesDir;

    public void onCreate() {
        super.onCreate();
        StaticContext.context = getApplicationContext();
        StaticContext.filesDir = getFilesDir();
    }

    public static Context getStaticContext() {
        return StaticContext.context;
    }

    public static File getStaticFilesDir() {
        return StaticContext.filesDir;
    }
}
