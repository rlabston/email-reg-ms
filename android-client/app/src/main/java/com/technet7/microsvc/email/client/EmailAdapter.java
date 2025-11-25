package com.technet7.microsvc.email.client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.ViewHolder> {

    private List<MainActivity.EmailItem> emails = new ArrayList<>();
    private String selectedEmail = null;
    private OnEmailClickListener listener;

    public interface OnEmailClickListener {
        void onEmailClick(String email);
    }

    public EmailAdapter(OnEmailClickListener listener) {
        this.listener = listener;
    }

    public void setEmails(List<MainActivity.EmailItem> emails) {
        this.emails = emails;
        notifyDataSetChanged();
    }

    public void setSelectedEmail(String email) {
        this.selectedEmail = email;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_email, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.EmailItem item = emails.get(position);
        holder.emailText.setText(item.email);
        holder.usernameText.setText(item.username);
        holder.dateText.setText(item.registrationDate);

        // Highlight selected item
        boolean isSelected = item.email.equals(selectedEmail);
        if (isSelected) {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary));
            holder.cardView.setAlpha(0.5f);
        } else {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.input_background));
            holder.cardView.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEmailClick(item.email);
            }
        });
    }

    @Override
    public int getItemCount() {
        return emails.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView emailText;
        TextView usernameText;
        TextView dateText;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            emailText = itemView.findViewById(R.id.emailText);
            usernameText = itemView.findViewById(R.id.usernameText);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }
}
