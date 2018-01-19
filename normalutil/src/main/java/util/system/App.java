package util.system;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.multidex.MultiDexApplication;

/**
 * Application
 * Created by @author ZRP on 2016/9/8.
 */
public class App extends MultiDexApplication {

    public static Context context;
    public static Activity activity;

    private static PActivityLifecycleCallbacks lifecycleCallbacks;

    /**
     * 是否锁屏中
     */
    public static volatile AppKeyguard isKeyguard = AppKeyguard.KG_INIT;

    public static long t;

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onCreate() {
        super.onCreate();

        t = System.currentTimeMillis();
        context = getApplicationContext();

    }

    /**
     * @return 获取进程生命周期回调
     */
    public static PActivityLifecycleCallbacks getLifecycleCallbacks() {
        return lifecycleCallbacks;
    }

    /**
     * @return 获取当前展示的activity对象，如果activity为null则返回applicationContext
     */
    public static Context getActivity() {
        return activity == null ? context : activity;
    }

    public static Context getContext() {
        return context;
    }

    /**
     * @return 判断最后的Activity是否属于前台显示
     */
    public static boolean isForeground() {
        return lifecycleCallbacks.isForeground();
    }

    /**
     * 应用锁屏状态
     */
    public enum AppKeyguard {
        //初始化
        KG_INIT,
        //锁屏中
        KG_SCREEN_OFF,
        //开屏中
        KG_SCREEN_ON
    }

}
