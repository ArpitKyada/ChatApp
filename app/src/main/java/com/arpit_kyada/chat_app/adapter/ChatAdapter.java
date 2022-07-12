package com.arpit_kyada.chat_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arpit_kyada.chat_app.databinding.ItemContailerReceivedMessageBinding;
import com.arpit_kyada.chat_app.databinding.ItemContailerSentMessageBinding;
import com.arpit_kyada.chat_app.databinding.ItemContainerReceivedImageBinding;
import com.arpit_kyada.chat_app.databinding.ItemContainerSentImageBinding;
import com.arpit_kyada.chat_app.model.ChatMessage;
import com.arpit_kyada.chat_app.utility.Constants;
import com.arpit_kyada.chat_app.utility.Cryptography;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    public Bitmap receverProfileImg;
    private String senderId;
    private Context context;

    public static int VIEW_TYPE_SENT = 1;
    public static int VIEW_TYPE_RECEIVED = 2;
    public static int VIEW_TYPE_SENT_IMG = 3;
    public static int VIEW_TYPE_RECEIVED_IMG = 4;

    public void setReceverProfileImg(Bitmap bitmap) {
        receverProfileImg = bitmap;
    }



    public ChatAdapter(Context context,List<ChatMessage> chatMessages, Bitmap receverProfileImg, String senderId) {
        this.chatMessages = chatMessages;
        this.receverProfileImg = receverProfileImg;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContailerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            return new ReceivedMessageViewHolder(
                    ItemContailerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else if (viewType == VIEW_TYPE_SENT_IMG) {
            return new SentImageViewHolder(
                    ItemContainerSentImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else {
            return new ReceivedImageViewHolder(
                    ItemContainerReceivedImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED) {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receverProfileImg);
        } else if (getItemViewType(position) == VIEW_TYPE_SENT_IMG) {
            ((SentImageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedImageViewHolder) holder).setData(chatMessages.get(position), receverProfileImg);
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            if (Cryptography.decript((chatMessages.get(position).message)) == null) {
                return VIEW_TYPE_SENT_IMG;
            }
            return VIEW_TYPE_SENT;
        } else {
            if (Cryptography.decript((chatMessages.get(position).message)) == null) {
                return VIEW_TYPE_RECEIVED_IMG;
            }
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        ItemContailerSentMessageBinding binding;

        public SentMessageViewHolder(ItemContailerSentMessageBinding sentMessageBinding) {
            super(sentMessageBinding.getRoot());
            binding = sentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(Cryptography.decript(chatMessage.message));
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        ItemContailerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContailerReceivedMessageBinding MessageBinding) {
            super(MessageBinding.getRoot());
            binding = MessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receverProfileImg) {
            binding.textMessage.setText(Cryptography.decript(chatMessage.message));
            binding.textDateTime.setText(chatMessage.dateTime);
            if (receverProfileImg != null) {
                binding.imgProfile.setImageBitmap(receverProfileImg);
            }
        }
    }

    static class SentImageViewHolder extends RecyclerView.ViewHolder {

        ItemContainerSentImageBinding binding;


        public SentImageViewHolder(ItemContainerSentImageBinding sentMessageBinding) {
            super(sentMessageBinding.getRoot());
            binding = sentMessageBinding;
        }

        public void openInGallery(Context context, Bitmap inImage) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "temp", null);

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(path), "image/*");
            context.startActivity(intent);
        }

        void setData(ChatMessage chatMessage) {
            byte[] bytes = Base64.getDecoder().decode(chatMessage.message);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.imgMessage.setImageBitmap(bitmap);
            binding.imgMessage.setOnClickListener(v -> openInGallery(itemView.getContext(),bitmap));
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder {

        ItemContainerReceivedImageBinding binding;

        public ReceivedImageViewHolder(ItemContainerReceivedImageBinding MessageBinding) {
            super(MessageBinding.getRoot());
            binding = MessageBinding;
        }
        public void openInGallery(Context context, Bitmap inImage) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "temp", null);

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(path), "image/*");
            context.startActivity(intent);
        }

        void setData(ChatMessage chatMessage, Bitmap receverProfileImg) {
            byte[] bytes = Base64.getDecoder().decode(chatMessage.message);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.imgMessage.setImageBitmap(bitmap);
            binding.imgMessage.setOnClickListener(v -> openInGallery(itemView.getContext(),bitmap));

            binding.textDateTime.setText(chatMessage.dateTime);
            if (receverProfileImg != null) {
                binding.imgProfile.setImageBitmap(receverProfileImg);
            }
        }
    }

}
