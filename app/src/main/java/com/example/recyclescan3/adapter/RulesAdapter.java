package com.example.recyclescan3.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclescan3.R;
import com.example.recyclescan3.model.BinRule;

import java.util.List;

public class RulesAdapter extends RecyclerView.Adapter<RulesAdapter.ViewHolder> {

    private final List<BinRule> rules;

    public RulesAdapter(List<BinRule> rules) {
        this.rules = rules;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BinRule rule = rules.get(position);
        holder.tvProductType.setText(rule.productType);
        holder.tvBinLabel.setText(rule.binLabel);
        holder.tvCategory.setText(rule.category.displayName);
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvProductType;
        final TextView tvBinLabel;
        final TextView tvCategory;

        ViewHolder(View itemView) {
            super(itemView);
            tvProductType = itemView.findViewById(R.id.tv_product_type);
            tvBinLabel = itemView.findViewById(R.id.tv_bin_label);
            tvCategory = itemView.findViewById(R.id.tv_category);
        }
    }
}
