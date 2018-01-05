package com.face.tagging.tagging;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.face.tagging.tagging.moudle.ImageAdapter;
import com.face.tagging.tagging.moudle.TagAdapter;
import com.megvii.csp.explorer.FileExplorer;
import com.megvii.csp.explorer.FileSelectListener;

import java.io.File;
import java.io.FilenameFilter;

import util.face.recognition.EncodeUtil;

/**
 * Created by zhoujie on 2017/12/29.
 */

public class ReTagFragment extends Fragment implements View.OnClickListener {
    RecyclerView recyclerView, tagView;
    ImageView baseIamge;
    static final int SELECT_BASE = 0, SELECT_IAMGE = 1;
    Button button;
    EditText tagText;

//    File[] imagesPath;

    TagAdapter tagAdapter;
    ImageAdapter imageAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.retag_fragment, container, false);
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

        imageAdapter = new ImageAdapter(this.getContext(),recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(),LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(imageAdapter);
        new LinearSnapHelper().attachToRecyclerView(recyclerView);


        tagAdapter = new TagAdapter(getContext());
        tagAdapter.setClickListener(imageAdapter);
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
        File[] imagesPath = new File(filePath).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File file = new File(dir,name);
                if(file.exists()){
                    if(file.isDirectory()){
                        return false;
                    }
                }
                else return false;
                return true;
            }
        });
        if(imagesPath ==null || imagesPath.length == 0) {
            Toast.makeText(this.getContext(),"请重新选择",Toast.LENGTH_SHORT).show();

            return;
        }
        addImage(imagesPath);
    }

    private void addImage(File[] imagesPath) {
        imageAdapter = new ImageAdapter(this.getContext(),recyclerView);
        imageAdapter.setData(imagesPath);
        recyclerView.setAdapter(imageAdapter);
//        imageAdapter.notifyDataSetChanged();
    }

    private void showBase(String filePath) {
        File file = new File(filePath);
        if(!file.exists() || file.isDirectory()){
            Toast.makeText(getContext()," 请选择有效的文件！",Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap;
        String fileName = file.getName();

        if (file.getName().contains(".png") || file.getName().contains(".jpg")) {
            bitmap = EncodeUtil.readRGBImage(file.getAbsolutePath());
        } else if (!file.getName().contains(".DS_Store")) {
            bitmap = EncodeUtil.readYUVImage(file.getPath(), 640, 480);
            bitmap = EncodeUtil.adjustPhotoRotation(bitmap, 270);
        } else {
            bitmap = null;
        }

        baseIamge.setImageBitmap(bitmap);
        ((TextView)getView().findViewById(R.id.base_name)).setText(fileName);

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

    private void selectFile(MyFileSelectListener listener) {
        FileExplorer.pickFile(getActivity(), listener.mode == SELECT_IAMGE, listener);
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

}
