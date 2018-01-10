package util.sftp;

import android.util.Log;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

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

    public SftpUtil(String user, String host, String sshKeyPath) {
        this.user = user;
        this.host = host;
        this.sshKeyPath = sshKeyPath;

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
                if (sshSession.isConnected()) {
                    return true;
                }
            }
        } catch (JSchException e) {
            throw e;
        }
        return false;
    }

    public void disConnect() {

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
        ChannelSftp channel = null;
        Vector<ChannelSftp.LsEntry> v = null;
        try {
            channel = getChannle();
            v = (Vector<ChannelSftp.LsEntry>) channel.ls(hostPath);
//            for(ChannelSftp.LsEntry entry : v){
//                Log.e("ls: ",hostPath +" -- " + entry.getFilename());
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        return v;
    }

    private ChannelSftp getChannle() throws Exception {
        ChannelSftp channel = (ChannelSftp) sshSession.openChannel("sftp"); // 打开SFTP通道
        channel.connect(channelTimeout); // 建立SFTP通道的连接
        if (channel.isConnected()) {
            return channel;
        }
        return null;
    }

    /**
     * 下载文件
     *
     * @param hostPath  下载目录
     * @param fileName  下载的文件
     * @param localPath 存在本地的路径
     */
    public void download(String hostPath, String fileName, String localPath) throws Exception {
        ChannelSftp channel = getChannle();
        mkdir(hostPath + "/" + fileName);
        channel.cd(hostPath);
        File file = new File(localPath, fileName);
        channel.get(fileName, new FileOutputStream(file));
        channel.disconnect();
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

    /**
     * 上传文件
     *
     * @param directory  上传的目录
     * @param uploadFile 要上传的文件
     */
    public synchronized void upload(String directory, String uploadFile, SftpProgressMonitor monitor, int mode) throws Exception {
//        channel.cd(directory);
//        mkdir(directory);
        ChannelSftp channel = (ChannelSftp) sshSession.openChannel("sftp"); // 打开SFTP通道
        channel.connect(channelTimeout); // 建立SFTP通道的连接
        if (channel.isConnected()) {
            channel.cd(directory);
            channel.put(uploadFile, directory, monitor, mode);
            channel.quit();
        }
    }


    private void mkdir(String directory) throws Exception {
        ChannelSftp channel = getChannle();
        try {
            mkdir(directory, channel);
        }catch (Exception e){
            channel.disconnect();
        }
        channel.disconnect();
    }

    private synchronized void mkdir(String directory, ChannelSftp channel) throws Exception {
        // 判断子目录文件夹是否存在，不存在即创建

        SftpATTRS attrs = null;
        try {
            attrs = channel.stat(directory);
        } catch (Exception e) {
            Log.e("mkdir: ", "directory" + directory);
        }
        if (attrs != null) {
            return;
        }
        mkdir(directory.substring(0, directory.lastIndexOf("/")));
        channel.mkdir(directory);
//        Log.e("mkdir：", directory);
    }

    /**
     * 删除文件
     *
     * @param directory  要删除文件所在目录
     * @param deleteFile 要删除的文件
     */
    public void delete(String directory, String deleteFile) throws Exception {
        ChannelSftp channel = getChannle();
        mkdir(directory);
        channel.cd(directory);
        channel.rm(deleteFile);
    }

    public boolean isConnect() {
        if (sshSession == null) return false;
        return sshSession.isConnected();
    }

    /**
     * 创建一个文件目录
     *
     * @param sftp
     * @author fengbo 20140226
     */
    public void createDir(String createpath, ChannelSftp sftp) throws Exception {
        if (isDirExist(createpath, sftp)) {
            sftp.cd(createpath);
        }
        String pathArry[] = createpath.split("/");
        StringBuffer filePath = new StringBuffer("/");
        for (String path : pathArry) {
            if (path.equals("")) {
                continue;
            }
            filePath.append(path + "/");
            if (isDirExist(filePath.toString(), sftp)) {
                sftp.cd(filePath.toString());
            } else {
                // 建立目录
                sftp.mkdir(filePath.toString());
                // 进入并设置为当前目录
                sftp.cd(filePath.toString());
            }

        }
        sftp.cd(createpath);
    }

    public boolean isDirExist(String directory, ChannelSftp sftp) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }


    public boolean isDirExist(String path) throws Exception {
        ChannelSftp channel = (ChannelSftp) sshSession.openChannel("sftp"); // 打开SFTP通道
        channel.connect(channelTimeout); // 建立SFTP通道的连接
        if (channel.isConnected()) {
            return isDirExist(path, channel);
        } else {
            throw new Exception("connect failed");
        }
    }

    public String checkPath(String root, String path) throws Exception{
        if (!path.startsWith(root)) return null;
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) sshSession.openChannel("sftp"); // 打开SFTP通道
            channel.connect(channelTimeout); // 建立SFTP通道的连接
            if (channel.isConnected()){
                if (isDirExist(root, channel)) {
                    String subPath = path.replace(root, "");
                    String pathArry[] = subPath.split("/");
                    StringBuffer filePath = new StringBuffer(root);
                    for (String path1 : pathArry) {
                        if (path1.equals("")) {
                            continue;
                        }
                        String exist = filePath.toString();
                        filePath.append(path + "/");
                        if (isDirExist(filePath.toString(), channel)) {
                            continue;
                        } else {
                            return exist;
                        }
                    }
                    return filePath.toString();
                }else{
                    return null;
                }
            } else{
                throw new Exception("connect failed");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
}
