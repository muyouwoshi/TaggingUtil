package com.face.tagging.moudle.base;

import util.log.PLogger;
import util.log.PSP;
import util.system.App;

/**
 * Created by zhoujie on 2018/1/15.
 */

public class MyApp extends App {

    @Override
    public void onCreate() {
        super.onCreate();
        PSP.getInstance().init(getContext(),Config.SP_SETTING);
        PLogger.init(true,"tagging");
    }
}
