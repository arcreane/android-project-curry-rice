package com.example.recyclescan3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclescan3.R;
import com.example.recyclescan3.model.Category;
import com.example.recyclescan3.model.HistoryItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(HistoryItem item);
    }

    private final Context             context;
    private final OnItemClickListener listener;
    private       List<HistoryItem>   items = new ArrayList<>();

    public HistoryAdapter(Context context, OnItemClickListener listener) {
        this.context  = context;
        this.listener = listener;
    }

    public void updateData(List<HistoryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = items.get(position);

        String name = item.getProductName();
        holder.tvProductName.setText((name != null && !name.isEmpty()) ? name : item.getBarcode());
        holder.tvBarcode.setText(item.getBarcode());
        holder.tvCategory.setText(item.getCategory().displayName);
        holder.tvScannedAt.setText(new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(new Date(item.getScannedAt())));
        holder.viewCategoryBadge.setBackgroundColor(colorForCategory(item.getCategory()));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int colorForCategory(Category category) {
        switch (category) {
            case RECYCLABLE:    return context.getColor(R.color.category_recyclable);
            case COMPOST:       return context.getColor(R.color.category_compost);
            case GENERAL_WASTE: return context.getColor(R.color.category_general);
            case HAZARDOUS:     return context.getColor(R.color.category_hazardous);
            default:            return context.getColor(R.color.category_general);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View     viewCategoryBadge;
        final TextView tvProductName;
        final TextView tvBarcode;
        final TextView tvCategory;
        final TextView tvScannedAt;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryBadge = itemView.findViewById(R.id.view_category_badge);
            tvProductName     = itemView.findViewById(R.id.tv_product_name);
            tvBarcode         = itemView.findViewById(R.id.tv_barcode);
            tvCategory        = itemView.findViewById(R.id.tv_category);
            tvScannedAt       = itemView.findViewById(R.id.tv_scanned_at);
        }
    }
}
