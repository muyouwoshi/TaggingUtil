package com.face.tagging.moudle;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.face.tagging.moudle.base.Config;
import com.face.tagging.tagging.R;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import util.face.recognition.EncodeUtil;
import util.file.FileUtil;
import util.observe.MsgMgr;

/**
 * Created by zhoujie on 2018/1/2.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder> implements TagAdapter.OnTagClickListener {
    private volatile List<File> files = new CopyOnWriteArrayList<>();
    private volatile File[] oringinFilePaths;
    private volatile String[] tags;
    ConcurrentHashMap<Integer, Future> map = new ConcurrentHashMap<>();
    ExecutorService saveFileService = Executors.newFixedThreadPool(3);
    private static int layoutWidth;
    private int angle = 270;

    Context context;
    RecyclerView recyclerView;
    float mScale = 0;
    private int startIndex, focusIndex = 1;
    int mCurrentItemOffset;

    public ImageAdapter(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        init();
    }

    void init() {
        focusIndex = 1;
        oringinFilePaths = new File[0];
        tags = new String[0];
        layoutWidth = recyclerView.getWidth();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // dx>0则表示右滑, dx<0表示左滑, dy<0表示上滑, dy>0表示下滑
                mCurrentItemOffset += dx;
                computeCurrentItemPos();
                onScrolledChangedCallback();
            }
        });
    }

    private void onScrolledChangedCallback() {

        int index = startIndex;
//        if (startIndex == 0) {
//            index = 1;
//        }

        View childView = recyclerView.getLayoutManager().findViewByPosition(index);
        if (childView == null) return;
        while (childView.getLeft() < layoutWidth) {
            int mid = childView.getLeft() + 240;
            int lmid = layoutWidth / 2;
            mid = Math.abs(lmid - mid);

            mScale = 1 - mid * 1.0f / (lmid * 3);

            childView.setScaleX(mScale);
            childView.setScaleY(mScale);

            index++;
            childView = recyclerView.getLayoutManager().findViewByPosition(index);
            if (childView == null) return;

        }

    }

    private void computeCurrentItemPos() {
        if (mCurrentItemOffset < 240) {
            startIndex = 0;
        } else {
            startIndex = (mCurrentItemOffset - 240) / 480 + 1;

        }
        View childView = recyclerView.getLayoutManager().findViewByPosition(startIndex);
        if (childView != null) {
            int childRight = childView.getRight();
            focusIndex = startIndex + (layoutWidth / 2 - 240 - childRight) / 480 + 1;
        }
    }


    public void setData(File[] data) {
        int length = data.length;
        files.clear();
        files.addAll(Arrays.asList(data));
        tags = new String[length];
        oringinFilePaths = new File[length];
        System.arraycopy(data, 0, oringinFilePaths, 0, length);
    }

    public void nextAngle(){
        angle += 90;
        if(angle == 360) angle = 0;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == files.size() + 1) {
            return 1;
        }
        return super.getItemViewType(position);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.tagging_blank_half_width_item, parent, false);
            View imageView = view.findViewById(R.id.blank);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.width = recyclerView.getWidth() / 2 - 240;
            imageView.setLayoutParams(params);
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.tagging_item, parent, false);
        }
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(Holder holder, final int position) {
        if (position == 0 || position == files.size() + 1) {
//            holder.tagText.setVisibility(View.GONE);
        } else {
            holder.index = position;
            File file = files.get(position - 1);

            String fileName = file.getName();
            String tag = tags[position - 1];
            Bitmap bitmap = getBitmap(file);
            holder.imageView.setImageBitmap(bitmap);
            holder.textView.setText(fileName);
            if (tag != null) {
                holder.tagText.setText(tag);
                holder.tagText.setVisibility(View.VISIBLE);
            } else {
                holder.tagText.setVisibility(View.GONE);
            }
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unTag(position);
                }
            });
        }
    }

    Bitmap getBitmap(File file) {
        Bitmap bitmap;
        if (!file.exists() || file.isDirectory()) {
            bitmap = null;
        } else if (file.getName().contains(".png") || file.getName().contains(".jpg")) {
            bitmap = EncodeUtil.readRGBImage(file.getAbsolutePath());
        } else if (!file.getName().contains(".DS_Store")) {
            bitmap = EncodeUtil.readYUVImage(file.getPath(), 640, 480);
            bitmap = EncodeUtil.adjustPhotoRotation(bitmap, angle);
        } else {
            bitmap = null;
        }
        return bitmap;
    }

    @Override
    public int getItemCount() {
        return files.size() + 2;
    }


    @Override
    public void onClick(int position, String tag) {
        if (files == null || files.size() == 0) {
            return;
        }
        tag(focusIndex, tag);
    }


    public void tag(int index, String tag) {
        String oldTag = tags[index - 1];
        if (oldTag == tag) {
            nextImage();
            return;
        }
        tags[index - 1] = tag;
        refreshItem(index);
//        resetFile(index-1);
        copyFile(index - 1, oldTag);
        nextImage();
    }

    private synchronized void copyFile(final int index, final String oldTag) {
        Future future = map.get(index);
        if (future != null) {
            future.cancel(true);
            if (future.isCancelled()) {
                if (oldTag != null) {
                    String path = Config.TAG_DIR + "/" + oldTag + "/" + files.get(index).getName();
                    deleteTagFile(path);
                }
            } else if (oldTag != null) {
                MsgMgr.getInstance().delay(new Runnable() {
                    @Override
                    public void run() {
                        String path = Config.TAG_DIR + "/" + oldTag + "/" + files.get(index).getName();
                        deleteTagFile(path);
                    }
                }, 1000);
            }
        }
        if (oldTag != null) {
            String path = Config.TAG_DIR + "/" + oldTag + "/" + files.get(index).getName();
            deleteTagFile(path);
        }
        map.replace(index, saveFileService.submit(new FileCallback(index)));
    }

    private void deleteTagFile(String path) {
        File file = new File(path);
        if (file.exists()) file.delete();
    }

    public void unTag(final int index) {
        String oldTag = tags[index -1];
        tags[index - 1] = null;
        refreshItem(index);
        copyFile(index - 1, oldTag);
//        resetFile(index-1);
    }

    class FileCallback implements Callable<Boolean> {
        final String tag;
        final File file;
        final String newPath;
        final String originPath;

        FileCallback(int index) {
            tag = tags[index];
            file = files.get(index);
            newPath = Config.TAG_DIR + "/" + tag + "/" + file.getName();
            originPath = oringinFilePaths[index].getPath();
        }

        @Override
        public Boolean call() throws Exception {

            if (tag == null) {
                File tagFile = new File(newPath);
                if (tagFile.exists()) {
                    tagFile.delete();
                }
                return true;
            } else {
                String oldPath = file.getAbsolutePath();
                FileUtil.copyFile(oldPath, newPath);
                return true;
            }
        }
    }

    private void refreshItem(final int index) {

        MsgMgr.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String tag = tags[index - 1];
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(index);
                if (viewHolder != null && viewHolder instanceof Holder) {
                    Holder holder = (Holder) viewHolder;
                    {
                        if (tag != null) {
                            holder.tagText.setText(tag);
                            holder.tagText.setVisibility(View.VISIBLE);
                        } else {
                            holder.tagText.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }

    synchronized void resetFile(int index) {
//        File file = files.get(index);
//        String tag = tags[index];
//        String newPath;
//        if (tag == null) {
//            newPath = oringinFilePaths[index].getAbsolutePath();
//        } else {
//            newPath = Config.TAG_DIR + "/" + tag + "/" + file.getName();
//        }
//        String oldPath = file.getAbsolutePath();
//
//        MyTask runTask = map.get(index);
//
//
//        if (runTask != null) {
//            String rNewpath = runTask.newPath;
//            String rOldPath = runTask.oldPath;
//            if (rNewpath == newPath && rOldPath == oldPath) {
//                return;
//            } else if(rNewpath == oldPath){
//                runTask.future.cancel(true);
//                map.remove(index);
//                try {
//                    FileUtil.deleteFile(new File(rNewpath));
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//            else {
//                if (runTask.future.isDone() && !runTask.future.isCancelled()) {
//                    runTask.future.cancel(true);
//                    final MyCallback task = new MyCallback(oldPath, newPath);
//                    map.remove(index);
//                    submit(task, index);
//                }
//            }
//        }
//        else{
//            final MyCallback task = new MyCallback(oldPath, newPath);
//            submit(task, index);
//        }
    }

    private void submit(final MyCallback task, final int index) {
//
//        Observable.create(new ObservableOnSubscribe<Boolean>() {
//            @Override
//            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
//                final Future<Boolean> tasks = saveFileService.submit(task);
//                map.putIfAbsent(index,new MyTask(task.oldPath,task.newPath,tasks));
//                boolean b = tasks.get();
//                e.onNext(b);
//                e.onComplete();
//            }
//        }).subscribeOn(Schedulers.io()).subscribe(new Observer<Boolean>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//
//            }
//
//            @Override
//            public void onNext(Boolean value) {
//                if (value) {
//                    resetFileSrc(index,task.newPath);
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });
    }

    public synchronized void resetFileSrc(int index, String path) {
        FileUtil.deleteFile(files.get(index));
        files.set(index, new File(path));
        map.remove(index);
    }


    class MyTask {

        String oldPath;
        String newPath;
        Future future;

        public MyTask(final String oldPath, final String newPath, Future future) {
            this.future = future;
            this.oldPath = oldPath;
            this.newPath = newPath;
        }

    }

    class MyCallback implements Callable<Boolean> {

        String oldPath;
        String newPath;

        public MyCallback(final String oldPath, final String newPath) {
            this.oldPath = oldPath;
            this.newPath = newPath;
        }

        @Override
        public Boolean call() throws Exception {
            boolean hasSave = false;
            try {
                File file = new File(oldPath);
                FileUtil.copyFile(file.getAbsolutePath(), newPath);
                FileUtil.deleteFile(file);
                hasSave = true;
            } catch (Exception e) {
                e.printStackTrace();
                hasSave = false;
            } finally {
                return hasSave;
            }
        }
    }


    void nextImage() {
        recyclerView.smoothScrollBy(480, 0);
    }

    class Holder extends RecyclerView.ViewHolder {
        int index;
        ImageView imageView;
        TextView textView;
        TextView tagText;
        View view;

        public Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.tag_image);
            textView = itemView.findViewById(R.id.tag_name);
            tagText = itemView.findViewById(R.id.tag);
            if (imageView == null) {
                view = itemView.findViewById(R.id.blank);
            }

        }
    }
}
