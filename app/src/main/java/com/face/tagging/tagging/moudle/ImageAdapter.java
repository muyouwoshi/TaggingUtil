package com.face.tagging.tagging.moudle;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.face.tagging.tagging.Config;
import com.face.tagging.tagging.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import util.face.recognition.EncodeUtil;
import util.file.FileUtil;
import util.observe.MsgMgr;

/**
 * Created by zhoujie on 2018/1/2.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder> implements TagAdapter.OnTagClickListener {
    private volatile static List<File> files = new ArrayList<>();
    private volatile static File[] oringinFilePaths;
    private volatile static String[] tags;
    Context context;
    RecyclerView recyclerView;
    float mScale = 0;
    int startIndex;
    int mCurrentItemOffset;

    public ImageAdapter(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        init();
    }

    void init() {
        oringinFilePaths = new File[0];
        tags = new String[0];
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
        int layoutWidth = recyclerView.getWidth();
        int index = startIndex;
        if (startIndex == 0) {
            index = 1;
        }


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
        if (mCurrentItemOffset < 240) startIndex = 0;
        else {
            startIndex = (mCurrentItemOffset - 240) / 480 + 1;
        }
    }


    public void setData(File[] data) {
        int length = data.length;
//        files = new ArrayList<>(length);
        files.clear();
        files.addAll(Arrays.asList(data));
        tags = new String[length];
        oringinFilePaths = new File[length];
        System.arraycopy(data, 0, oringinFilePaths, 0, length);
    }

    public void addData(File[] data) {
//        files.addAll(Arrays.asList(data));
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
            view = inflater.inflate(R.layout.tagging_item, parent, false);
            ImageView imageView = view.findViewById(R.id.tag_image);
            view.findViewById(R.id.tag).setVisibility(View.GONE);
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
            holder.tagText.setVisibility(View.GONE);
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
                    unTag(position - 1);
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
            bitmap = EncodeUtil.adjustPhotoRotation(bitmap, 270);
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
        tag(startIndex, tag);
    }


    public void tag(int index, String tag) {
        moveFile(index, files.get(index), tag);
        nextImage();
    }

    public void unTag(final int index) {
        Observable.create(new ObservableOnSubscribe<Void>() {
            @Override
            public void subscribe(ObservableEmitter<Void> e) throws Exception {
                try {
                    File file = files.get(index);
                    String filePath = oringinFilePaths[index].getAbsolutePath();
                    FileUtil.copyFile(file.getAbsolutePath(), filePath);
                    refreshItemData(index, filePath, null);
                    FileUtil.deleteFile(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    private void moveFile(final int index, final File file, final String tag) {
        Observable.create(new ObservableOnSubscribe<Void>() {
            @Override
            public void subscribe(ObservableEmitter<Void> e) throws Exception {
                try {
                    String filePath = Config.TAG_DIR + "/" + tag + "/" + file.getName();
                    FileUtil.copyFile(file.getAbsolutePath(), filePath);
                    refreshItemData(index, filePath, tag);
                    FileUtil.deleteFile(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    private synchronized void refreshItemData(final int index, String filePath, final String tag) {

        File file = new File(filePath);
        files.set(index, file);
        tags[index] = tag;
        refreshItem(index+1);
    }

    private void refreshItem(final int index){

        MsgMgr.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String tag = tags[index -1];
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

    void nextImage() {
        recyclerView.smoothScrollBy(480, 0);
    }

    class Holder extends RecyclerView.ViewHolder {
        int index;
        ImageView imageView;
        TextView textView;
        TextView tagText;

        public Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.tag_image);
            textView = itemView.findViewById(R.id.tag_name);
            tagText = itemView.findViewById(R.id.tag);
        }
    }
}
