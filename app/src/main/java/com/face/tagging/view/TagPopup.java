package com.face.tagging.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.face.tagging.tagging.R;

/**
 * Created by zhoujie on 2018/1/9.
 */

public class TagPopup extends PopupWindow {
    Context context;
    public TagPopup(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.tag_pop_up,null);
        setContentView(view);
        setBackgroundDrawable(new ColorDrawable(0x00ffffff));
        setOutsideTouchable(true);
    }
}
