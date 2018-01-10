package com.face.tagging.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.face.tagging.moudle.UploadMgr;
import com.face.tagging.moudle.base.Config;
import com.face.tagging.tagging.R;
import com.jcraft.jsch.ChannelSftp;

import java.io.File;
import java.util.Vector;

import util.observe.MsgMgr;

/**
 * Created by zhoujie on 2018/1/9.
 */

public class UploadPrograssDialog extends DialogFragment implements View.OnClickListener {

    String srcFile, dstDir;
    ProgressBar progressBar;
    TextView remotePathTv;
    RecyclerView remoteFileList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.upload_prograss_dialog, container, true);

        init(view);
        initData();

        return view;
    }

    private void initData() {
        dstDir = Config.getSetting(getContext()).getString(Config.SP_REMOTE_DIR, Config.REMOTE_ROOT);
        UploadMgr.getInstance().checkPath(dstDir, new UploadMgr.CheckPathCallback() {
            @Override
            public void success(Vector<ChannelSftp.LsEntry> subFileList) {
                refreshData();
            }

            @Override
            public void failed(String exist) {
                if (exist != null) {
                    dstDir = exist;
                    refreshData();
                }
            }
        });
    }

    private void refreshData() {
        if (dstDir == null) {
            Toast.makeText(getContext(), "没有可用路径，请手动上传", Toast.LENGTH_SHORT).show();
            dismiss();
        }
        remotePathTv.setText("远程存储路径：" + dstDir);
    }

    private void init(View view) {
        view.findViewById(R.id.cancel).setOnClickListener(this);
        progressBar = view.findViewById(R.id.upload_prograss);
        remoteFileList = view.findViewById(R.id.remote_file_list);
        remotePathTv = view.findViewById(R.id.remote_path_tv);
        view.findViewById(R.id.cancel).setOnClickListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    public void setUploadFile(String srcFile, String dstDir) {
        this.srcFile = srcFile;
        this.dstDir = dstDir;
    }

    @Override
    public void onStart() {
        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics display = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(display);
        getDialog().getWindow().setLayout(display.widthPixels, display.heightPixels);
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK) {
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
                progressBar.setVisibility(View.VISIBLE);
                startUpload();
                v.setEnabled(false);
                break;
            default:
                break;
        }
    }

    private  void startUpload(){
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
                    dismiss();
                }

                @Override
                public void uploadFailed(String msg) {
//                    Toast.makeText(getContext(),"上传文件出错",Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            });
        }
    }
}
