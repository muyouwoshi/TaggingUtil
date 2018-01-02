package util.sftp;

import android.util.Log;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by zhoujie on 2017/12/7.
 */

public class SftpUtil {
    private String user, host, sshKeyPath;
    private int sessionTimeout = 5000;
    private int channelTimeout = 5000;
    JSch jsch;
    Session sshSession = null;
    ChannelSftp channel;
    BlockingQueue<ChannelSftp> channelQueue;

    public SftpUtil(String user, String host, String sshKeyPath) {
        this.user = user;
        this.host = host;
        this.sshKeyPath = sshKeyPath;

        channelQueue = new LinkedBlockingQueue<>(4);
    }

    public boolean connect() throws JSchException {
        try {
            jsch = new JSch();
            jsch.addIdentity(sshKeyPath);
            sshSession = jsch.getSession(user, host);
            sshSession.setDaemonThread(true);
            if (sshSession != null) {
                sshSession.setConfig("StrictHostKeyChecking", "no");
                sshSession.connect(sessionTimeout);
                channel = (ChannelSftp) sshSession.openChannel("sftp"); // 打开SFTP通道
                channel.connect(channelTimeout); // 建立SFTP通道的连接
                if (channel.isConnected()) return true;
            }
        } catch (JSchException e) {
            throw e;
        }
        return false;
    }

    public void disConnect() {
        if (channel != null) {
            if (channel.isConnected()) {
                channel.disconnect();
            }
            channel = null;
        }
        if (sshSession != null) {
            if (sshSession.isConnected()) {
                sshSession.disconnect();
            }
            sshSession = null;
        }
    }

    /**
     * 列出目录下的文件
     *
     * @param hostPath 要列出的目录
     */
    public Vector<ChannelSftp.LsEntry> ls(String hostPath) {
        Vector<ChannelSftp.LsEntry> v = null;
        try {
            v = (Vector<ChannelSftp.LsEntry>) channel.ls(hostPath);
//            for(ChannelSftp.LsEntry entry : v){
//                Log.e("ls: ",hostPath +" -- " + entry.getFilename());
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    /**
     * 下载文件
     *
     * @param hostPath  下载目录
     * @param fileName  下载的文件
     * @param localPath 存在本地的路径
     */
    public void download(String hostPath, String fileName, String localPath) throws Exception {
        mkdir(hostPath + "/" + fileName);
        channel.cd(hostPath);
        File file = new File(localPath, fileName);
        channel.get(fileName, new FileOutputStream(file));
    }

    /**
     * 上传文件
     *
     * @param directory  上传的目录
     * @param uploadFile 要上传的文件
     */
    public synchronized void upload(String directory, String uploadFile) throws Exception {
        mkdir(directory);
        ChannelSftp channel = (ChannelSftp) sshSession.openChannel("sftp"); // 打开SFTP通道
        channel.connect(channelTimeout); // 建立SFTP通道的连接
        if (channel.isConnected()) {
            channel.cd(directory);
            File file = new File(uploadFile);
            channel.put(new FileInputStream(file), file.getName());
            channel.disconnect();
        }
    }

    private synchronized void mkdir(String directory) throws Exception {
        // 判断子目录文件夹是否存在，不存在即创建
        SftpATTRS attrs = null;
        try {
            attrs = channel.stat(directory);
        } catch (Exception e) {
            Log.e("mkdir: ", "directory"+directory);
        }
        if (attrs != null) return;
        mkdir(directory.substring(0,directory.lastIndexOf("/")));
        try{
            channel.mkdir(directory);
        }catch (Exception e){
            throw e;
        }

//        Log.e("mkdir：", directory);
    }

    /**
     * 删除文件
     *
     * @param directory  要删除文件所在目录
     * @param deleteFile 要删除的文件
     */
    public void delete(String directory, String deleteFile) throws Exception {
        mkdir(directory);
        channel.cd(directory);
        channel.rm(deleteFile);
    }

    public boolean isConnect() {
        if(sshSession == null || channel == null) return false;
        return channel.isConnected();
    }
}
