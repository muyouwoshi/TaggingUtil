package com.face.tagging.moudle;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import util.file.FileUtil;

/**
 * Created by zhoujie on 2018/1/8.
 */

public class FutureTest {
    ExecutorService service = Executors.newFixedThreadPool(3);
    Future futureTask;

    FutureTest() {

    }

    public void submit() {
        futureTask = service.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                FileUtil.copyFile("/Users/zhoujie/Desktop/test.zip","/Users/zhoujie/Desktop/test1.zip");
                return null;
            }
        });

    }

    public void cancle(){
        futureTask.cancel(true);
    }
}
