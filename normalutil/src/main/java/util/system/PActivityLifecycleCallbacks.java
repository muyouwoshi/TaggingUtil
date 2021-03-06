package util.system;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.LinkedList;

/**
 * Application中activity生命周期监测回调，>=API14
 * Created by @author ZRP on 2016/9/8.
 */
public class PActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    final int LISTSIZE = 10;
    /**
     * 本地模拟的activity栈链表，记录activity跳转行为
     */
    private LinkedList<String> activities = new LinkedList<>();
    /**
     * 最后的Activity是否属于前台显示
     */
    private volatile boolean isForeground = false;

    /**
     * @return 获取本地模拟的activity栈链表
     */
    public LinkedList<String> getActivities() {
        return activities;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {

        // 本地模拟一个只存储10条记录的activity栈
        activities.add(activity.getClass().getSimpleName());
        if (activities.size() > LISTSIZE) {
            activities.remove(0);
        }

    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    /**
     * @return 最后的Activity是否属于前台显示
     */
    public boolean isForeground() {
        return isForeground;
    }
}
