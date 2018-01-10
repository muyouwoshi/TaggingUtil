package com.face.tagging.view;

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
import android.widget.EditText;
import android.widget.RadioGroup;
import com.face.tagging.moudle.base.Config;
import com.face.tagging.tagging.R;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by zhoujie on 2018/1/9.
 */

public class BaseSettingFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    EditText baseName;
    int baseSet = Config.BASE_SIMPLE;
    SettingDialog.DialogDismissListener mListener;
    int baseAngle = Config.BASE_ANGLE_270;
    RadioGroup angleGroup;
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

            default:
                break;
        }
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

    private SharedPreferences getSharedPreferences() {
        return getActivity().getSharedPreferences(Config.SP_SETTING, MODE_PRIVATE);
    }

    interface DialogDismissListener {
        void onDismiss(int baseId, String baseName, int baseAngle);
    }
}
