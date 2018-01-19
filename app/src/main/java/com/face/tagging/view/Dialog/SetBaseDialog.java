package com.face.tagging.view.Dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.face.tagging.tagging.R;

/**
 * Created by zhoujie on 2018/1/9.
 */

public class SetBaseDialog extends DialogFragment implements View.OnClickListener {
    private SetBaseListener listener;
    private boolean showRemove;
    private TextView removeTv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.select_base_pop_up, container, true);
        init(view);
        return view;
    }

    public void setListener(SetBaseListener listener) {
        this.listener = listener;
    }

    private void init(View view) {
        view.findViewById(R.id.add_base).setOnClickListener(this);
        removeTv = view.findViewById(R.id.remove_base);
        removeTv.setOnClickListener(this);
        removeTv.setVisibility(showRemove ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_base:
                if (listener != null) listener.onAdd();
                break;
            case R.id.remove_base:
                if (listener != null) listener.onRemove();
                break;
        }
        dismiss();
    }

    public void setShowRemove(boolean show) {
        showRemove = show;
        if (removeTv != null) {
            removeTv.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public interface SetBaseListener {
        void onAdd();

        void onRemove();
    }
}
