package com.face.tagging.tagging.moudle;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.face.tagging.tagging.Config;
import com.face.tagging.tagging.R;

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
    public TagAdapter(Context context){
        this.context = context;
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
    public void onBindViewHolder(Holder holder, final int position) {
        holder.tag.setText(tags.get(position));
        holder.tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener != null) clickListener.onClick(position,tags.get(position));
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
            tag = (TextView) itemView.findViewById(R.id.tag_item);
        }
    }

    public interface OnTagClickListener {
        void onClick(int position,String tag);
    }
}
