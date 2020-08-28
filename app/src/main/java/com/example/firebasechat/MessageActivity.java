package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebasechat.Adapters.MessageAdapter;
import com.example.firebasechat.Retrofit.APIService;
import com.example.firebasechat.Models.Chat;
import com.example.firebasechat.Models.User;
import com.example.firebasechat.Retrofit.Client;
import com.example.firebasechat.Models.Data;
import com.example.firebasechat.Models.MyResponse;
import com.example.firebasechat.Models.Sender;
import com.example.firebasechat.Models.Token;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    CircularImageView profile_image;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    ImageButton btn_send,btn_select_image;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mChat;

    RecyclerView recyclerView;

    String userId;
    Intent intent;

    ValueEventListener seenListener;

    APIService apiService;
    boolean notify = false;

    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;


    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);


        recyclerView = findViewById(R.id.recycler_view_msg);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username_title);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        btn_select_image = findViewById(R.id.btn_select_image);
        storageReference = FirebaseStorage.getInstance().getReference("sentImage");
        intent = getIntent();
        userId = intent.getStringExtra("userId");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);


        btn_send.setOnClickListener(view -> {
            notify = true;

            String msg = text_send.getText().toString();

            if (!msg.equals("")) {
                sendMessage(firebaseUser.getUid(), userId, msg);
                Log.i("users", firebaseUser.getUid() + userId + msg);
            } else {
                Toast.makeText(this, "You can't send empty message", Toast.LENGTH_SHORT).show();
            }

            text_send.setText("");
        });


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }

                readMessage(firebaseUser.getUid(), userId, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        seenMessage(userId);


        btn_select_image.setOnClickListener(view -> {
            openImage();
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(MessageActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    private void openImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading....");
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference fileRef = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {

                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);


                        progressDialog.dismiss();
                        sendImage(firebaseUser.getUid(),userId,"Image",mUri);

                    } else {
                        Toast.makeText(MessageActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(MessageActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void seenMessage(String userId) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);

                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);

                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        databaseReference.child("Chats").push().setValue(hashMap);

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(userId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final String msg = message;

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, user.getUsername(), msg);
                    Log.i("users1", receiver + user.getUsername() + msg);

                }

                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("senderror", error.getMessage());

            }
        });
    }

    private void sendImage(String sender, String receiver, String message, String image) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("imageURL", image);
        hashMap.put("isseen", false);

        databaseReference.child("Chats").push().setValue(hashMap);

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(userId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final String msg = message;
        notify = true;
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, user.getUsername(), msg);
                    Log.i("users1", receiver + user.getUsername() + msg);

                }

                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("senderror", error.getMessage());

            }
        });
    }

    private void sendNotification(String receiver, String username, String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren())
                {
                    Token token = snapshot1.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(),String.valueOf(R.mipmap.ic_launcher),username + ": " + msg, "New Message",  userId);

                    Log.i("users2", firebaseUser.getUid()+" : " + username+" : "  + userId+" : "  + msg);
                    Sender sender = new Sender(token.getToken(),data);

                    Log.i("senderror", "token: " + token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response)
                                {
                                    Log.i("users3", "token: " + response.body().getResults().get(0));
                                    if (response.code() == 200) {
                                        if (response.body().getSuccess() == 1) {
                                            Toast.makeText(MessageActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t)
                                {
                                    Log.i("senderrorSend", t.getMessage());

                                }
                            });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readMessage(String myId, String userId, String imageUrl) {
        mChat = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Chat chat = snapshot1.getValue(Chat.class);
                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                        mChat.add(chat);
                    }

                        messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageUrl);
                        recyclerView.setAdapter(messageAdapter);



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void currentUser(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentUser", userId);
        editor.apply();
    }

    private void status(String status) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }
}