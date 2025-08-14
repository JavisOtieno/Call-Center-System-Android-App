package com.scg.scgcalls.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.scg.scgcalls.R;
import com.scg.scgcalls.models.Videocall;
import com.scg.scgcalls.models.Message;

import java.util.ArrayList;
import java.util.List;

public class AdapterListVideocalls extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Videocall> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Videocall obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListVideocalls(Context context, List<Videocall> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListVideocalls(List<Videocall> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView room;
        public TextView date;
        public TextView duration;
//        public TextView subject;
        public View lyt_parent;
        public TextView contactName;
        public TextView amount;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            room = (TextView) v.findViewById(R.id.phoneNumber);
//            message = (TextView) v.findViewById(R.id.message);
            duration = (TextView) v.findViewById(R.id.type);
            date = (TextView) v.findViewById(R.id.date);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
//            amount = (TextView) v.findViewById(R.id.amount);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            Videocall p = items.get(position);
            view.room.setText(p.room);
//            view.message.setText(p.message);
            view.date.setText(p.date);
            view.duration.setText(p.duration);
//            view.endlocation.setText(p.endlocation);
//            view.amount.setText(p.amount);
//            view.date.setText(p.phone);

            //Tools.displayImageRound(ctx, view.image, p.image);
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
