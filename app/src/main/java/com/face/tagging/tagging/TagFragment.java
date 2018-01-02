package com.face.tagging.tagging;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.megvii.csp.explorer.FileExplorer;
import com.megvii.csp.explorer.FileSelectListener;

/**
 * Created by zhoujie on 2017/12/29.
 */

public class TagFragment extends Fragment implements View.OnClickListener {
    RecyclerView recyclerView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.tag_fragment,container,false);
        view.findViewById(R.id.select_file_button).setOnClickListener(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.select_file_button:
                selectFile();
                break;
            default:
                break;
        }
    }

    private void selectFile() {
        FileExplorer.pickFile(getActivity(), true, new FileSelectListener() {
            @Override
            public void onFileSelected(String filePath) {

            }

            @Override
            public void onError() {

            }

            @Override
            public void onCancel() {

            }
        });
    }
}
