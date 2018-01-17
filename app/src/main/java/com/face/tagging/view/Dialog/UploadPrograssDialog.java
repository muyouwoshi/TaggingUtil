package com.face.tagging.view.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.face.tagging.moudle.UploadMgr;
import com.face.tagging.moudle.base.Config;
import com.face.tagging.moudle.base.FileAdapter;
import com.face.tagging.tagging.R;
import com.jcraft.jsch.ChannelSftp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import util.observe.MsgMgr;

/**
 * Created by zhoujie on 2018/1/9.
 */

public class UploadPrograssDialog extends DialogFragment implements View.OnClickListener, FileAdapter.OnItemClickLitener {

    String srcFile, dstDir;
    ProgressBar progressBar;
    TextView remotePathTv, createFileTv;
    RecyclerView remoteFileList;
    List<String> fileList;
    List<String> otherFileList;
    FileAdapter fileAdapter;

    volatile boolean isUpload,isCanUpload;
    EditText newFileName;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.upload_prograss_dialog, container, false);

        init(view);
        initData();

        return view;
    }


    private void init(View view) {
        progressBar = view.findViewById(R.id.upload_prograss);
        remoteFileList = view.findViewById(R.id.remote_file_list);
        remotePathTv = view.findViewById(R.id.remote_path_tv);
        view.findViewById(R.id.cancel).setOnClickListener(this);
        remoteFileList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        newFileName = view.findViewById(R.id.new_file_name);
        createFileTv = view.findViewById(R.id.new_file_tv);

        newFileName.setVisibility(View.INVISIBLE);
        createFileTv.setOnClickListener(this);
    }

    private void initData() {
        dstDir = Config.getSetting(getContext()).getString(Config.SP_REMOTE_DIR, Config.REMOTE_ROOT);
        upDataFiles(dstDir);
    }

    private void upDataFiles(String path) {
        isCanUpload = false;
        UploadMgr.getInstance().checkPath(path, new UploadMgr.CheckPathCallback() {
            @Override
            public void success(Vector<ChannelSftp.LsEntry> subFileList) {
                fileList = getDirFilesName(subFileList)[0];
                otherFileList = getDirFilesName(subFileList)[1];
                refreshData();
                isCanUpload = true;
            }

            @Override
            public void failed(String exist, Vector<ChannelSftp.LsEntry> subFileList) {
                dstDir = exist;
                fileList = getDirFilesName(subFileList)[0];
                otherFileList = getDirFilesName(subFileList)[1];
                refreshData();
                isCanUpload = true;
            }
        });
    }

    private List<String>[] getDirFilesName(Vector<ChannelSftp.LsEntry> subFileList) {
        List<String> dirFiles = new ArrayList<>();
        List<String> otherFiles = new ArrayList<>();
        if (subFileList != null) {
            dirFiles.add("返回上一层");
            for (ChannelSftp.LsEntry entry : subFileList) {
                String fileName = entry.getFilename();
                if (entry.getAttrs().isDir()) {
                    fileName = entry.getFilename();
                    if (!fileName.equals(".") && !fileName.equals("..")) {
                        dirFiles.add(entry.getFilename());
                    } else {
                        otherFiles.add(fileName);
                    }
                } else {
                    otherFiles.add(fileName);
                }
            }
        }
        List[] lists = new List[]{dirFiles, otherFiles};
        return lists;
    }

    private void refreshData() {
        MsgMgr.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dstDir == null) {
                    Toast.makeText(getContext(), "没有可用路径，请手动上传", Toast.LENGTH_SHORT).show();
                    dismiss();
                    return;
                }
                fileAdapter = new FileAdapter(getContext());
                remotePathTv.setText("远程存储路径：" + dstDir);
                fileAdapter.setFileNames(fileList);
                remoteFileList.setAdapter(fileAdapter);
                fileAdapter.setItemClickLitener(UploadPrograssDialog.this);
                Config.getSetting(getContext()).edit().putString(Config.SP_REMOTE_DIR, dstDir).apply();
            }
        });

    }

    public void setUploadFile(String srcFile, String dstDir) {
        this.srcFile = srcFile;
        this.dstDir = dstDir;
    }

    @Override
    public void onStart() {
        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
//        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics display = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(display);
        getDialog().getWindow().setLayout(display.widthPixels, display.heightPixels);
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK && isUpload) {
                    Toast.makeText(getContext(), "正在上传，请稍等", Toast.LENGTH_SHORT);
                    return true;
                }
                return false;
            }
        });
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                if(!isCanUpload) {
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                startUpload();
                v.setEnabled(false);
                isUpload = true;
                remoteFileList.setVisibility(View.INVISIBLE);
                break;
            case R.id.new_file_tv:
                resetCreateStates();
                break;
            default:
                break;
        }
    }

    private void resetCreateStates() {
        if (newFileName.getVisibility() == View.VISIBLE) {
            isCanUpload = false;
            String fileName = newFileName.getText().toString().trim();
            if (!fileName.matches("\\s*") && !fileList.contains(fileName) && !otherFileList.contains(fileName)) {
                final String newPath = dstDir + "/" + fileName;
                UploadMgr.getInstance().mkDir(newPath, new UploadMgr.MakeFileCallback() {
                    @Override
                    public void success() {
                        dstDir = newPath;
                        upDataFiles(dstDir);
                        isCanUpload = true;
                    }

                    @Override
                    public void failed() {
                        isCanUpload = true;
                        showToast("创建目录失败");
                    }
                });
            }
            newFileName.setVisibility(View.INVISIBLE);
            createFileTv.setText("新建");
        } else {
            newFileName.setText("");
            newFileName.setVisibility(View.VISIBLE);
            createFileTv.setText("确认");
        }
    }

    private void startUpload() {
        if (srcFile != null && dstDir != null) {
            if (!new File(srcFile).exists()) {
                Toast.makeText(getContext(), "文件不存在", Toast.LENGTH_SHORT).show();
                dismiss();
            }
            UploadMgr.getInstance().compressAndUploadFile(srcFile, dstDir, new UploadMgr.CopAndUploadCallback() {
                @Override
                public void copFailed(Throwable e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "压缩文件出错", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                @Override
                public void copSuccess() {

                }

                @Override
                public void uploadStart() {
                    progressBar.setMax(100);
                }

                @Override
                public void onPrograss(int percent) {
                    progressBar.setProgress(percent);
                }

                @Override
                public void uploadComplite() {
                    isUpload = false;
                    showToast("上传成功", true);
                }

                @Override
                public void uploadFailed(String msg) {
                    isUpload = false;
                    showToast("上传失败", true);
                }
            });
        }
    }

    private void showToast(String msg) {
        showToast(msg, false);
    }

    private void showToast(final String msg, final boolean close) {
        MsgMgr.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                if (close) dismiss();
            }
        });
    }

    @Override
    public void onItemClick(int position, String fileName) {
        if (position == 0 && fileName.equals("返回上一层")) {
            if (dstDir.equals(Config.REMOTE_ROOT)) {
                Toast.makeText(getContext(), "当前目录为顶层目录", Toast.LENGTH_SHORT).show();
            } else {
                dstDir = dstDir.substring(0, dstDir.lastIndexOf("/"));
                upDataFiles(dstDir);
            }
        } else {
            dstDir = dstDir + "/" + fileName;
            upDataFiles(dstDir);
        }
    }
}
