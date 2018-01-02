package com.megvii.csp.explorer;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.TextUtils;

/**
 * Created by huangyifei on 2017/4/25.
 */

public class FileExplorer {

    /**
     * 从SD卡根目录开始浏览文件
     *
     * @param activity      用于弹出文件选择框
     * @param onlyDirectory 只选择文件夹
     * @param listener      选择完成后的回调
     */
    public static void pickFile(Activity activity,
                                boolean onlyDirectory,
                                final FileSelectListener listener) {
       pickFile(activity, onlyDirectory, "", listener);
    }

    public static void pickFile(Activity activity,
                                boolean onlyDirectory,
                                int stringResID,
                                final FileSelectListener listener){
        String title = activity.getString(stringResID);
        pickFile(activity, onlyDirectory, title, listener);
    }

    public static void pickFile(Activity activity,
                                boolean onlyDirectory,
                                String title,
                                final FileSelectListener listener){
        String sdCardPath = getSDCardPath();
        if (TextUtils.isEmpty(sdCardPath)) {
            if (listener != null) {
                listener.onError();
            }
            return;
        }
        Dialog dialog = new FileExplorerDialog(activity, sdCardPath, onlyDirectory, title, listener);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null) listener.onCancel();
            }
        });
        dialog.show();
    }


    private static String getSDCardPath() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) return null;
        return Environment.getExternalStorageDirectory().getPath();
    }

}
