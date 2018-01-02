package com.face.tagging.tagging;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.face.tagging.tagging.moudle.ImageAdapter;
import com.megvii.csp.explorer.FileExplorer;
import com.megvii.csp.explorer.FileSelectListener;

/**
 * Created by zhoujie on 2017/12/29.
 */

public class TagFragment extends Fragment implements View.OnClickListener {
    RecyclerView recyclerView, tagView;
    ImageView baseIamge;
    static final int SELECT_BASE = 0, SELECT_IAMGE = 1;


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
        view.findViewById(R.id.tag_button).setOnClickListener(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        tagView = (RecyclerView) view.findViewById(R.id.tags_view);
        baseIamge = (ImageView) view.findViewById(R.id.base_image);
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
            case R.id.tag_add:
                showAdd();
                break;
            default:
                break;
        }
    }

    private void showImages(String filePath) {
        recyclerView.setAdapter(new ImageAdapter(filePath));
    }


    private void showBase(String filePath) {

    }


    private void showAdd() {

    }

    private void selectFile(FileSelectListener listener) {
        FileExplorer.pickFile(getActivity(), true, listener);
    }

    class MyFileSelectListener implements FileSelectListener {
        int mode;

        MyFileSelectListener(int mode) {

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
