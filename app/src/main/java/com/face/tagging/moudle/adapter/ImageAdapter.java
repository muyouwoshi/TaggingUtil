package com.face.tagging.moudle.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.face.tagging.moudle.base.Config;
import com.face.tagging.tagging.R;
import com.face.tagging.view.Dialog.SaveBaseDialog;
import com.face.tagging.view.Dialog.SetBaseDialog;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder> implements com.face.tagging.moudle.TagAdapter.OnTagClickListener {

    private List<TagData> dataList = new CopyOnWriteArrayList<>();

    ConcurrentHashMap<Integer, Future> map = new ConcurrentHashMap<>();
    ExecutorService saveFileService = Executors.newFixedThreadPool(3);
    private int layoutWidth;
    private int angle = 270;

    Context context;
    RecyclerView recyclerView;
    float mScale = 0;
    private int startIndex, focusIndex = 1;
    int mCurrentItemOffset;

    private SelectBaseCallback selectBaseCallback;

    private FragmentManager fragmentManager;

    private Map<String,Integer> baseMap;

    public ImageAdapter(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        init();
    }

    void init() {
        focusIndex = 1;
        layoutWidth = recyclerView.getWidth();
        removeScrollListener();
        recyclerView.addOnScrollListener(new MyScrollListener());
    }

    private void removeScrollListener() {
        try {
            Field field = RecyclerView.class.getDeclaredField("mScrollListeners");
            field.setAccessible(true);
            List<RecyclerView.OnScrollListener> listeners = (List<RecyclerView.OnScrollListener>) field.get(recyclerView);
            if (listeners != null) {
                for (RecyclerView.OnScrollListener listener : listeners) {
                    if (listener instanceof MyScrollListener) {
                        recyclerView.removeOnScrollListener(listener);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSelectBaseCallback(SelectBaseCallback callback) {
        selectBaseCallback = callback;
    }

    private class MyScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            // dx>0则表示右滑, dx<0表示左滑, dy<0表示上滑, dy>0表示下滑
            mCurrentItemOffset += dx;
            computeCurrentItemPos();
            onScrolledChangedCallback();
        }
    }

    public void setFragmentManager(FragmentManager fm) {
        fragmentManager = fm;
    }

    private void onScrolledChangedCallback() {

        int index = startIndex;

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
        dataList = new CopyOnWriteArrayList<>();
        baseMap = new HashMap<>();

        for (int i = 0; i < length; i++) {
            TagData tagData = new TagData();
            tagData.file = data[i];
            tagData.originFile = data[i];
            dataList.add(tagData);
        }

        notifyDataSetChanged();
        mCurrentItemOffset = 0;
        startIndex = 0;
        focusIndex = 0;
    }

    public void nextAngle() {
        angle += 90;
        if (angle == 360) angle = 0;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == dataList.size() + 1) {
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
        if (position == 0 || position == dataList.size() + 1) {
//            holder.tagText.setVisibility(View.GONE);
        } else {
            holder.index = position;
            TagData tagData = dataList.get(position - 1);
            File file = tagData.file;

            String fileName = file.getName();
            String tag = tagData.tag;
            Bitmap bitmap = getBitmap(file);
            holder.imageView.setImageBitmap(bitmap);
            holder.textView.setText(fileName);
            if (tag != null) {
                holder.tagText.setText(tag);
                holder.tagText.setVisibility(View.VISIBLE);
            } else {
                holder.tagText.setVisibility(View.GONE);
            }
            holder.baseText.setVisibility(tagData.baseDir != null ? View.VISIBLE : View.GONE);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unTag(position);
                }
            });
            holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TagData tagData = dataList.get(position - 1);

                    if (tagData.tag != null) {
                        SetBaseDialog dialog = new SetBaseDialog();
                        dialog.setShowRemove(tagData.baseDir != null);
                        dialog.setListener(new SetBaseDialog.SetBaseListener() {
                            @Override
                            public void onAdd() {
                                addBase(position - 1);
                            }

                            @Override
                            public void onRemove() {
                                removeBase(position - 1);
                            }
                        });
                        if (fragmentManager != null) {
                            dialog.show(fragmentManager, "set_base_dialog");
                        }
                    }
                    return true;
                }
            });
        }
    }

    private void addBase(final int index) {
        SaveBaseDialog dialog = new SaveBaseDialog();

        final TagData tagData = dataList.get(index);
        final String newPath = Config.BASE_DIR + "/" + tagData.tag;
        final String oldPath = tagData.originFile.getAbsolutePath();

        dialog.setData(oldPath, newPath);
        dialog.setSaveCallback(new SaveBaseDialog.SaveBaseCallback() {
            @Override
            public void savedSuccess(String savePath) {
                tagData.baseDir = savePath;
                removeSameBase(index,savePath);
                refreshItem(index + 1);
                if(selectBaseCallback !=null){
                    selectBaseCallback.onBaseAdd(savePath);
                }
            }

            @Override
            public void saveFailed() {
                Toast.makeText(context, "保存底库失败", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(fragmentManager, "save_base_dialog");
    }

    /**
     * 移除相同路径下的相同底库
     * @param index 新的底库图片索引
     * @param savePath 新的底库图片保存路径
     */
    private void removeSameBase(int index,String savePath) {
        if(baseMap.get(savePath) != null) {
            int oldIndex = baseMap.get(savePath);
            TagData tagData = dataList.get(oldIndex);
            tagData.baseDir = null;
            refreshItem(oldIndex + 1);
        }

        baseMap.put(savePath, index);
    }

    private void removeBase(final int index) {
        if(selectBaseCallback !=null){
            selectBaseCallback.onBaseRemoved();
        }
        TagData tagData = dataList.get(index);
        File file = new File(tagData.baseDir);
        if (file.exists()) {
            file.delete();
        }
        tagData.baseDir = null;
        refreshItem(index + 1);

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
        return dataList.size() + 2;
    }


    @Override
    public void onClick(int position, String tag) {
        if (dataList == null || dataList.size() == 0) {
            return;
        }
        tag(focusIndex, tag);
    }


    public void tag(int index, String tag) {
        TagData tagData = dataList.get(index - 1);
        String oldTag = tagData.tag;
        if (oldTag == tag) {
            nextImage();
            return;
        }

        if (tagData.baseDir != null) {
            removeBase(index - 1);
        }

        tagData.tag = tag;
        refreshItem(index);
        copyFile(index - 1, oldTag);
        nextImage();
    }

    private synchronized void copyFile(final int index, final String oldTag) {
        Future future = map.get(index);
        final String tagFilePath = getTagFilePath(oldTag, dataList.get(index).file.getName());
        if (future != null) {
            future.cancel(true);
            if (future.isCancelled()) {
                if (oldTag != null) {
                    deleteTagFile(tagFilePath);
                }
            } else if (oldTag != null) {
                MsgMgr.getInstance().delay(new Runnable() {
                    @Override
                    public void run() {
                        deleteTagFile(tagFilePath);
                    }
                }, 1000);
            }
        } else if (oldTag != null) {
            deleteTagFile(tagFilePath);
        }
        map.replace(index, saveFileService.submit(new FileCallback(index)));
    }

    private String getTagFilePath(String tag, String fileName) {
        String newPath = Config.TAG_DIR + "/" + tag + "/" + "tag." + fileName;

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
        return newPath;
//        return Config.TAG_DIR + "/" + tag + "/" + tag+"_" + fileName;
    }

    private boolean hasSameName(File[] files, String newPath) {
        for(File file:files){
            if(file.getAbsolutePath().equals(newPath)){
                return true;
            }
        }
        return false;
    }

    private void deleteTagFile(String path) {
        File file = new File(path);
        if (file.exists()) file.delete();
    }

    public void unTag(final int index) {
        TagData tagData = dataList.get(index - 1);
        String oldTag = tagData.tag;
        tagData.tag = null;
        if (tagData.baseDir != null) {
            removeBase(index - 1);
        }
        refreshItem(index);
        copyFile(index - 1, oldTag);
    }

    class FileCallback implements Callable<Boolean> {
        final String tag;
        final File file;
        final String newPath;
        final String originPath;

        FileCallback(int index) {
            TagData tagData = dataList.get(index);
            tag = tagData.tag;
            file = tagData.file;
            newPath = getTagFilePath(tag, file.getName());
            originPath = tagData.originFile.getPath();
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
                FileUtil.copyFile(oldPath, newPath,true);
                return true;
            }
        }
    }

    private void refreshItem(final int index) {

        MsgMgr.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TagData tagData = dataList.get(index - 1);
                String tag = tagData.tag;
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
                        holder.baseText.setVisibility(tagData.baseDir != null ? View.VISIBLE : View.GONE);
                    }
                }
            }
        });
    }

    void nextImage() {
        recyclerView.smoothScrollBy(480, 0);
    }

    class Holder extends RecyclerView.ViewHolder {
        int index;
        ImageView imageView;
        TextView textView;
        TextView tagText, baseText;
        View view;

        public Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.tag_image);
            textView = itemView.findViewById(R.id.tag_name);
            tagText = itemView.findViewById(R.id.tag);
            baseText = itemView.findViewById(R.id.is_base_tv);
            if (imageView == null) {
                view = itemView.findViewById(R.id.blank);
            }
        }
    }

    private class TagData {
        File file;
        File originFile;
        String tag;
        String baseDir;
        String tagFile;
    }

    public interface SelectBaseCallback{
        void onBaseAdd(String path);
        void onBaseRemoved();
    }
}
