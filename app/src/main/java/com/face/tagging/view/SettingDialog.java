package com.face.tagging.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
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

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by zhoujie on 2018/1/8.
 */

public class SettingDialog extends DialogFragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    EditText baseName,baseSaveDirET;
    int baseSet = Config.BASE_SIMPLE;
    DialogDismissListener mListener;
    int baseAngle = Config.BASE_ANGLE_270;
    RadioGroup angleGroup;

    CheckBox baseSaveOneCB,baseSaveTagCB;
    boolean saveWithTag,saveAllInOne;
    String baseSaveDir = "base";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.base_setting_dialog, container, true);
        init(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onStart() {
        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics display = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(display);
        getDialog().getWindow().setLayout(display.widthPixels, display.heightPixels);
        super.onStart();
    }

    public void setDismissListener(DialogDismissListener listener) {
        mListener = listener;
    }

    private void init(View view) {

        RadioGroup settingGroup = view.findViewById(R.id.base_setting);
        settingGroup.setOnCheckedChangeListener(this);
        view.findViewById(R.id.close).setOnClickListener(this);
        baseName = view.findViewById(R.id.base_name);
        baseSet = getSharedPreferences().getInt(Config.SP_BASE_SETTING, Config.BASE_SIMPLE);
        switch (baseSet) {
            case Config.BASE_SIMPLE:
                settingGroup.check(R.id.base_manual);
                baseName.setVisibility(View.GONE);
                break;
            case Config.BASE_SAME_AS_IMAGE:
                settingGroup.check(R.id.same_as_image);
                baseName.setVisibility(View.VISIBLE);
                break;
            case Config.BASE_ALL:
                settingGroup.check(R.id.all_base);
                baseName.setVisibility(View.GONE);
                break;
        }
        String base_name = getSharedPreferences().getString(Config.SP_BASE_NAME, null);
        if (base_name != null && !base_name.matches("\\s*")) {
            baseName.setText(base_name);
        }
        baseName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && !s.toString().matches("\\s*")) {
                    getSharedPreferences().edit().putString(Config.SP_BASE_NAME, s.toString()).commit();
                }
            }
        });

        angleGroup = view.findViewById(R.id.base_angle);
        angleGroup.setOnCheckedChangeListener(this);
        baseAngle = getSharedPreferences().getInt(Config.SP_BASE_ANGLE, Config.BASE_ANGLE_270);
        setAngle();

        baseSaveDirET = view.findViewById(R.id.base_dir_name_tv);
        baseSaveOneCB = view.findViewById(R.id.all_save_in_one);
        baseSaveTagCB = view.findViewById(R.id.save_with_tag);

        SharedPreferences sp = Config.getSetting(getContext());
        saveAllInOne = sp.getBoolean(Config.SAVE_ALL_IN_ONE,false);
        saveWithTag = sp.getBoolean(Config.SAVE_WITH_TAG,false);
        baseSaveDir = sp.getString(Config.SP_SAVE_BASE_NAME,"base");
        setSaveAllInOne();
        setSaveWithTag();

        baseSaveDirET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                baseSaveDir = s.toString();
            }
        });

        baseSaveOneCB.setOnCheckedChangeListener(this);
        baseSaveTagCB.setOnCheckedChangeListener(this);
    }

    private void setSaveAllInOne() {
        if(saveAllInOne){
            baseSaveOneCB.setChecked(true);
            baseSaveDirET.setVisibility(View.VISIBLE);
        }else {
            baseSaveOneCB.setChecked(false);
            baseSaveDirET.setVisibility(View.GONE);
        }
    }

    private void setSaveWithTag(){
        baseSaveTagCB.setChecked(saveWithTag);
    }

    private void setAngle() {
        switch (baseAngle) {
            case Config.BASE_ANGLE_0:
                angleGroup.check(R.id.angle_0);
                break;
            case Config.BASE_ANGLE_90:
                angleGroup.check(R.id.angle_90);
                break;
            case Config.BASE_ANGLE_180:
                angleGroup.check(R.id.angle_180);
                break;
            case Config.BASE_ANGLE_270:
                angleGroup.check(R.id.angle_270);
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:

                dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        getSharedPreferences().edit().putString(Config.SP_SAVE_BASE_NAME,baseSaveDir)
                .putBoolean(Config.SAVE_WITH_TAG,saveWithTag)
                .putBoolean(Config.SAVE_ALL_IN_ONE,saveAllInOne).apply();

        BaseSetBean baseSetBean = new BaseSetBean();
        baseSetBean.baseSet = baseSet;
        baseSetBean.baseName = baseName.getText().toString();
        baseSetBean.baseAngle = baseAngle;
        baseSetBean.saveAllInOne = saveAllInOne;
        baseSetBean.saveWithTag = saveWithTag;
        baseSetBean.saveDirName = baseSaveDir;
        if (mListener != null)
            mListener.onDismiss(baseSetBean);
        super.onDismiss(dialog);
    }

    public static class  BaseSetBean{
        public int baseSet;
        public String baseName;
        public int baseAngle;
        public boolean saveAllInOne;
        public boolean saveWithTag;
        public String saveDirName;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        SharedPreferences sp = getActivity().getSharedPreferences(Config.SP_SETTING, MODE_PRIVATE);

        switch (checkedId) {
            case R.id.base_manual:
                baseSet = Config.BASE_SIMPLE;
                baseName.setVisibility(View.GONE);
                break;
            case R.id.same_as_image:
                baseSet = Config.BASE_SAME_AS_IMAGE;
                baseName.setVisibility(View.VISIBLE);
                break;
            case R.id.all_base:
                baseName.setVisibility(View.GONE);
                baseSet = Config.BASE_ALL;

                break;

            case R.id.angle_0:
                baseAngle = Config.BASE_ANGLE_0;
                break;
            case R.id.angle_90:
                baseAngle = Config.BASE_ANGLE_90;
                break;
            case R.id.angle_180:
                baseAngle = Config.BASE_ANGLE_180;
                break;
            case R.id.angle_270:

                baseAngle = Config.BASE_ANGLE_270;
                break;
            default:
                break;
        }
        sp.edit().putInt(Config.SP_BASE_SETTING, baseSet).putInt(Config.SP_BASE_ANGLE,baseAngle).commit();
    }

    @Override
    public void show(FragmentManager manager, String tag) {

        super.show(manager, tag);
    }

    private SharedPreferences getSharedPreferences() {
        return getActivity().getSharedPreferences(Config.SP_SETTING, MODE_PRIVATE);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.all_save_in_one:
                    saveAllInOne = isChecked;
                    if(saveAllInOne) baseSaveDirET.setVisibility(View.VISIBLE);
                    else baseSaveDirET.setVisibility(View.GONE);
                break;
            case R.id.save_with_tag:
                    saveWithTag = isChecked;
                break;
        }
    }

    interface DialogDismissListener {
        void onDismiss(BaseSetBean baseSet);
    }
}
