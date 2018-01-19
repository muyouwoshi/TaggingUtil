package com.face.tagging.view.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.face.tagging.moudle.adapter.FileAdapter;
import com.face.tagging.tagging.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import util.file.FileUtil;
import util.observe.MsgMgr;

/**
 * Created by zhoujie on 2018/1/9.
 */

public class SaveBaseDialog extends DialogFragment implements View.OnClickListener, FileAdapter.OnItemClickLitener {

    String srcFile, dstDir, dstFileName;
    TextView filePathTv, createFileTv;
    RecyclerView fileListView;
    List<String> fileList,otherFileList;
    FileAdapter fileAdapter;
    EditText newFileName,baseFileName;

    SaveBaseCallback callback;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.save_base_dialog, container, false);

        init(view);
        initData();

        return view;
    }

    public void setSaveCallback(SaveBaseCallback callback){
        this.callback = callback;
    }

    public void setData(String srcFile,String dstPath){
        this.srcFile = srcFile;
        this.dstDir = dstPath.substring(0,dstPath.lastIndexOf("/"));
        this.dstFileName = dstPath.replace(dstDir+"/","");
    }

    private void init(View view) {
        fileListView = view.findViewById(R.id.remote_file_list);
        filePathTv = view.findViewById(R.id.remote_path_tv);
        view.findViewById(R.id.save_base_bt).setOnClickListener(this);
        fileListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        newFileName = view.findViewById(R.id.new_file_name);
        createFileTv = view.findViewById(R.id.new_file_tv);
        baseFileName = view.findViewById(R.id.base_file_name);
        baseFileName.setText(dstFileName);

        newFileName.setVisibility(View.INVISIBLE);
        createFileTv.setOnClickListener(this);

    }

    private void initData() {
        upDataFiles(dstDir);
    }

    private void upDataFiles(String path) {
        File file = new File(path);
        file.mkdirs();
        if(file.exists() && file.isDirectory()){
            File[] subFiles = file.listFiles();
            fileList = getDirFilesName(subFiles)[0];
            otherFileList = getDirFilesName(subFiles)[1];
            refreshData();
        }
    }

    private List<String>[] getDirFilesName(File[] subFileList) {
        List<String> dirFiles = new ArrayList<>();
        List<String> otherFiles = new ArrayList<>();
        if (subFileList != null) {
            dirFiles.add("返回上一层");
            for (File file : subFileList) {
                String fileName = file.getName();
                if (file.isDirectory()) {
                    if (!fileName.equals(".") && !fileName.equals("..")) {
                        dirFiles.add(fileName);
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
                fileAdapter = new FileAdapter(getContext());
                String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
                filePathTv.setText("底库另存为：" + dstDir.replace(sdcard,"sdcard")+"/");
                fileAdapter.setFileNames(fileList);
                fileListView.setAdapter(fileAdapter);
                fileAdapter.setItemClickLitener(SaveBaseDialog.this);
                //todo 存储路径保存
            }
        });

    }

    @Override
    public void onStart() {
        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
//        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics display = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(display);
        getDialog().getWindow().setLayout(display.widthPixels, display.heightPixels);

        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_base_bt:

                checkAndSave();

                break;
            case R.id.new_file_tv:
                resetCreateStates();
                break;
            default:
                break;
        }
    }

    private void checkAndSave() {
        String fileName =  baseFileName.getText().toString();
        if(fileName.matches("\\s*")){
            Toast.makeText(getContext(),"文件名不能为空",Toast.LENGTH_SHORT).show();
            return;
        }

        final String newPath = dstDir+"/" +fileName;

        File file = new File(newPath);
        if(file.exists() && file.isDirectory()){
            Toast.makeText(getContext(),"存在同名文件夹",Toast.LENGTH_SHORT).show();
            return;
        }

        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                    FileUtil.copyFile(srcFile, newPath);
                    e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
                if(callback !=null) {
                    callback.saveFailed();
                }
                dismiss();
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                if(callback!=null){
                    callback.savedSuccess(newPath);
                }
                dismiss();
            }
        });
    }

    private void resetCreateStates() {
        if (newFileName.getVisibility() == View.VISIBLE) {
            String fileName = newFileName.getText().toString().trim();
            if (!fileName.matches("\\s*") && !fileList.contains(fileName) && !otherFileList.contains(fileName)) {
                final String newPath = dstDir + "/" + fileName;
                File file = new File(newPath);

                boolean created = false;
                if(file.exists()) {
                    created = true;
                }
                else if(file.mkdirs()){
                    created = true;
                }

                if(created){
                    dstDir = newPath;
                    upDataFiles(dstDir);
                }else{
                    showToast("创建目录失败");
                }
            }
            newFileName.setVisibility(View.INVISIBLE);
            createFileTv.setText("新建");
        } else {
            newFileName.setText("");
            newFileName.setVisibility(View.VISIBLE);
            createFileTv.setText("确认");
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
            if (dstDir.equals(Environment.getDownloadCacheDirectory().getAbsolutePath())) {
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

    public interface SaveBaseCallback{
        void savedSuccess(String savePath);
        void saveFailed();
    }
}
