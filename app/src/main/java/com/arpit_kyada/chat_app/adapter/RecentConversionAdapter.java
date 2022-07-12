package com.arpit_kyada.chat_app.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arpit_kyada.chat_app.databinding.ItemContainerRecentConversionBinding;
import com.arpit_kyada.chat_app.listeners.ConversionListener;
import com.arpit_kyada.chat_app.model.ChatMessage;
import com.arpit_kyada.chat_app.model.Users;
import com.arpit_kyada.chat_app.utility.Cryptography;

import java.util.List;

public class RecentConversionAdapter extends RecyclerView.Adapter<RecentConversionAdapter.ConversionViewHolder>{
    private final List<ChatMessage> chatMessages;
    public final ConversionListener listener;
    public RecentConversionAdapter(List<ChatMessage> chatMessages, ConversionListener listener) {
        this.chatMessages = chatMessages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent,
                false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position),listener);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ConversionViewHolder extends RecyclerView.ViewHolder {

        ItemContainerRecentConversionBinding binding;


        public ConversionViewHolder(ItemContainerRecentConversionBinding itemBinding) {
            super(itemBinding.getRoot());
            binding = itemBinding;
        }

        private Bitmap getImgFromString(String str) {
            byte[] bytes = Base64.decode(str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        void setData(ChatMessage chatMessage,ConversionListener listener) {
            binding.imgProfile.setImageBitmap(getImgFromString(chatMessage.conversionImg));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(Cryptography.decript(chatMessage.message));
            binding.getRoot().setOnClickListener(v -> {
                Users u = new Users();
                u.image = chatMessage.conversionImg;
                u.id = chatMessage.conversionId;
                u.name = chatMessage.conversionName;
                listener.onConversionClicked(u);

            });
        }
    }
}
