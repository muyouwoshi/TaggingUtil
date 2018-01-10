package com.face.tagging.moudle;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by zhoujie on 2018/1/8.
 */
public class FutureTestTest {
    FutureTest test;
    @Before
    public void setUp() throws Exception {
        test = new FutureTest();
    }

    @Test
    public void submit() throws Exception {
        test.submit();
    }

    @Test
    public void cancle() throws Exception {

        test.submit();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                test.cancle();
            }
        }).start();
    }

}