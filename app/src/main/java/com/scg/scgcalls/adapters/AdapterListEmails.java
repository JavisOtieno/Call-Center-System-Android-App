package com.scg.scgcalls.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.scg.scgcalls.R;
import com.scg.scgcalls.models.Email;
import com.scg.scgcalls.models.Message;

import java.util.ArrayList;
import java.util.List;

public class AdapterListEmails extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Email> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Email obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListEmails(Context context, List<Email> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListEmails(List<Email> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView email;
        public TextView date;
        public TextView message;
        public TextView subject;
        public View lyt_parent;
        public TextView contactName;
        public TextView amount;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            subject = (TextView) v.findViewById(R.id.phoneNumber);
            message = (TextView) v.findViewById(R.id.message);
            email = (TextView) v.findViewById(R.id.type);
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

            Email p = items.get(position);
            view.email.setText(p.emailto);
            view.message.setText(p.message);
            view.date.setText(p.date);
            view.subject.setText(p.subject);
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
