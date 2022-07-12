package com.arpit_kyada.chat_app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.arpit_kyada.chat_app.databinding.ActivitySignUpBinding;
import com.arpit_kyada.chat_app.utility.Constants;
import com.arpit_kyada.chat_app.utility.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    PreferenceManager pm;
    ActivitySignUpBinding binding;
    String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pm = new PreferenceManager(getApplicationContext());
        setListners();


    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void SignUp() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString().trim());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString().trim());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString().trim());
        user.put(Constants.KEY_IMAGE, encodedImage);

        db.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Loading(false);
                    pm.setString(Constants.KEY_USER_ID, documentReference.getId());
                    pm.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    pm.setString(Constants.KEY_NAME, binding.inputName.getText().toString().trim());
                    pm.setString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString().trim());
                    pm.setString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(this, "Failed to get Data", Toast.LENGTH_SHORT).show();
                });


    }

    private final ActivityResultLauncher<Intent> pickImg = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                Uri uri = result.getData().getData();
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    binding.profileImg.setImageBitmap(bitmap);
                    binding.addImgTV.setVisibility(View.GONE);
                    encodedImage = encodeImg(bitmap);
                } catch (Exception e) {

                }
            }
        }
    });

    private String encodeImg(Bitmap bitmap) {
        int prevW = 150;
        int prevH = bitmap.getHeight() * prevW / bitmap.getWidth();
        Bitmap b = Bitmap.createScaledBitmap(bitmap, prevW, prevH, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private Boolean isValidDetails() {
        if (encodedImage == null) {
            showToast("Select Profile Image");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter the Name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter the Name");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString().trim()).matches()) {
            showToast("Enter valid Email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter the Password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm the Password");
            return false;
        } else if (!binding.inputPassword.getText().toString().trim().equals(binding.inputConfirmPassword.getText().toString().trim())) {
            showToast("Password & Confirm Password must be same");
            return false;
        }
        return true;
    }

    private void Loading(boolean isLoading) {
        if (isLoading) {
            binding.btnSingUp.setVisibility(View.INVISIBLE);
            binding.progrssBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnSingUp.setVisibility(View.VISIBLE);
            binding.progrssBar.setVisibility(View.INVISIBLE);
        }
    }

    private void setListners() {
        binding.signIn.setOnClickListener(v -> onBackPressed());
        binding.btnSingUp.setOnClickListener(v -> {
            if (isValidDetails()) {
                SignUp();
            }
        });
        binding.layoutImg.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImg.launch(intent);
        });
    }
}