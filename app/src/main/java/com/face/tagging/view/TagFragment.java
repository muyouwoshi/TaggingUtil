package com.face.tagging.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.face.tagging.moudle.TagAdapter;
import com.face.tagging.moudle.base.Config;
import com.face.tagging.tagging.R;
import com.megvii.csp.explorer.FileExplorer;
import com.megvii.csp.explorer.FileSelectListener;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import util.face.recognition.EncodeUtil;
import util.file.FileUtil;

/**
 * Created by zhoujie on 2017/12/29.
 */

public class TagFragment extends Fragment implements View.OnClickListener, TagAdapter.OnTagClickListener {
    RecyclerView recyclerView, tagView;
    ImageView baseIamge, tagImage;
    static final int SELECT_BASE = 0, SELECT_IAMGE = 1;
    LinkedBlockingQueue<MyData> queue = new LinkedBlockingQueue<>(5);
    Button button;
    EditText tagText;
    TextView imageName;

    File[] iamgesPath;
    AtomicInteger count;

    TagAdapter tagAdapter;
    MyData data;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tag_fragment, container, false);
        initView(view);

        return view;
    }

    private void initView(View view) {
        view.findViewById(R.id.select_base_button).setOnClickListener(this);
        view.findViewById(R.id.select_image_button).setOnClickListener(this);
        tagText = (EditText) view.findViewById(R.id.tag_add);
        button = (Button) view.findViewById(R.id.tag_button);
        button.setOnClickListener(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        tagView = (RecyclerView) view.findViewById(R.id.tags_view);
        baseIamge = (ImageView) view.findViewById(R.id.base_image);
        tagImage = (ImageView) view.findViewById(R.id.tag_image);
        imageName = (TextView) view.findViewById(R.id.image_name);

        tagAdapter = new TagAdapter(getContext(),getChildFragmentManager());
        tagAdapter.setClickListener(this);
        tagView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        tagView.setAdapter(tagAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_base_button:
                selectFile(new MyFileSelectListener(SELECT_BASE));
                break;
            case R.id.select_image_button:
                selectFile(new MyFileSelectListener(SELECT_IAMGE));
                break;
            case R.id.tag_button:
                showAdd();
                break;
            default:
                break;
        }
    }

    private void showImages(String filePath) {
        queue.clear();
        iamgesPath = new File(filePath).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File file = new File(dir,name);
                if(file.exists()){
                    if(file.isDirectory()){
                        return false;
                    }
                }
                return true;
            }
        });
        if(iamgesPath==null || iamgesPath.length == 0) {
            Toast.makeText(this.getContext(),"请重新选择",Toast.LENGTH_SHORT).show();
            imageName.setText("空文件夹");
            return;
        }
        count = new AtomicInteger(0);
        data = null;
        addImage();
        nextImage();
    }

    private void addImage() {
        Observable.create(new ObservableOnSubscribe<Void>() {
            @Override
            public void subscribe(ObservableEmitter<Void> e) throws Exception {
                Bitmap imageData;
                synchronized (count) {
                    while (queue.size() < 5 && count.get()<iamgesPath.length) {
                        File file = iamgesPath[count.get()];
                        if(file.isDirectory()){
                            count.addAndGet(1);
                            continue;
                        }
                        if (file.getName().contains(".png") || file.getName().contains(".jpg")) {
                            imageData = EncodeUtil.readRGBImage(file.getAbsolutePath());
                        } else if (!file.getName().contains(".DS_Store")) {
                            imageData = EncodeUtil.readYUVImage(file.getPath(), 640, 480);
                            imageData = EncodeUtil.adjustPhotoRotation(imageData, 270);
                        } else {
                            count.addAndGet(1);
                            continue;
                        }
                        MyData data = new MyData(imageData,file);
                        queue.put(data);
                        count.addAndGet(1);
                    }
                }
            }
        }).subscribeOn(Schedulers.single()).subscribe();
    }

    private void showBase(String filePath) {

    }

    private void showAdd() {
        if (tagText.getVisibility() == View.VISIBLE) {
            tagText.setVisibility(View.GONE);
            tagAdapter.addTag(tagText.getText().toString());
            button.setText("新建标签");
        } else {
            tagText.setVisibility(View.VISIBLE);
            tagText.setText("");
            tagText.setFocusable(true);
            button.setText("确定");
        }
    }

    private void selectFile(FileSelectListener listener) {
        FileExplorer.pickFile(getActivity(), true, listener);
    }

    @Override
    public void onClick(int position, String tag) {
        try {
            File file = null;
            if(data != null){
                file =data.file;
            }
            nextImage();
            if(data == null) return;
            addImage();
            moveFile(file,tag);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moveFile(final File file,final  String tag) {
        Observable.create(new ObservableOnSubscribe<Void>() {
            @Override
            public void subscribe(ObservableEmitter<Void> e) throws Exception {
                try{
                    FileUtil.copyFile(file.getAbsolutePath(), Config.TAG_DIR+"/"+tag+"/"+file.getName());
                    FileUtil.deleteFile(file);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();


    }

    void nextImage() {
        if(data != null && queue.isEmpty()) {
            imageName.setText("已完成");
            tagImage.setImageBitmap(null);
            data.bitmap.recycle();
            return;
        }

        try {

           if(data == null) {
               data = queue.take();
               tagImage.setImageBitmap(data.bitmap);
               queue.remove(data);
           }
           else{
               data.bitmap.recycle();
               data = queue.take();
               tagImage.setImageBitmap(data.bitmap);
               queue.remove(data);

           }
            imageName.setText(data.file.getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    class MyFileSelectListener implements FileSelectListener {
        int mode;

        MyFileSelectListener(int mode) {
            this.mode = mode;
        }

        @Override
        public void onFileSelected(String filePath) {
            if (mode == SELECT_BASE) {
                showBase(filePath);
            } else if (mode == SELECT_IAMGE) {
                showImages(filePath);
            }
        }

        @Override
        public void onError() {

        }

        @Override
        public void onCancel() {

        }
    }

    class MyData{
        final Bitmap bitmap;
        final File file;
        MyData(Bitmap bitmap,File file){
            this.bitmap = bitmap;
            this.file = file;
        }
    }
}
