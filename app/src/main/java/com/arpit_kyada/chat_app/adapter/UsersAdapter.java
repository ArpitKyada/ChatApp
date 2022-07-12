package com.arpit_kyada.chat_app.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arpit_kyada.chat_app.databinding.ItemContainerUserBinding;
import com.arpit_kyada.chat_app.listeners.UserListener;
import com.arpit_kyada.chat_app.model.Users;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{

    private final List<Users> users;
    private final UserListener userListener;

    public UsersAdapter(List<Users> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        ItemContainerUserBinding binding;
        public UserViewHolder(ItemContainerUserBinding itembinding) {
            super(itembinding.getRoot());

            binding = itembinding;
        }
        void setUserData(Users user)
        {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imgProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }

    private Bitmap getUserImage(String encodedImg)
    {
        byte[] bytes = Base64.decode(encodedImg,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
     }

}
