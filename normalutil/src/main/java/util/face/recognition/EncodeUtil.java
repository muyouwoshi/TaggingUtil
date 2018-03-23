package util.face.recognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v4.math.MathUtils;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by zhoujie on 2017/12/7.
 */

public class EncodeUtil {
    public static Bitmap readRGBImage(String filepath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);
        return bitmap;
    }

    public static byte[] readRGBImageToYuv(String filepath) {
        byte[] imgData = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);
        if (bitmap != null) {
            bitmap = adjustPhotoRotation(bitmap, 90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth() / 2 * 2, bitmap.getHeight() / 2 * 2);
            imgData = getNV21(bitmap.getWidth(), bitmap.getHeight(), bitmap);
        }
        return imgData;
    }

    public static Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        if (bm == null) {
            return null;
        }
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);

        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            return bm1;
        } catch (OutOfMemoryError ex) {
        }
        return null;
    }

    public static byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    public static Bitmap readYUVImage(String filePath, int width, int height) {
        return readYUVImage(readFile(filePath), width, height);
    }

    public static Bitmap readYUVImage(byte[] data, int width, int height) {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];
        if (data.length < (width * height * 3 / 2)) {
            return null;
        }

        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0, width, 0, 0, width, height);
        return bmp;
    }

    public static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0 && yuv420sp.length > uvIndex + 1) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }

    public static byte[] readFile(String file) {
        try {
            FileInputStream fin = new FileInputStream(file);

            int length = fin.available();

            byte[] buffer = new byte[length];
            fin.read(buffer);
            return buffer;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] irToYuv(byte[] irData, int width, int height) {
        if (irData.length < width * height * 2) {
            return null;
        }

        byte[] yuvData = new byte[width * height * 3 / 2];
        short[] source = new short[width * height];
        ByteBuffer.wrap(irData).order(ByteOrder.nativeOrder()).asShortBuffer().get(source);

        double sum = 0;
        double[] x_value = new double[width * height];
        for (int i = 0; i < x_value.length; i++) {
            x_value[i] = source[i] / 1023d;
            sum += x_value[i];
        }

        double mean = sum / x_value.length;

        double accum = 0;
        for (int i = 0; i < x_value.length; i++) {
            accum += (x_value[i] - mean) * (x_value[i] - mean);
        }
        double stdev = Math.sqrt(accum / (x_value.length - 1));

        double e_value = Math.max(Math.pow(10, -6), stdev);

        for (int i = 0; i < source.length / 2; i++) {
            double x = x_value[i];
            double d = x < 0 ? 0 : (x > 1 ? 1 : x);
            double result = 0.6 * d + 0.4 * ((x - mean) / e_value + 2) / 4;
            result = result < 0 ? 0 : (result > 1 ? 1 : result);
            yuvData[i] = (byte) (result * 255);
        }

        for (int i = width * height; i < yuvData.length; i++) {
            yuvData[i] = (byte) 128;
        }
        return yuvData;
    }

    public static Bitmap readIRImage(String filePath,int width,int height){
        return getIRImage(readFile(filePath),width,height);
    }

    public static Bitmap getIRImage(byte[] irData,int width,int height){
        if(irData.length<width*height*2)return null;
        int length = width*height;
        short[] source = new short[width * height];
//        int[] y = new int[width*height];
        byte[] yuv = new byte[width*height*3/2];
        ByteBuffer.wrap(irData).order(ByteOrder.nativeOrder()).asShortBuffer().get(source);
        for(int x = 0;x<length;x++){
            yuv[x] = irData[2*x];
        }

        for(int x = length;x<yuv.length;x++){
            yuv[x] = (byte)128;
        }
//
//        int[] rgba = new int[length];
//        for (int w = 0; w < width; w++)
//            for (int h = 0; h < height; h++) {
//                int y = (0xff & ((int) yuv[i * width + j]));
//                y = y < 16 ? 16 : y;
//                int r = Math.round(1.164f * (y - 16));
//                int g = Math.round(1.164f * (y - 16));
//                int b = Math.round(1.164f * (y - 16));
//
//                r = r < 0 ? 0 : (r > 255 ? 255 : r);
//                g = g < 0 ? 0 : (g > 255 ? 255 : g);
//                b = b < 0 ? 0 : (b > 255 ? 255 : b);
//
//                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
//            }
//
//        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        bmp.setPixels(rgba, 0, width, 0, 0, width, height);
//        return bmp;

        return readYUVImage(yuv,height,width);
    }

    public static byte[] rotateYUV240SP(byte[] src,int width,int height)
    {
        byte[] des = new byte[src.length];
        int wh = width * height;
        //旋转Y
        int k = 0;
        for(int i=0;i<width;i++) {
            for(int j=0;j<height;j++)
            {
                des[k] = src[width*j + i];
                k++;
            }
        }

        for(int i=0;i<width/2;i++) {
            for(int j=0;j<height/2;j++)
            {
                des[k] = src[wh+ width/2*j + i];
                des[k+width*height/4]=src[wh*5/4 + width/2*j + i];
                k++;
            }
        }
        return des;
    }

}
