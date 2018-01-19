package com.face.tagging.moudle.base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhoujie on 2018/1/15.
 */

public class BaseSettingData implements Parcelable {
    /**
     * 选择底库设置
     */
    public int baseSelectSet;
    /**
     * 选择底库的名称
     */
    public String baseSelectName;
    /**
     * 底库图片旋转角度
     */
    public int baseReangle;
    /**
     * 所有底库保存同一文件夹
     */
    public boolean saveAllInOne;
    /**
     *底库保存在tag文件夹
     */
    public boolean saveWithTag;

    /**
     * 保存底库文件夹的名称
     */
    public String saveDirName;

    public boolean saveJPG;

    public boolean saveYUV;


    public BaseSettingData() {
    }

    public int getBaseSelectSet() {
        return baseSelectSet;
    }

    public String getBaseSelectName() {
        return baseSelectName;
    }

    public int getBaseReangle() {
        return baseReangle;
    }

    public boolean isSaveAllInOne() {
        return saveAllInOne;
    }

    public boolean isSaveWithTag() {
        return saveWithTag;
    }

    public String getSaveDirName() {
        return saveDirName;
    }

    public void setBaseSelectSet(int baseSelectSet) {
        this.baseSelectSet = baseSelectSet;
    }

    public void setBaseSelectName(String baseSelectName) {
        this.baseSelectName = baseSelectName;
    }

    public void setBaseReangle(int baseReangle) {
        this.baseReangle = baseReangle;
    }

    public void setSaveAllInOne(boolean saveAllInOne) {
        this.saveAllInOne = saveAllInOne;
    }

    public void setSaveWithTag(boolean saveWithTag) {
        this.saveWithTag = saveWithTag;
    }

    public void setSaveDirName(String saveDirName) {
        this.saveDirName = saveDirName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.baseSelectSet);
        dest.writeString(this.baseSelectName);
        dest.writeInt(this.baseReangle);
        dest.writeByte(this.saveWithTag ? (byte) 1 : (byte) 0);
        dest.writeString(this.saveDirName);
        dest.writeByte(this.saveJPG ? (byte) 1 : (byte) 0);
        dest.writeByte(this.saveYUV ? (byte) 1 : (byte) 0);
        dest.writeByte(this.saveAllInOne ? (byte) 1 : (byte) 0);
    }

    protected BaseSettingData(Parcel in) {
        this.baseSelectSet = in.readInt();
        this.baseSelectName = in.readString();
        this.baseReangle = in.readInt();
        this.saveWithTag = in.readByte() != 0;
        this.saveDirName = in.readString();
        this.saveJPG = in.readByte() != 0;
        this.saveYUV = in.readByte() != 0;
        this.saveAllInOne = in.readByte() != 0;
    }

    public static final Creator<BaseSettingData> CREATOR = new Creator<BaseSettingData>() {
        @Override
        public BaseSettingData createFromParcel(Parcel source) {
            return new BaseSettingData(source);
        }

        @Override
        public BaseSettingData[] newArray(int size) {
            return new BaseSettingData[size];
        }
    };
}
