package cn.ucai.superwechat.utils;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/6.
 */
public class ExitAppActivity {

    List<Activity> mActivity = new LinkedList<>();
    private static ExitAppActivity instance = new ExitAppActivity();

    public static ExitAppActivity getInstance(){
        return instance;
    }

    public void addActivity(Activity activity){
        mActivity.add(activity);
    }

    public void deleteActivity(Activity activity){
        mActivity.remove(activity);
    }

    public void exitActivity(){
        for (Activity activity:mActivity) {
            activity.finish();
        }
    }
}
