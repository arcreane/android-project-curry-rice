package com.example.recyclescan3.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclescan3.R;
import com.example.recyclescan3.adapter.RulesAdapter;
import com.example.recyclescan3.data.RegionRepository;
import com.example.recyclescan3.model.Region;

public class RegionRulesFragment extends Fragment {

    private static final String ARG_REGION_CODE = "region_code";

    // Always create fragments this way — never pass data through the constructor.
    // Android can recreate fragments and the constructor must remain empty.
    public static RegionRulesFragment newInstance(String regionCode) {
        RegionRulesFragment fragment = new RegionRulesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REGION_CODE, regionCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_region_rules, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String code = getArguments() != null
                ? getArguments().getString(ARG_REGION_CODE, RegionRepository.getDefault().code)
                : RegionRepository.getDefault().code;

        Region region = RegionRepository.getByCode(code);

        TextView title = view.findViewById(R.id.tv_rules_title);
        title.setText("Sorting rules — " + region.displayName);

        RecyclerView recyclerView = view.findViewById(R.id.rv_rules);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new RulesAdapter(region.rules));
    }
}
