package com.arpit_kyada.chat_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.arpit_kyada.chat_app.adapter.UsersAdapter;
import com.arpit_kyada.chat_app.databinding.ActivityUsersBinding;
import com.arpit_kyada.chat_app.listeners.UserListener;
import com.arpit_kyada.chat_app.model.Users;
import com.arpit_kyada.chat_app.utility.Constants;
import com.arpit_kyada.chat_app.utility.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    ActivityUsersBinding binding;
    PreferenceManager pm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pm = new PreferenceManager(getApplicationContext());
        setListners();
        getUsers();

    }

    private void setListners() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }


    private void getUsers()
    {
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currUserId = pm.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null)
                    {
                        List<Users> usersList = new ArrayList<>();
                        for(QueryDocumentSnapshot snapshot : task.getResult())
                        {
                            if(!currUserId.equals(snapshot.getId())){
                                Users user = new Users();
                                user.name = snapshot.getString(Constants.KEY_NAME);
                                user.email = snapshot.getString(Constants.KEY_EMAIL);
                                user.image = snapshot.getString(Constants.KEY_IMAGE);
                                user.token = snapshot.getString(Constants.KEY_FCM_TOKEN);
                                user.id = snapshot.getId();
                                usersList.add(user);
                            }
                        }

                        if(usersList.size() > 0)
                        {
                            UsersAdapter adapter = new UsersAdapter(usersList, this);
                            binding.userRecyclerView.setAdapter(adapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        }else {
                            showErr();
                        }
                    }else {
                        showErr();
                    }
                });
    }

    private void showErr()
    {
        binding.textErrorMsg.setText(String.format("%s","No User Available"));
        binding.textErrorMsg.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(Users user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}