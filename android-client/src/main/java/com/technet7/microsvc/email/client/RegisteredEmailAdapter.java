package com.technet7.microsvc.email.client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RegisteredEmailAdapter extends RecyclerView.Adapter<RegisteredEmailAdapter.ViewHolder> {

    private final List<RegisteredEmailItem> items = new ArrayList<>();

    public void setItems(List<RegisteredEmailItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registered_email, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RegisteredEmailItem item = items.get(position);
        holder.emailText.setText(item.getEmail());
        holder.usernameText.setText(item.getUsername());
        holder.dateText.setText(item.getRegistrationDate());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView emailText;
        TextView usernameText;
        TextView dateText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            emailText = itemView.findViewById(R.id.emailText);
            usernameText = itemView.findViewById(R.id.usernameText);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }
}
