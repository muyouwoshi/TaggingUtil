package com.face.tagging.view.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.face.tagging.moudle.base.Config;
import com.face.tagging.tagging.R;
import com.face.tagging.view.fragment.BaseSettingFragment;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by zhoujie on 2018/1/8.
 */

public class BaseSettingDialog extends DialogFragment implements View.OnClickListener{
    DialogDismissListener mListener;
    private Fragment current,basesetFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.setting_dialog, container, false);
        init(view);
        return view;
    }


    private void init(View view) {
        view.findViewById(R.id.close).setOnClickListener(this);
        basesetFragment = new BaseSettingFragment();
        switchContent(basesetFragment);
    }

    @Override
    public void onStart() {
        getDialog().getWindow().setGravity(Gravity.CENTER);
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics display = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(display);
        getDialog().getWindow().setLayout(display.widthPixels, display.heightPixels);
        super.onStart();
    }

    public void setDismissListener(DialogDismissListener listener) {
        mListener = listener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mListener != null)
            mListener.onDismiss();
        super.onDismiss(dialog);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:

                dismiss();
                break;
            case R.id.setting_dialog_base_bt:

                break;
            default:
                break;
        }
    }



    public interface DialogDismissListener {
        void onDismiss();
    }

    /**
     * 切换当前显示的fragment
     */
    private void switchContent(Fragment fragment) {

        if (current != fragment) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            if (current != null) {
                transaction.hide(current);
            }
            //先判断是否被add过
            if (!fragment.isAdded()) {
                transaction.add(R.id.setting_content, fragment).commitAllowingStateLoss();
            } else {
                // 隐藏当前的fragment，显示下一个
                transaction.show(fragment).commitAllowingStateLoss();
            }
            getChildFragmentManager().executePendingTransactions();
            current = fragment;
        }
    }
}
