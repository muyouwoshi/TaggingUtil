package com.face.tagging.moudle.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.face.tagging.tagging.R;

import java.util.List;

/**
 * Created by zhoujie on 2018/1/10.
 */

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.Holder> {
    private Context context;
    private List<String> fileNames;
    private OnItemClickLitener itemClickLitener;

    public FileAdapter(Context context){
        this.context = context;
    }

    public void setFileNames(List<String> fileNames){
        this.fileNames = fileNames;
    }

    public void setItemClickLitener(OnItemClickLitener itemClickLitener) {
        this.itemClickLitener = itemClickLitener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.file_list_adapter_item,parent,false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(Holder holder,final int position) {
        holder.fileName.setText(fileNames.get(position));
        holder.fileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClickLitener !=null) itemClickLitener.onItemClick(position,fileNames.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        if(fileNames == null) return 0;
        return fileNames.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        TextView fileName;
        public Holder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
        }
    }

    public interface OnItemClickLitener{
        void onItemClick(int position,String fileName);
    }
}
