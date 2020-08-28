package com.example.firebasechat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.firebasechat.MessageActivity;
import com.example.firebasechat.Models.Chat;
import com.example.firebasechat.Models.User;
import com.example.firebasechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context mCtx;
    private List<Chat> mChat;
    private String imageUrl;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context mCtx, List<Chat> mChat, String imageUrl) {
        this.mCtx = mCtx;
        this.mChat = mChat;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mCtx).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.MyViewHolder(view);
        } else {
            View view = LayoutInflater.from(mCtx).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.MyViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MyViewHolder holder, int position) {
        Chat chat = mChat.get(position);
        holder.show_msg.setText(chat.getMessage());


        if (chat.getMessage().equals("Image")) {
            Glide.with(mCtx).load(chat.getImageURL()).into(holder.sent_image);
            holder.show_msg.setVisibility(View.GONE);
        } else{
            if (imageUrl.equals("default") && !chat.getMessage().equals("Image"))
            {
                holder.profile_image.setImageResource(R.mipmap.ic_launcher);
            } else {
                Glide.with(mCtx).load(imageUrl).into(holder.profile_image);
                holder.sent_image.setVisibility(View.GONE);
            }
        }




        if (position == mChat.size()-1) {
            if (chat.isIsseen() && chat.getMessage().equals("Image"))
            {
                holder.txt_image_seen.setText("Seen");

            } else if (chat.isIsseen()) {
                holder.txt_seen.setText("Seen");
                holder.txt_image_seen.setVisibility(View.GONE);
                holder.sent_image.setVisibility(View.GONE);

            } else if (!chat.isIsseen() && chat.getMessage().equals("Image")){
                holder.txt_image_seen.setText("Delivered");

            }
            else{
                holder.txt_seen.setText("Delivered");
                holder.txt_image_seen.setVisibility(View.GONE);
                holder.sent_image.setVisibility(View.GONE);
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
            holder.txt_image_seen.setVisibility(View.GONE);

        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView show_msg;
        public CircularImageView profile_image;
        public TextView txt_seen,txt_image_seen;
        public ImageView sent_image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            show_msg = itemView.findViewById(R.id.show_msg);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            txt_image_seen = itemView.findViewById(R.id.txt_image_seen);
            sent_image = itemView.findViewById(R.id.sent_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }
}


