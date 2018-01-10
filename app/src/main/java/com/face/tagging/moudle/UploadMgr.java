package com.face.tagging.moudle;
import android.util.Log;

import com.face.tagging.moudle.base.Config;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpProgressMonitor;

import java.util.List;
import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import util.file.FileUtil;
import util.file.TarUtils;
import util.sftp.SftpUtil;

/**
 * Created by zhoujie on 2018/1/9.
 */

public class UploadMgr {
    String TAG = UploadMgr.class.getSimpleName();
    SftpUtil defaultUtil;
    private volatile static UploadMgr instance;

    public static UploadMgr getInstance(){
        if(instance == null) {
            synchronized (UploadMgr.class) {
                if (instance == null) {
                    instance = new UploadMgr();
                }
            }
        }
        return instance;
    }

    private SftpUtil getUtil() throws Exception{
        if(defaultUtil == null || !defaultUtil.isConnect()){
            if(defaultUtil!=null){
                defaultUtil.disConnect();
            }
            defaultUtil = new SftpUtil(Config.SFTP_USER, Config.SFTP_HOST, Config.SSH_KEY_Path);
            defaultUtil.connect();
        }
        return  defaultUtil;
    }

    public void checkPath(final String path,final CheckPathCallback callback){
        Observable.create(new ObservableOnSubscribe<Void>() {
            @Override
            public void subscribe(ObservableEmitter<Void> e) throws Exception {
                SftpUtil util = null;
                try {
                    util = getUtil();
                    if (util.isConnect()) {
                        if(util.isDirExist(path)){
                            callback.success(util.ls(path));
                        }else{
                            String newPath = util.checkPath(Config.REMOTE_ROOT,path);
                            callback.failed(newPath,util.ls(newPath));
                        }
                    }else {
                        callback.failed(null,null);
                        Log.e(TAG, "checkPath 链接失败");
                    }
                } catch (Exception ex) {
                    callback.failed(null,null);
                    ex.printStackTrace();
                }
            }

        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void mkDir(final String dstDir, final MakeFileCallback callback) {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                SftpUtil util = null;
                try {
                    util = getUtil();
                    if (util.isConnect()) {
                        if(util.createDir(dstDir)){
                            e.onNext(true);
                            e.onComplete();
                            return;
                        }
                    }else {
                        Log.e(TAG, "checkPath 链接失败");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                e.onNext(false);
                e.onComplete();
            }

        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean success) throws Exception {
                if(success){
                    callback.success();
                }else {
                    callback.failed();
                }
            }
        });
    }

    public interface MakeFileCallback{
        void success();
        void failed();
    }

    public interface CheckPathCallback{
        void success(Vector<ChannelSftp.LsEntry> subFileList);
        void failed(String contains,Vector<ChannelSftp.LsEntry> subFileList);
    }

    public void compressAndUploadFile(final String filePath, final String dstDir ,final CopAndUploadCallback callback){
        compress(filePath, new CompressCallback() {
            @Override
            public void copFailed(Throwable e) {
                callback.copFailed(e);
            }

            @Override
            public void copSuccess() {
                callback.copSuccess();
                FileUtil.deleteCache(filePath);
                uploadFile(filePath+".tar",dstDir,callback,true);
            }
        });
    }

    private void uploadFile(final String filePath, final String dstDir,final UploadCallback callback, final boolean deleteFile) {
        Observable.create(new ObservableOnSubscribe<Void>() {
            @Override
            public void subscribe(ObservableEmitter<Void> e) throws Exception {
                SftpUtil util = null;
                try{
                    util = getUtil();
                    if(util.isConnect()){
                        callback.uploadStart();
                        util.upload(dstDir,filePath,new ProgressMonitor(){

                            @Override
                            public void upPrograss(int percent) {
                                callback.onPrograss(percent);
                            }

                            @Override
                            public void end() {
                                callback.uploadComplite();
                                super.end();
                            }
                        }, ChannelSftp.OVERWRITE);
                        if(deleteFile) {
                            FileUtil.deletePath(filePath);
                        }
                    }else{
                        callback.uploadFailed("连接失败");
                    }

                }catch (Exception ex){
                    ex.printStackTrace();
                    callback.uploadFailed("上传失败");
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void compress(final String filePath,final CompressCallback callback){
        Observable.create(new ObservableOnSubscribe<Void>() {
            @Override
            public void subscribe(ObservableEmitter<Void> e) throws Exception {
                try {
                    TarUtils.archive(filePath);
                    callback.copSuccess();
                } catch (Exception ex) {
                    callback.copFailed(ex);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public interface CompressCallback{
        void copFailed(Throwable e);
        void copSuccess();
    }

    public interface UploadCallback{
        void uploadStart();
        void onPrograss(int percent);
        void uploadComplite();
        void uploadFailed(String msg);
    }

    public interface CopAndUploadCallback extends CompressCallback,UploadCallback{

    }

    public static abstract class ProgressMonitor implements SftpProgressMonitor {
        long count = 0;
        long max = 0;
        /**
         * 当文件开始传输时，调用init方法。
         */
        public void init(int op, String src, String dest, long max) {
            this.max = max;
            count = 0;
            percent = 0;
        }
        private long percent = -1;
        /**
         * 当每次传输了一个数据块后，调用count方法，count方法的参数为这一次传输的数据块大小。
         */
        public boolean count(long count) {
            this.count += count;
            if (percent >= this.count * 100 / max) {
                return true;
            }
            percent = this.count * 100 / max;
            Log.e("bug11", "count: "+count );
            Log.e("bug11", "percent: "+percent );
            upPrograss((int)percent);
            return true;
        }

        public abstract void upPrograss(int percent);
        /**
         * 当传输结束时，调用end方法。
         */
        public void end() {

        }
    }

}
