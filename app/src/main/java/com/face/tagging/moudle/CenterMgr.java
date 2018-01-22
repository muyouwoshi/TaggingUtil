package com.face.tagging.moudle;

import com.face.tagging.moudle.base.BaseSettingData;
import com.face.tagging.moudle.base.Config;

import util.log.PSP;

/**
 * Created by zhoujie on 2018/1/15.
 */

public class CenterMgr {

    private BaseSettingData baseSettingData;

    private static volatile CenterMgr instance;

    public static CenterMgr getinstance(){
        if(instance == null){
            synchronized (CenterMgr.class){
                if(instance == null){
                    instance = new CenterMgr();
                }
            }
        }
        return instance;
    }

    private CenterMgr(){
    }

    public synchronized BaseSettingData getBaseSetting(){
        if(baseSettingData == null){
            baseSettingData = new BaseSettingData();
            baseSettingData.baseSelectSet = PSP.getInstance().getInt(Config.SP_BASE_SETTING, Config.BASE_SAME_AS_IMAGE);
            baseSettingData.baseSelectName = PSP.getInstance().getString(Config.SP_BASE_NAME, "base");
            baseSettingData.baseReangle = PSP.getInstance().getInt(Config.SP_BASE_ANGLE,Config.BASE_ANGLE_270);
            baseSettingData.saveAllInOne = PSP.getInstance().getBoolean(Config.SAVE_ALL_IN_ONE,false);
            baseSettingData.saveDirName = PSP.getInstance().getString(Config.SP_SAVE_BASE_NAME,"base");
            baseSettingData.saveWithTag = PSP.getInstance().getBoolean(Config.SAVE_WITH_TAG,false);
            baseSettingData.saveJPG = PSP.getInstance().getBoolean(Config.SAVE_JPG,false);
            baseSettingData.saveYUV = PSP.getInstance().getBoolean(Config.SAVE_YUV,false);
        }
        return baseSettingData;
    }

    public synchronized void updataBaseSetting(){
        PSP.getInstance().put(Config.SP_BASE_SETTING,baseSettingData.baseSelectSet);
        PSP.getInstance().put(Config.SP_BASE_NAME,baseSettingData.baseSelectName);
        PSP.getInstance().put(Config.SP_BASE_ANGLE,baseSettingData.baseReangle);
        PSP.getInstance().put(Config.SAVE_ALL_IN_ONE,baseSettingData.saveAllInOne);
        PSP.getInstance().put(Config.SP_SAVE_BASE_NAME,baseSettingData.saveDirName);
        PSP.getInstance().put(Config.SAVE_WITH_TAG,baseSettingData.saveWithTag);
        PSP.getInstance().put(Config.SAVE_JPG,baseSettingData.saveJPG);
        PSP.getInstance().put(Config.SAVE_YUV,baseSettingData.saveYUV);
    }
}
