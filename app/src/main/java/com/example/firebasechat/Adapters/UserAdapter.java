package com.example.firebasechat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder>
{
    private Context mCtx;
    private List<User> mUsers;
    private boolean isChat;
    String theLastMessage;
    public UserAdapter(Context mCtx, List<User> mUsers, boolean isChat) {
        this.mCtx = mCtx;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public UserAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.user_item,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.MyViewHolder holder, int position)
    {
        User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mCtx).load(user.getImageURL()).into(holder.profile_image);
        }

        if (isChat) {
            lastMessage(user.getId(),holder.last_msg);
        }
        else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (isChat) {
            if (user.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(mCtx, MessageActivity.class);
            intent.putExtra("userId",user.getId());
            mCtx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        public TextView username;
        public CircularImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);

        }
    }

    private void lastMessage(String userId, TextView last_msg) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                int unread = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Chat chat = snapshot1.getValue(Chat.class);

                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = chat.getMessage();
                    }
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()) {
                        unread++;
                    }

                }
                switch (theLastMessage) {

                    case "default":
                        last_msg.setText("");
                        break;
                    default:
                        if (unread == 0 ) {
                            last_msg.setText(theLastMessage);
                            last_msg.setTextColor(ContextCompat.getColor(mCtx, R.color.colorDarkGrey));
                        } else {
                            last_msg.setText(theLastMessage);
                            last_msg.setTypeface(Typeface.DEFAULT_BOLD);
                            last_msg.setTextColor(ContextCompat.getColor(mCtx, R.color.colorPrimaryDark));
                        }
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
