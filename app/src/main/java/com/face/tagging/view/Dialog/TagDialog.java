package com.face.tagging.view.Dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.face.tagging.tagging.R;

/**
 * Created by zhoujie on 2018/1/9.
 */

public class TagDialog extends DialogFragment implements View.OnClickListener {
    private TagPopupListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tag_pop_up, container, true);
        init(view);
        return view;
    }

    public void setListener(TagPopupListener listener){
        this.listener = listener;
    }

    private void init(View view) {
        view.findViewById(R.id.delete).setOnClickListener(this);
        view.findViewById(R.id.upload).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete:
                if (listener != null) listener.delete();
                break;
            case R.id.upload:
                if (listener != null) listener.upload();
                break;
        }
        dismiss();
    }

    public interface TagPopupListener {
        void delete();

        void upload();
    }
}
