package util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhoujie on 2017/12/8.
 */

public class FileUtil {

    public static void saveBytesToFile(String fileName, byte[] data) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readEachLineToString(String filePath) {
        return readEachLineToString(filePath, false);
    }

    /**
     * @param filePath     路径
     * @param withoutBlank 是否去头尾的空格符
     * @return
     */
    public static String readEachLineToString(String filePath, boolean withoutBlank) {
        StringBuilder builder = new StringBuilder("");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String str;
            while ((str = reader.readLine()) != null) {
                builder.append(str.trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }


    public static void copyFile(String oldPath, String newPath, boolean keepSameNameFile)throws Exception {
        File newFile = new File(newPath);
        File parentFile = newFile.getParentFile();
        if(parentFile.exists() && parentFile.isDirectory()){
            File[] files = parentFile.listFiles();
            int n = 1;
            String perfix = newPath;
            String suffix = "";
            if(newPath.lastIndexOf(".") > 0){
                perfix = newPath.substring(0,newPath.lastIndexOf("."));
                suffix = newPath.replace(perfix,"");
            }
            while(hasSameName(files,newPath)){
                newPath = perfix+"("+String.valueOf(n)+")"+suffix;
                n++;
            }
        }
        copyFile(oldPath,newPath);
    }

    private static boolean hasSameName(File[] files, String newPath) {
        for(File file:files){
            if(file.getAbsolutePath().equals(newPath)){
                return true;
            }
        }
        return false;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     */
    public static void copyFile(String oldPath, String newPath) throws Exception {
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int bytesum = 0;
            int byteread;
            File oldfile = new File(oldPath);
            File newFile = new File(newPath);
            newFile.getParentFile().mkdirs();
            if (oldfile.exists()) { //文件存在时
                inStream = new FileInputStream(oldPath); //读入原文件
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
            }
        }catch (Exception e){
            throw e;
        }finally {
            if(inStream !=null){
                inStream.close();
            }
            if(fs != null){
                fs.close();
            }
        }
    }

    /**git
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static void copyFolder(String oldPath, String newPath) throws Exception {

        (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
        File a = new File(oldPath);
        String[] file = a.list();
        File temp;
        for (int i = 0; i < file.length; i++) {
            if (oldPath.endsWith(File.separator)) {
                temp = new File(oldPath + file[i]);
            } else {
                temp = new File(oldPath + File.separator + file[i]);
            }

            if (temp.isFile()) {
                FileInputStream input = new FileInputStream(temp);
                FileOutputStream output = new FileOutputStream(newPath + "/" + (temp.getName()));
                byte[] b = new byte[1024 * 5];
                int len;
                while ((len = input.read(b)) != -1) {
                    output.write(b, 0, len);
                }
                output.flush();
                output.close();
                input.close();
            }
            if (temp.isDirectory()) {//如果是子文件夹
                copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
            }
        }
    }

    /**
     * 删除相对路径
     */
    public static void deletePath(String path) {
        File file;
        file = new File(path);
        deleteFile(file);
    }

    /**
     * 删除文件夹中的缓存
     */
    public static void deleteCache(String directory) {
        File file = new File(directory);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File deleteFile : files) deleteFile(deleteFile);
        }
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public static void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                deleteFile(f);
            }
            file.delete();
        }
    }
}
