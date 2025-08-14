package com.scg.scgcalls.adapters;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.scg.scgcalls.R;
import com.scg.scgcalls.models.Caller;
import com.scg.scgcalls.models.Caller;

import java.util.ArrayList;
import java.util.List;

public class AdapterListCallers extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Caller> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Caller obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListCallers(Context context, List<Caller> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListCallers(List<Caller> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView phone;
        public TextView date;
        public TextView name;
        public TextView email;
        public View lyt_parent;
        public TextView contactName;
        public TextView amount;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.callerName);
            phone = (TextView) v.findViewById(R.id.callerPhone);
            email = (TextView) v.findViewById(R.id.callerEmail);
//            endlocation = (TextView) v.findViewById(R.id.endlocation);
            date = (TextView) v.findViewById(R.id.date);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
//            type = (TextView) v.findViewById(R.id.type);
//            amount = (TextView) v.findViewById(R.id.amount);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_caller, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            Caller p = items.get(position);
            view.name.setText(p.name);
            view.phone.setText(p.phone);
            view.email.setText(p.email);
            view.date.setText(p.date);
            
//            view.type.setText(p.type);
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
