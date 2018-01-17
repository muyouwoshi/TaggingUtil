package com.face.tagging.moudle;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.face.tagging.moudle.base.Config;
import com.face.tagging.tagging.R;
import com.face.tagging.view.Dialog.TagDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoujie on 2018/1/3.
 */

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.Holder> {
    private List<String> tags = new ArrayList<>();
    private Context context;
    private OnTagClickListener clickListener;
    private OntagOperatedListener popupListener;
    private FragmentManager manager;
    public TagAdapter(Context context, FragmentManager manager){
        this.context = context;
        this.manager = manager;
    }

    public void addTag(String s,String basePath){
        if(s!=null && !s.startsWith(".") && !s.matches("\\s*")){
            if(!tags.contains(s)){
                tags.add(s);
                this.notifyDataSetChanged();
                String filePath = Config.TAG_DIR+"/"+s;
                File file = new File(filePath);
                file.mkdirs();
            }
        }
    }

    public void addTag(String s){
        if(s!=null && !s.startsWith(".") && !s.matches("\\s*")){
            if(!tags.contains(s)){
                tags.add(s);
                this.notifyDataSetChanged();
                String filePath = Config.TAG_DIR+"/"+s;
                File file = new File(filePath);
                file.mkdirs();
            }
        }
    }

    public void setTagOperatedListener(OntagOperatedListener listener){
        popupListener = listener;
    }


    public void setClickListener(OnTagClickListener listener){
        this.clickListener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.tags_item, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final Holder holder, final int position) {
        holder.tag.setText(tags.get(position));
        holder.tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener != null) clickListener.onClick(position,tags.get(position));
            }
        });
        holder.tag.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TagDialog dialog = new TagDialog();
                dialog.setListener(new TagDialog.TagPopupListener() {
                    @Override
                    public void delete() {
                        tags.remove(position);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void upload() {
                        if(popupListener !=null){
                            popupListener.upload(position,tags.get(position));
                        }
                    }
                });
                dialog.show(manager,"popup");
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        TextView tag;
        public Holder(View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.tag_item);
        }
    }

    public interface OnTagClickListener {
        void onClick(int position,String tag);
    }

    public interface OntagOperatedListener{
        void upload(int position,String tag);
    }
}
