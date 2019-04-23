package cs.configeditor;

import android.app.Application;
import android.content.Context;

import cs.util.DbHelper;
import cs.util.Util;

/**
 * Created by sunliang on 2016/11/7.
 */

public class MyApplication extends Application { // user your appid the key.
    @Override
    public void onCreate() {
        super.onCreate();
        DbHelper.setContext(this);
        Util.setApplicationContext(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}