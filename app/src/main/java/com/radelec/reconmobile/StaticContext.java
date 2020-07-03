package com.radelec.reconmobile;

import android.app.Application;
import android.content.Context;

public class StaticContext extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        StaticContext.context = getApplicationContext();
    }

    public static Context getStaticContext() {
        return StaticContext.context;
    }
}
