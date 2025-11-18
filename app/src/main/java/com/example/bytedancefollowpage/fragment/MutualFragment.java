package com.example.bytedancefollowpage.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bytedancefollowpage.R;

public class MutualFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mutual, container, false);

        // TODO: findViewById, RecyclerView、空视图初始化、数据绑定
        // RecyclerView recyclerView = view.findViewById(R.id.recycler_view_mutual);
        // View emptyView = view.findViewById(R.id.empty_view);

        return view;
    }
}