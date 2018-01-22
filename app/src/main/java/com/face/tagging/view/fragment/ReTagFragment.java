package com.face.tagging.view.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.face.tagging.moudle.CenterMgr;
import com.face.tagging.moudle.TagAdapter;
import com.face.tagging.moudle.adapter.ImageAdapter;
import com.face.tagging.moudle.base.BaseSettingData;
import com.face.tagging.moudle.base.Config;
import com.face.tagging.tagging.R;
import com.face.tagging.view.Dialog.BaseSettingDialog;
import com.face.tagging.view.Dialog.UploadPrograssDialog;
import com.megvii.csp.explorer.FileExplorer;
import com.megvii.csp.explorer.FileSelectListener;

import java.io.File;
import java.io.FilenameFilter;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import util.face.recognition.EncodeUtil;
import util.file.FileUtil;

/**
 * Created by zhoujie on 2017/12/29.
 */

public class ReTagFragment extends Fragment implements View.OnClickListener, TagAdapter.OntagOperatedListener {
    RecyclerView recyclerView, tagView;
    ImageView baseIamge;
    static final int SELECT_BASE = 0, SELECT_IAMGE = 1;
    Button button, baseSelectedBt;
    EditText tagText;

    TagAdapter tagAdapter;
    ImageAdapter imageAdapter;

    BaseSettingData baseSetBean;
    TextView baseNameTv;

    String basePath, imagesPath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.retag_fragment, container, false);
        initData();
        initView(view);
        return view;
    }

    private void initData() {
        baseSetBean = CenterMgr.getinstance().getBaseSetting();
    }

    private void initView(View view) {
        baseSelectedBt = view.findViewById(R.id.select_base_button);
        baseSelectedBt.setOnClickListener(this);
        view.findViewById(R.id.select_image_button).setOnClickListener(this);
        tagText = (EditText) view.findViewById(R.id.tag_add);
        button = (Button) view.findViewById(R.id.tag_button);
        button.setOnClickListener(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        tagView = (RecyclerView) view.findViewById(R.id.tags_view);
        baseIamge = (ImageView) view.findViewById(R.id.base_image);
        baseNameTv = view.findViewById(R.id.base_name);


        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(recyclerView);

        tagAdapter = new TagAdapter(getContext(), getChildFragmentManager());
        tagAdapter.setTagOperatedListener(this);
        tagView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        tagView.setAdapter(tagAdapter);


        view.findViewById(R.id.angle).setOnClickListener(this);
        view.findViewById(R.id.setting).setOnClickListener(this);

        if (baseSetBean.baseSelectSet == Config.BASE_SAME_AS_IMAGE) {
            baseSelectedBt.setVisibility(View.INVISIBLE);
        }
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
            case R.id.tag_button:
                showAdd();
                break;
            case R.id.angle:
                if (imageAdapter != null) imageAdapter.nextAngle();
                break;
            case R.id.setting:
                showSetting();
                break;
            default:
                break;
        }
    }

    private void showSetting() {
        BaseSettingDialog dialog = new BaseSettingDialog();
        dialog.setDismissListener(new BaseSettingDialog.DialogDismissListener() {
            @Override
            public void onDismiss() {
                switch (baseSetBean.baseSelectSet) {
                    case Config.BASE_SAME_AS_IMAGE:
                        showBase(imagesPath);
                        baseSelectedBt.setVisibility(View.INVISIBLE);
                        break;
                    case Config.BASE_SIMPLE:
                        baseIamge.setImageBitmap(null);
                        baseNameTv.setText("");
                        baseSelectedBt.setVisibility(View.VISIBLE);
                        break;
                    case Config.BASE_ALL:
                        baseIamge.setImageBitmap(null);
                        baseNameTv.setText("");
                        baseSelectedBt.setVisibility(View.VISIBLE);
                        showBase(basePath);
                        break;
                }
            }
        });
        dialog.show(getFragmentManager().beginTransaction(), "setting");
    }

    private void showImages(String filePath) {
        File[] imagesFile = new File(filePath).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File file = new File(dir, name);

                if (file.exists()) {
                    if (file.isDirectory()) {
                        return false;
                    }
                } else return false;
                return true;
            }
        });
        if (imagesFile == null || imagesFile.length == 0) {
            Toast.makeText(this.getContext(), "请重新选择", Toast.LENGTH_SHORT).show();

            return;
        }

        this.imagesPath = filePath;
        showBase(basePath);
        addImage(imagesFile);
        if (baseSetBean.baseSelectSet == Config.BASE_SAME_AS_IMAGE) showBase(imagesPath);
    }

    private void addImage(File[] imagesPath) {
        imageAdapter = new ImageAdapter(this.getContext(), recyclerView);
        imageAdapter.setData(imagesPath);
        tagAdapter.setClickListener(imageAdapter);
        recyclerView.setAdapter(imageAdapter);
        imageAdapter.setFragmentManager(getChildFragmentManager());
        imageAdapter.setSelectBaseCallback(new MySelectBaseCallback());
    }

    private void showBase(String filePath) {
        if (baseSetBean.baseSelectSet == Config.BASE_SIMPLE) {
            refreshBase(filePath);
        } else if (baseSetBean.baseSelectSet == Config.BASE_ALL) {
            refreshBase(filePath, imagesPath);
        } else if (baseSetBean.baseSelectSet == Config.BASE_SAME_AS_IMAGE) {
//            refreshBaseFromImage(filePath, baseSetBean.baseSelectName);

        }
    }

    private void refreshBase(String filePath, String imagesPath) {
        if (filePath != null && imagesPath != null) {
            File imagsFile = new File(imagesPath);
            String imagesBase = imagsFile.getName();
            File baseFile = new File(filePath, imagesBase);
            if (baseFile.exists() && baseFile.isFile()) {
                refreshBase(baseFile.getAbsolutePath());
            } else {
                Toast.makeText(getContext(), " 没有匹配的底库！", Toast.LENGTH_SHORT).show();
                baseIamge.setImageBitmap(null);
                baseNameTv.setText("");
                return;
            }
        }
    }

    private void refreshBaseFromImage(String filePath, String fileName) {
        if (filePath != null && fileName != null) {
            File baseFile = new File(filePath, fileName);
            if (baseFile.exists() && baseFile.isFile()) {
                refreshBase(baseFile.getAbsolutePath());
            } else {
                Toast.makeText(getContext(), " 没有底库！", Toast.LENGTH_SHORT).show();
                baseIamge.setImageBitmap(null);
                baseNameTv.setText("");
                return;
            }
        }
    }

    private void refreshBase(String filePath) {
        if (filePath == null) return;
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            Toast.makeText(getContext(), " 请选择有效的文件！", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap;
        String fileName = file.getName();

        if (file.getName().contains(".png") || file.getName().contains(".jpg")) {
            bitmap = EncodeUtil.readRGBImage(file.getAbsolutePath());
        } else if (!file.getName().contains(".DS_Store")) {
            bitmap = EncodeUtil.readYUVImage(file.getPath(), 640, 480);
            bitmap = EncodeUtil.adjustPhotoRotation(bitmap, baseSetBean.baseReangle * 90);
        } else {
            bitmap = null;
        }

        baseIamge.setImageBitmap(bitmap);
        baseNameTv.setText(fileName);
    }


    private void showAdd() {
        if (tagText.getVisibility() == View.VISIBLE) {
            tagText.setVisibility(View.GONE);
            tagAdapter.addTag(tagText.getText().toString());
            button.setText("新建标签");

        } else {
            tagText.setVisibility(View.VISIBLE);
            tagText.setText("");
            tagText.setFocusable(true);
            button.setText("确定");
        }
    }

    private void selectFile(MyFileSelectListener listener) {
        boolean onlyDir = true;
        if (listener.mode == SELECT_BASE && baseSetBean.baseSelectSet == Config.BASE_SIMPLE) {
            onlyDir = false;
        }
        FileExplorer.pickFile(getActivity(), onlyDir, listener);
    }

    @Override
    public void upload(int position, String tag) {
        UploadPrograssDialog prograssDialog = new UploadPrograssDialog();
        prograssDialog.setUploadFile(Config.TAG_DIR + "/" + tag, "");
        prograssDialog.show(getChildFragmentManager(), "upload_prograss");
    }


    class MyFileSelectListener implements FileSelectListener {
        int mode;

        MyFileSelectListener(int mode) {
            this.mode = mode;
        }

        @Override
        public void onFileSelected(String filePath) {
            if (mode == SELECT_BASE) {
                basePath = filePath;
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

    class MySelectBaseCallback implements ImageAdapter.SelectBaseCallback{

        @Override
        public void onBaseAdd(String path) {
            refreshBase(path);
        }

        @Override
        public void onBaseRemoved() {
            baseIamge.setImageBitmap(null);
            baseNameTv.setText("");
        }
    }
}
