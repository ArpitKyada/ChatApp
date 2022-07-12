package com.arpit_kyada.chat_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.arpit_kyada.chat_app.adapter.RecentConversionAdapter;
import com.arpit_kyada.chat_app.databinding.ActivityMainBinding;
import com.arpit_kyada.chat_app.listeners.ConversionListener;
import com.arpit_kyada.chat_app.model.ChatMessage;
import com.arpit_kyada.chat_app.model.Users;
import com.arpit_kyada.chat_app.utility.Constants;
import com.arpit_kyada.chat_app.utility.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    ActivityMainBinding binding;
    PreferenceManager pm;
    private List<ChatMessage> chatList;
    private RecentConversionAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pm = new PreferenceManager(getApplicationContext());
        init();
        loadUserData();
        getToken();
        setListners();
        listenConversations();
    }

    private void init() {
        chatList = new ArrayList<>();
        adapter = new RecentConversionAdapter(chatList, this);
        binding.conversionRecyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
    }

    private void setListners() {
        binding.btnLogout.setOnClickListener(v -> {
            signOut();
        });
        binding.btnNewChat.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), UsersActivity.class));
        });
    }

    private void listenConversations()
    {
        db.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID,pm.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        db.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,pm.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null)
            return;
        if (value != null) {
            for (DocumentChange change : value.getDocumentChanges()) {
                if (change.getType() == DocumentChange.Type.ADDED) {
                    String senderId = change.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = change.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chat = new ChatMessage();
                    chat.senderId = senderId;
                    chat.receiverId = receiverId;
                    if (pm.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chat.conversionImg = change.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chat.conversionName = change.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chat.conversionId = change.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        chat.conversionImg = change.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chat.conversionName = change.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chat.conversionId = change.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    Log.d("BACDD", "ADDED : "+chat.message);
                    chat.message = change.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chat.dateTimeObj = change.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatList.add(chat);
                }
                else if(change.getType() == DocumentChange.Type.MODIFIED)
                {
                    for (int i=0 ; i< chatList.size() ; i++)
                    {
                        String senderId = change.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = change.getDocument().getString(Constants.KEY_RECEIVER_ID);

                        if(chatList.get(i).senderId.equals(senderId) && chatList.get(i).receiverId.equals(receiverId))
                        {
                            chatList.get(i).message = change.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            chatList.get(i).dateTimeObj = change.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            Log.d("BACDD", "MODIFIED : "+chatList.get(i).conversionName);
                            break;
                        }
                    }
                }
            }
            Collections.sort(chatList, (obj1,obj2) -> obj2.dateTimeObj.compareTo(obj1.dateTimeObj));
            adapter.notifyDataSetChanged();
            binding.conversionRecyclerView.smoothScrollToPosition(0);
            binding.conversionRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void signOut() {
        showToast("Signing Out ...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reference =
                db.collection(Constants.KEY_COLLECTION_USERS).document(pm.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        reference.update(map)
                .addOnSuccessListener(unused -> {
                    pm.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to SingOut"));

    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        pm.setString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reference =
                db.collection(Constants.KEY_COLLECTION_USERS).document(
                        pm.getString(Constants.KEY_USER_ID)
                );
        reference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to Update Token"));
    }

    private void loadUserData() {
        binding.textName.setText(pm.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.getDecoder().decode(pm.getString(Constants.KEY_IMAGE));
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imgProfile.setImageBitmap(bitmap);
    }

    @Override
    public void onConversionClicked(Users users) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,users);
        startActivity(intent);
    }
}