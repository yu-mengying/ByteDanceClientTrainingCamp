package com.example.bytedancefollowpage.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bytedancefollowpage.R;
import com.example.bytedancefollowpage.adapter.UserAdapter;
import com.example.bytedancefollowpage.db.AppDatabase;
import com.example.bytedancefollowpage.db.User;
import com.example.bytedancefollowpage.db.UserDao;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class FollowingFragment extends Fragment {
    private RecyclerView recyclerView;
    private View emptyView;
    private UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_following, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_following);
        emptyView = view.findViewById(R.id.empty_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        UserAdapter.OnUserMenuClickListener menuListener = (user, anchor) -> {
            showBottomSheetMenu(anchor.getContext(), user);
        };
        // 先加载空数据，避免空指针
        adapter = new UserAdapter(null, menuListener);
        recyclerView.setAdapter(adapter);



        loadData();
        return view;
    }

    private void showBottomSheetMenu(Context context, User user) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.menu_following_item, null);

// 设置数据
        ((TextView) sheetView.findViewById(R.id.menu_user_name)).setText(user.name);
        ((TextView) sheetView.findViewById(R.id.menu_user_desc)).setText(user.id); // 示例用ID作为描述

// 特别关注
        Switch specialSwitch = sheetView.findViewById(R.id.menu_special_switch);
        specialSwitch.setChecked(user.isSpecial);
        specialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 更新数据库特别关注状态
        });

// 备注编辑
        sheetView.findViewById(R.id.menu_remark_edit).setOnClickListener(v -> {
            // 弹出备注编辑对话框
        });

// 取消关注
        sheetView.findViewById(R.id.menu_unfollow).setOnClickListener(v -> {
            // 处理取消关注
            dialog.dismiss();
        });

        dialog.setContentView(sheetView);
        dialog.show();
    }

    private void loadData() {
        // 假设你用 Room 数据库
        new Thread(() -> {

            // 创建测试用户
            User user1 = new User();
            user1.id = "1";
            user1.name = "周杰伦";
            user1.avatarResId = R.drawable.avatar1; // 你的头像资源
            user1.isVerified = true;
            user1.relationType = "following";

            User user2 = new User();
            user2.id = "2";
            user2.name = "林俊杰";
            user2.avatarResId = R.drawable.avatar1;
            user2.isVerified = true;
            user2.relationType = "following";

// 获取数据库实例和Dao
            UserDao userDao = AppDatabase.getInstance(getContext()).userDao();

// 插入（放子线程或使用Room的异步API，避免主线程阻塞）
            new Thread(() -> {
                userDao.insert(user1);
                userDao.insert(user2);
                // 可以继续插入更多测试用户
            }).start();


            List<User> followingList = userDao.getUsersByType("following"); // "following"类型为你关注的人
            // 回到主线程更新UI
            requireActivity().runOnUiThread(() -> {
                adapter.update(followingList);
                if (followingList == null || followingList.size() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            });
        }).start();
    }
}