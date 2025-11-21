package com.example.bytedancefollowpage.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    private TextView followingCount;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_following, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_following);
        emptyView = view.findViewById(R.id.empty_view);
        followingCount = view.findViewById(R.id.tv_following_count);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        UserAdapter.OnUserMenuClickListener menuListener = new UserAdapter.OnUserMenuClickListener() {
            @Override
            public void onMoreClick(User user, View anchor) {
                showBottomSheetMenu(anchor.getContext(), user);
            }

            @Override
            public void onUnfollowClick(User user) {
                // 取关数据库更新，推荐放子线程
                new Thread(() -> {
                    UserDao userDao = AppDatabase.getInstance(getContext()).userDao();
                    userDao.updateFollowingStatus(user.id, false);
                }).start();
                user.isFollowing = false;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onfollowClick(User user) {
                // 关注数据库更新，推荐放子线程
                new Thread(() -> {
                    UserDao userDao = AppDatabase.getInstance(getContext()).userDao();
                    userDao.updateFollowingStatus(user.id, true);
                }).start();
                user.isFollowing = true;
                adapter.notifyDataSetChanged();
            }
        };
        // 先加载空数据，避免空指针
        adapter = new UserAdapter(null, menuListener);
        recyclerView.setAdapter(adapter);

        // 下拉刷新监听
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadData2(); // 重新加载数据
        });


        loadData();
        return view;
    }

    private void showBottomSheetMenu(Context context, User user) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.menu_following_item, null);
        View followview = LayoutInflater.from(context).inflate(R.layout.item_user, null);
        TextView specialLabel = followview.findViewById(R.id.menu_user_special);
        specialLabel.setVisibility(user.isSpecial ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();

// 设置数据
        //如果有备注则显示备注，否则显示名字
        if(user.remark!= null && !user.remark.isEmpty()){
            ((TextView) sheetView.findViewById(R.id.menu_user_name)).setText(user.remark);
            ((TextView) sheetView.findViewById(R.id.menu_user_desc)).setText("用户名：" + user.name +"    抖音号： " + user.id);
        } else{
            ((TextView) sheetView.findViewById(R.id.menu_user_name)).setText(user.name);
            ((TextView) sheetView.findViewById(R.id.menu_user_desc)).setText("抖音号： " + user.id);
        }// 示例用ID作为描述

// 特别关注
        Switch specialSwitch = sheetView.findViewById(R.id.menu_special_switch);
        specialSwitch.setChecked(user.isSpecial);
        specialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 更新数据库特别关注状态
            specialLabel.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            adapter.notifyDataSetChanged();
            new Thread(() -> {
                UserDao userDao = AppDatabase.getInstance(context).userDao();
                userDao.updateSpecialStatus(user.id, isChecked);
            }).start();

            // 同步User数据&数据库
            user.isSpecial = isChecked;
        });

// 备注编辑
        sheetView.findViewById(R.id.menu_remark_edit).setOnClickListener(v -> {
            // 弹出备注编辑对话框
            // 创建输入框
            EditText editText = new EditText(context);
            editText.setText(user.remark);
            editText.setHint("请输入备注");

            new AlertDialog.Builder(context)
                    .setTitle("编辑备注")
                    .setView(editText)
                    .setPositiveButton("保存", (dialogInterface, which) -> {
                        String remark = editText.getText().toString().trim();
                        user.remark = remark;
                        new Thread(() -> {
                            UserDao userDao = AppDatabase.getInstance(context).userDao();
                            userDao.updateRemark(user.id, remark);
                        }).start();
                        adapter.notifyDataSetChanged();
                        ((TextView) sheetView.findViewById(R.id.menu_user_name)).setText(remark);
                        ((TextView) sheetView.findViewById(R.id.menu_user_desc)).setText("用户名：" + user.name +"    抖音号： " + user.id);
                        Toast.makeText(context, "备注已保存", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();



        });

// 取消关注
        sheetView.findViewById(R.id.menu_unfollow).setOnClickListener(v -> {
            // 处理取消关注
            dialog.dismiss();
            user.isFollowing= false;
            adapter.notifyDataSetChanged();

            new Thread(() -> {
                UserDao userDao = AppDatabase.getInstance(context).userDao();
                userDao.updateFollowingStatus(user.id, false);
            }).start();



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
            user1.isSpecial = false;
            user1.isFollowing = true;
            user1.relationType = "following";

            User user2 = new User();
            user2.id = "2";
            user2.name = "林俊杰";
            user2.avatarResId = R.drawable.avatar1;
            user2.isSpecial = true;
            user2.isVerified = true;
            user2.isFollowing = true;
            user2.relationType = "following";

            User user3 = new User();
            user3.id = "3";
            user3.name = "王力宏";
            user3.avatarResId = R.drawable.avatar1;
            user3.isSpecial = false;
            user3.isVerified = false;
            user3.isFollowing = true;
            user3.relationType = "following";

            User user4 = new User();
            user4.id = "4";
            user4.name = "蔡依林";
            user4.avatarResId = R.drawable.avatar1;
            user4.isSpecial = true;
            user4.isVerified = true;
            user4.isFollowing = true;
            user4.relationType = "following";

            User user5 = new User();
            user5.id = "5";
            user5.name = "小明";
            user5.avatarResId = R.drawable.avatar1;
            user5.isSpecial = false;
            user5.isVerified = false;
            user5.isFollowing = true;
            user5.relationType = "following";


// 获取数据库实例和Dao
            UserDao userDao = AppDatabase.getInstance(getContext()).userDao();

// 插入（放子线程或使用Room的异步API，避免主线程阻塞）
            new Thread(() -> {
                userDao.insert(user1);
                userDao.insert(user2);
                userDao.insert(user3);
                userDao.insert(user4);
                userDao.insert(user5);
                // 可以继续插入更多测试用户
            }).start();


            List<User> followingList = userDao.getFollowingUsers();
            int count = userDao.getFollowingCount();


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
                followingCount.setText("我的关注（ " + count + "人）");
                swipeRefreshLayout.setRefreshing(false); // 加载完成后停止刷新动画
            });
        }).start();
    }


    private void loadData2() {
        // 假设你用 Room 数据库
        new Thread(() -> {

//            // 创建测试用户
//            User user1 = new User();
//            user1.id = "1";
//            user1.name = "周杰伦";
//            user1.avatarResId = R.drawable.avatar1; // 你的头像资源
//            user1.isVerified = true;
//            user1.isSpecial = false;
//            user1.isFollowing = true;
//            user1.relationType = "following";
//
//            User user2 = new User();
//            user2.id = "2";
//            user2.name = "林俊杰";
//            user2.avatarResId = R.drawable.avatar1;
//            user2.isSpecial = true;
//            user2.isVerified = true;
//            user2.isFollowing = true;
//            user2.relationType = "following";
//
//            User user3 = new User();
//            user3.id = "3";
//            user3.name = "王力宏";
//            user3.avatarResId = R.drawable.avatar1;
//            user3.isSpecial = false;
//            user3.isVerified = false;
//            user3.isFollowing = true;
//            user3.relationType = "following";

// 获取数据库实例和Dao
            UserDao userDao = AppDatabase.getInstance(getContext()).userDao();

// 插入（放子线程或使用Room的异步API，避免主线程阻塞）
//            new Thread(() -> {
//                userDao.insert(user1);
//                userDao.insert(user2);
//                userDao.insert(user3);
//                // 可以继续插入更多测试用户
//            }).start();


            List<User> followingList = userDao.getFollowingUsers();
            int count = userDao.getFollowingCount();


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
                followingCount.setText("我的关注（ " + count + "人）");
                swipeRefreshLayout.setRefreshing(false); // 加载完成后停止刷新动画
            });
        }).start();
    }
}