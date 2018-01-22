package com.face.tagging.view.fragment;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.face.tagging.moudle.CenterMgr;
import com.face.tagging.moudle.base.BaseSettingData;
import com.face.tagging.moudle.base.Config;
import com.face.tagging.tagging.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by zhoujie on 2018/1/9.
 */

public class BaseSettingFragment extends Fragment implements RadioGroup.OnCheckedChangeListener ,CompoundButton.OnCheckedChangeListener{
    EditText baseNameET,baseSaveDirET;
    RadioGroup angleGroup;
    CheckBox baseSaveOneCB,baseSaveTagCB;

    BaseSettingData baseSetData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.base_setting_fragment, container, false);
        initData();
        initView(view);
        return view;
    }

    private void initData() {
        baseSetData = CenterMgr.getinstance().getBaseSetting();
        baseSetData.baseSelectSet = Config.BASE_SAME_AS_IMAGE;
        baseSetData.saveAllInOne = false;
        baseSetData.saveWithTag = false;
    }

    private void initView(View view) {

        RadioGroup settingGroup = view.findViewById(R.id.base_setting);
        settingGroup.setOnCheckedChangeListener(this);
        baseNameET = view.findViewById(R.id.base_name);
        switch (baseSetData.baseSelectSet) {
            case Config.BASE_SIMPLE:
                settingGroup.check(R.id.base_manual);
                baseNameET.setVisibility(View.GONE);
                break;
            case Config.BASE_SAME_AS_IMAGE:
                settingGroup.check(R.id.same_as_image);
//                baseNameET.setVisibility(View.VISIBLE);
                break;
            case Config.BASE_ALL:
                settingGroup.check(R.id.all_base);
                baseNameET.setVisibility(View.GONE);
                break;
        }
        if (baseSetData.baseSelectName != null && !baseSetData.baseSelectName.matches("\\s*")) {
            baseNameET.setText(baseSetData.baseSelectName);
        }
        baseNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && !s.toString().matches("\\s*")) {
                    baseSetData.baseSelectName = s.toString();
                }
            }
        });

        angleGroup = view.findViewById(R.id.base_angle);
        angleGroup.setOnCheckedChangeListener(this);
        setAngle();

        baseSaveDirET = view.findViewById(R.id.base_dir_name_tv);
        baseSaveOneCB = view.findViewById(R.id.all_save_in_one);
        baseSaveTagCB = view.findViewById(R.id.save_with_tag);

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
                baseSetData.saveDirName = s.toString();
            }
        });

        baseSaveOneCB.setOnCheckedChangeListener(this);
        baseSaveTagCB.setOnCheckedChangeListener(this);
    }

    private void setSaveAllInOne() {
        if(baseSetData.isSaveAllInOne()){
            baseSaveOneCB.setChecked(true);
            baseSaveDirET.setVisibility(View.VISIBLE);
        }else {
            baseSaveOneCB.setChecked(false);
            baseSaveDirET.setVisibility(View.GONE);
        }
    }

    private void setSaveWithTag(){
        baseSaveTagCB.setChecked(baseSetData.isSaveWithTag());
    }

    private void setAngle() {
        switch (baseSetData.baseReangle) {
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
    public void onPause() {
        CenterMgr.getinstance().updataBaseSetting();
        super.onPause();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        SharedPreferences sp = getActivity().getSharedPreferences(Config.SP_SETTING, MODE_PRIVATE);

        switch (checkedId) {
            case R.id.base_manual:
                baseSetData.baseSelectSet = Config.BASE_SIMPLE;
                baseNameET.setVisibility(View.GONE);
                break;
            case R.id.same_as_image:
                baseSetData.baseSelectSet = Config.BASE_SAME_AS_IMAGE;
                baseNameET.setVisibility(View.VISIBLE);
                break;
            case R.id.all_base:
                baseNameET.setVisibility(View.GONE);
                baseSetData.baseSelectSet = Config.BASE_ALL;

                break;

            case R.id.angle_0:
                baseSetData.baseReangle = Config.BASE_ANGLE_0;
                break;
            case R.id.angle_90:
                baseSetData.baseReangle = Config.BASE_ANGLE_90;
                break;
            case R.id.angle_180:
                baseSetData.baseReangle = Config.BASE_ANGLE_180;
                break;
            case R.id.angle_270:

                baseSetData.baseReangle = Config.BASE_ANGLE_270;
                break;
            default:
                break;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.all_save_in_one:
                baseSetData.saveAllInOne = isChecked;
                if(isChecked) baseSaveDirET.setVisibility(View.VISIBLE);
                else baseSaveDirET.setVisibility(View.GONE);
                break;
            case R.id.save_with_tag:
                baseSetData.saveWithTag = isChecked;
                break;
        }
    }
}
