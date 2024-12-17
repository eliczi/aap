// DateAdapter.java
package com.example.aap.ui.meals;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aap.R;
import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    public interface OnDateClickListener {
        void onDateClick(String date);
    }

    private List<String> dateList;
    private OnDateClickListener listener;

    public DateAdapter(List<String> dateList, OnDateClickListener listener) {
        this.dateList = dateList;
        this.listener = listener;
    }

    public void setDateList(List<String> dateList) {
        this.dateList = dateList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        String date = dateList.get(position);
        holder.textViewDate.setText(date);//(formatDate(date));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDateClick(date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (dateList != null) ? dateList.size() : 0;
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }

        // Optional: Format the date for better readability
        private String formatDate(String date) {
            // Implement date formatting if needed
            return date;
        }
    }
}
