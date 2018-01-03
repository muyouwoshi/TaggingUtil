package com.face.tagging.tagging.moudle;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by zhoujie on 2018/1/2.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder> {
    String[] filePaths;
    public ImageAdapter(String filePath) {
        filePaths = new File(filePath).list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if(file.isDirectory()){
                    return false;
                }else{
                    return true;
                }
            }
        });
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        return null;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class Holder extends RecyclerView.ViewHolder{
        String tag;
        String filePath;
        public Holder(View itemView) {
            super(itemView);
        }

//        void loadImage(ImageView imageView){
//
//            if (filePath.contains(".png") || filePath.contains(".jpg")) {
//                imageData = EncodeUtil.readRGBImageToYuv(file.getAbsolutePath());
//
//            } else {
//                imageData = EncodeUtil.readFile(file.getPath());
//            }
//
//            FutureTask<Bitmap> futureTask = new FutureTask<Bitmap>()
//        }


    }


}
