package com.arpit_kyada.chat_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.arpit_kyada.chat_app.adapter.ChatAdapter;
import com.arpit_kyada.chat_app.databinding.ActivityChatBinding;
import com.arpit_kyada.chat_app.model.ChatMessage;
import com.arpit_kyada.chat_app.model.Users;
import com.arpit_kyada.chat_app.network.ApiClient;
import com.arpit_kyada.chat_app.network.ApiService;
import com.arpit_kyada.chat_app.utility.Constants;
import com.arpit_kyada.chat_app.utility.Cryptography;
import com.arpit_kyada.chat_app.utility.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    ActivityChatBinding binding;
    private Users receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter adapter;
    private PreferenceManager pm;
    private FirebaseFirestore db;
    private String conversationId;
    private Boolean isAvailable = false;
    private int IMG_RES = 550;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        LoadReceiverData();
        setListeners();
        init();
        listenMessages();
    }

    private void init() {
        pm = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        adapter = new ChatAdapter(getApplicationContext(),chatMessages, getImgBitmap(receiverUser.image), pm.getString(Constants.KEY_USER_ID));
        db = FirebaseFirestore.getInstance();
        binding.chatRecyclerView.setAdapter(adapter);
    }

    private void sendMessage(String res) {

        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, pm.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        if (res == null) {
            if (binding.inputMessage.getText().toString().trim().length() <= 0) {
                return;
            }
            message.put(Constants.KEY_MESSAGE, Cryptography.encript(binding.inputMessage.getText().toString()));
        } else {
            message.put(Constants.KEY_MESSAGE, res);
        }
        message.put(Constants.KEY_TIMESTAMP, new Date());
        db.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversationId != null)
            updateConversion(Cryptography.encript(binding.inputMessage.getText().toString()));
        else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, pm.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, pm.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, pm.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, Cryptography.encript(binding.inputMessage.getText().toString()));
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!isAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);
                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, pm.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, pm.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, pm.getString(Constants.KEY_FCM_TOKEN));

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());

            } catch (Exception e) {

            }
        }
        binding.inputMessage.setText(null);
    }

    private void listenAvailability() {
        db.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null)
                return;
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABLE) != null) {
                    int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABLE)).intValue();
                    isAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                if (receiverUser.image == null) {
                    receiverUser.image = value.getString(Constants.KEY_IMAGE);
                    adapter.setReceverProfileImg(getImgBitmap(receiverUser.image));
                    adapter.notifyItemRangeChanged(0, chatMessages.size());
                }
            }
            if (isAvailable) {
                binding.txtAvailable.setVisibility(View.VISIBLE);
            } else {
                binding.txtAvailable.setVisibility(View.GONE);
            }
        });
    }

    private void showToast(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String msg) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                msg
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                            showToast("Notification Sent Successful");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    showToast("Error : " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

            }
        });
    }

    private void listenMessages() {
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, pm.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, pm.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null)
            return;
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTimeObj = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }

            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateTimeObj.compareTo(obj2.dateTimeObj));
            if (count == 0) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }

            binding.chatRecyclerView.setVisibility(View.VISIBLE);

        }
        binding.progrssBar.setVisibility(View.GONE);
        if (conversationId == null) {
            checkForConversion();
        }
    };

    private Bitmap getImgBitmap(String encodedImg) {
        if (encodedImg != null) {
            byte[] bytes = Base64.getDecoder().decode(encodedImg);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        return null;
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage(null));
        binding.layoutSendImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImg.launch(intent);
        });
    }

    private void LoadReceiverData() {
        receiverUser = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String, Object> map) {
        db.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .add(map)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversion(String msg) {
        DocumentReference reference = db.collection(Constants.KEY_COLLECTION_CONVERSATION).document(conversationId);
        reference.update(Constants.KEY_LAST_MESSAGE, msg, Constants.KEY_TIMESTAMP, new Date());
    }

    private void checkForConversion() {
        if (chatMessages.size() > 0) {
            checkForConversionRemotely(pm.getString(Constants.KEY_USER_ID), receiverUser.id);
            checkForConversionRemotely(receiverUser.id, pm.getString(Constants.KEY_USER_ID));
        }
    }

    private String encodeImg(Bitmap bitmap) {
        int prevW = IMG_RES;
        int prevH = bitmap.getHeight() * prevW / bitmap.getWidth();
        Bitmap b = Bitmap.createScaledBitmap(bitmap, prevW, prevH, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private final ActivityResultLauncher<Intent> pickImg = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                Uri uri = result.getData().getData();
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    sendMessage(encodeImg(bitmap));
                } catch (Exception e) {

                }
            }
        }
    });


    private void checkForConversionRemotely(String senderId, String receiverId) {
        db.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
            conversationId = snapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailability();
    }
}