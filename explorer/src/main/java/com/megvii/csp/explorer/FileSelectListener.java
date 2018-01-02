package com.megvii.csp.explorer;

/**
 * Created by huangyifei on 2017/4/25.
 */

public interface FileSelectListener {
    void onFileSelected(String filePath);
    void onError();
    void onCancel();
}
