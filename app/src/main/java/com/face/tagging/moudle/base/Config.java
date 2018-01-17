package com.face.tagging.moudle.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 * Created by zhoujie on 2018/1/3.
 */

public class Config {
    public final static String TAG_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+"/com.face.tagging";
    public final static String BASE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+"/com.face.tagging/base_save";
    public final static int PREVIEW_WIDTH = 640;
    public final static int PREVIEW_HEIGHT = 480;


    public final static  String SP_SETTING = "setting";

    /**
     * sp存储底库选择模式为"BASE_SAME_AS_IMAGE"是底库的名称
     */
    public final static  String SP_BASE_NAME = "base_setting_name";

    /**
     * sp底库选择模式
     */
    public final static  String SP_BASE_SETTING = "base_setting_id";
    public final static int BASE_SIMPLE = 0;
    public final static int BASE_SAME_AS_IMAGE= 1;
    public final static int BASE_ALL= 2;

    /**
     * sp保存底库方式
     */
    public final static String SAVE_WITH_TAG= "save_with_tag";
    public final static String SAVE_ALL_IN_ONE = "save_all_in_one";
    public final static String SAVE_JPG = "save_jpg";
    public final static String SAVE_YUV = "save_yuv";

    /**
     * sp保存底库方式为"SAVE_ALL_IN_ONE"时底库文件夹的名称
     */
    public final static  String SP_SAVE_BASE_NAME = "base_save_setting_name";


    /**
     * sp存储角度变换
     */
    public final static  String SP_BASE_ANGLE = "base_setting_angle";
    public final static int BASE_ANGLE_0 = 0;
    public final static int BASE_ANGLE_90 = 1;
    public final static int BASE_ANGLE_180 = 2;
    public final static int BASE_ANGLE_270 = 3;

    /**
     * sftp配置
     */
    public static String SSH_KEY_Path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/sshkey/brainpp_id_rsa";
    public final static String SFTP_HOST = "bj-a-internal.brainpp.ml";
    public final static String SFTP_USER = "ws.zhoujie.brw";
    public static String REMOTE_ROOT = "/unsullied/sharefs/zhoujie/face_unlock";
    public static String SP_REMOTE_DIR = "remote_dir";

    public static SharedPreferences getSetting(Context context){
        return context.getSharedPreferences(SP_SETTING,0);
    }
}
