package com.example.nesvie_copyzalo;

import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<ChatMessage> messageList;
    private String currentUserId;

    public ChatAdapter(List<ChatMessage> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        return message.getFromUser().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == VIEW_TYPE_SENT ?
                R.layout.item_message_sent : R.layout.item_message_received;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        // Nếu có ảnh
        if (message.getImageUri() != null && !message.getImageUri().isEmpty()) {
            holder.ivImage.setVisibility(View.VISIBLE);
            holder.tvMessage.setVisibility(View.GONE);

            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(message.getImageUri()))
                    .into(holder.ivImage);

        } else { // Tin nhắn text hoặc emoji
            holder.ivImage.setVisibility(View.GONE);
            holder.tvMessage.setVisibility(View.VISIBLE);

            // Hiển thị emoji nếu có (giả sử emoji là chuỗi text)
            holder.tvMessage.setText(message.getText());
            holder.tvMessage.setTypeface(null, Typeface.NORMAL);
            holder.tvMessage.setTextColor(0xFF000000);
        }

        // Hiển thị thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(message.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(ChatMessage message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        ImageView ivImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.txtMessage);
            tvTime = itemView.findViewById(R.id.txtTime);
            ivImage = itemView.findViewById(R.id.imgMessage);
        }
    }
}
