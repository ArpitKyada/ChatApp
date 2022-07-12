package com.arpit_kyada.chat_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.arpit_kyada.chat_app.databinding.ActivitySignInBinding;
import com.arpit_kyada.chat_app.utility.Constants;
import com.arpit_kyada.chat_app.utility.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {
    ActivitySignInBinding binding;
    PreferenceManager pm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pm = new PreferenceManager(getApplicationContext());
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListners();
    }

    private void showToast(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidSignIn()
    {
        if(binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter the Email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString().trim()).matches()) {
            showToast("Enter valid Email");
            return false;
        }else if(binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter the Password");
            return false;
        }
        return true;
    }
    private void setListners() {
        binding.btnSingIn.setOnClickListener(v -> {
            if(isValidSignIn())
            {
                SignIn();
            }
        });
        binding.createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
            }
        });
    }

    private void SignIn() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString().trim())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult()!= null && task.getResult().getDocuments().size() > 0)
                    {
                        DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                        pm.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        pm.setString(Constants.KEY_USER_ID,snapshot.getId());
                        pm.setString(Constants.KEY_NAME,snapshot.getString(Constants.KEY_NAME));
                        pm.setString(Constants.KEY_IMAGE,snapshot.getString(Constants.KEY_IMAGE));

                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else {
                        showToast("Unable to Sign In");
                    }
                });
    }
}