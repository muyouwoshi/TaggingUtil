package com.megvii.csp.explorer;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangyifei on 2017/4/25.
 */

public class FileExplorerDialog extends Dialog implements AdapterView.OnItemClickListener {
    private static final String TAG = FileExplorerDialog.class.getClass().getSimpleName();

    private TextView mCurrentPathView;
    private ListView mFileListView;
    private FileSelectListener mListener;
    private String mRootPath;
    private String mCurrentPath;
    private String mTitle;
    private boolean onlyDirectory;
    private MyAdapter mAdapter = new MyAdapter();
    private Map<String, int[]> mPosition = new HashMap<>();

    public FileExplorerDialog(Context context, String rootPath, boolean onlyDirectory, String title, FileSelectListener listener) {
        super(context,R.style.dialog_style);
        mListener = listener;
        mRootPath = mCurrentPath = rootPath;
        this.onlyDirectory = onlyDirectory;
        this.mTitle = title;
        initViews();
        updateList(mCurrentPath);
    }

    private void initViews() {
        setContentView(R.layout.file_list_dailog);

        mCurrentPathView = (TextView) findViewById(R.id.current_path);
        mFileListView = (ListView) findViewById(R.id.file_list);
        mFileListView.setOnItemClickListener(this);

        TextView textView = (TextView) findViewById(R.id.file_list_title);
        if (mTitle.length() > 0){
            textView.setText(mTitle);
        }else {
            textView.setText(onlyDirectory ? R.string.select_dir : R.string.select_file);
        }

    }

    private void updateList(String path) {
        String oldPath = mCurrentPath;
        mCurrentPath = path;
        mCurrentPathView.setText(mCurrentPath);
        mAdapter.scanFile();
        if (mFileListView.getAdapter() == null) {
            mFileListView.setAdapter(mAdapter);
        } else {
            int[] oldPosition = new int[2];
            oldPosition[0] = mFileListView.getFirstVisiblePosition();
            View firstChild = mFileListView.getChildAt(0);
            oldPosition[1] = firstChild == null ? 0 : firstChild.getTop();
            mPosition.put(oldPath, oldPosition);
            mAdapter.notifyDataSetChanged();

            int[] position = mPosition.get(mCurrentPath);
            if (position != null) {
                mFileListView.setSelectionFromTop(position[0], position[1]);
            } else {
                mFileListView.setSelection(0);
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String fileName = (String) mAdapter.getItem(position);
        final boolean isDirectory = new File(mCurrentPath, fileName).isDirectory();
        boolean isParent = fileName.equals("..");
        if (isParent){
            // 回到上一级
            updateList(new File(mCurrentPath).getParent());
        }else {
            if (!isDirectory && !onlyDirectory){
                dismiss();
                if (mListener != null){
                    mListener.onFileSelected(new File(mCurrentPath, fileName).getAbsolutePath());
                }
                return;
            }
            updateList(new File(mCurrentPath, fileName).getAbsolutePath());
        }
    }


    private class MyAdapter extends BaseAdapter {
        private List<String> mData;

        private void scanFile() {
            if (mData == null) {
                mData = new ArrayList<>();
            }
            mData.clear();
            if (!TextUtils.equals(mRootPath, mCurrentPath)) {
                mData.add(".."); // 上级目录
            }
            File file = new File(mCurrentPath);
            File[] children = file.listFiles(new FilenameFilter(){
                @Override
                public boolean accept(File dir, String name) {
                    // 过滤点隐藏文件
                    return !name.startsWith(".");
                }
            });

            if (children != null) {
                // 按照文件名进行排序，目录在前，文件名在后
                List<File> fileList = Arrays.asList(children);
                Collections.sort(fileList, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if (o1.isDirectory() && o2.isFile()){
                            return 1;
                        }

                        if (o1.isFile() && o2.isDirectory()) {
                            return -1;
                        }

                        return o1.getName().compareTo(o2.getName());
                    }
                });

                for (File child : fileList) {
                    if (onlyDirectory && !child.isDirectory()) continue;
                    mData.add(child.getName());
                }
            }
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(getContext(), R.layout.file_list_item, null);
                convertView.setTag(holder);
                holder.mFileNameView = (TextView) convertView.findViewById(R.id.file_path);
                holder.mSelectBtn = (TextView) convertView.findViewById(R.id.choose);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.updateView((String) getItem(position));
            return convertView;
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private class ViewHolder {
        TextView mFileNameView;
        TextView mSelectBtn;

        void updateView(final String fileName) {
            final boolean isParent = fileName.equals("..");
            if (isParent) {
                mFileNameView.setText("上一级");
                mSelectBtn.setVisibility(View.GONE);
            } else {
                mFileNameView.setText(fileName);
                mSelectBtn.setVisibility(View.VISIBLE);
            }

            mSelectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (mListener != null)
                        mListener.onFileSelected(new File(mCurrentPath, fileName).getAbsolutePath());
                }
            });

        }
    }
}
