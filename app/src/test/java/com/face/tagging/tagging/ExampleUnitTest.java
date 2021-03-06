package com.face.tagging.tagging;

import android.content.Context;
import android.os.Environment;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import util.file.FileUtil;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void checkFile(){
        String name = "03-04-17_38_55.649_current_19_-1_-1.nv21.640.480";
        String newName = name.replace("current","last");
        String subStrring = newName.substring(newName.lastIndexOf("last")+4,newName.indexOf(".nv21"));
        Assert.assertEquals("_19_-1_-1",subStrring);
    }


    @Test
    public void set() {
        File[] files = new File("/Users/zhoujie/my_home/测试集/IR/xiaomi-20180208-image/query").listFiles();
        File[] bases = new File("/Users/zhoujie/my_home/测试集/IR/xiaomi-20180208-image/base").listFiles();

        for(File baseFile : bases){
            if(baseFile.isFile()) {
                System.out.println(baseFile.getAbsolutePath());
                continue;
            }
            if(baseFile.listFiles().length == 0){
                System.out.println(baseFile.getAbsolutePath());
                continue;
            }
            File base = baseFile.listFiles()[0];

            for(File file:files){
                if(file.getName().equals(baseFile.getName())){
                    try {
                        FileUtil.copyFile(base.getAbsolutePath(),file.getAbsolutePath()+"/register.png");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}