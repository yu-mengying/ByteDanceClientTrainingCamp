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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FollowingFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyView;
    private UserAdapter adapter;
    private TextView followingCount;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int currentPage = 1;
    private final int pageSize = 10;
    private int totalUsers = 1000; // mock数据固定，实际根据接口返回赋值

    private final int MAX_CONCURRENT_PAGE_REQUESTS = 3;
    private final Set<Integer> loadingPages = new HashSet<>();
    private final OkHttpClient okHttpClient = new OkHttpClient();

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
                new Thread(() -> {
                    UserDao userDao = AppDatabase.getInstance(getContext()).userDao();
                    userDao.updateFollowingStatus(user.id, false);
                }).start();
                user.isFollowing = false;
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onfollowClick(User user) {
                new Thread(() -> {
                    UserDao userDao = AppDatabase.getInstance(getContext()).userDao();
                    userDao.updateFollowingStatus(user.id, true);
                }).start();
                user.isFollowing = true;
                adapter.notifyDataSetChanged();
            }
        };

        recyclerView.setHasFixedSize(true);
        adapter = new UserAdapter(new ArrayList<>(), menuListener);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastPos = manager.findLastVisibleItemPosition();
                int itemCount = adapter.getItemCount();
                // 若滑动接近底部
                if (itemCount < totalUsers && lastPos >= itemCount - 20) {
                    // 并发请求后续多页，每页只请求一次
                    for (int i = 0; i < MAX_CONCURRENT_PAGE_REQUESTS; i++) {
                        int nextPage = currentPage + i;
                        if (!loadingPages.contains(nextPage) && nextPage * pageSize <= totalUsers) {
                            fetchUsers(nextPage);
                        }
                    }
                }
            }
        });

        fetchUsers(currentPage); // 首次加载第一页

        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            adapter.clear();
            loadingPages.clear();
            fetchUsers(currentPage);
        });

        return view;
    }

    private synchronized void fetchUsers(int page) {
        if (loadingPages.contains(page)) return; // 已经请求，跳过
        loadingPages.add(page);
        swipeRefreshLayout.setRefreshing(true);

        String url = "https://8589e5ae-741b-4515-9f5e-52f5920a06f7.mock.pstmn.io/api/users?page=" + page + "&size=" + pageSize;

        Request request = new Request.Builder().url(url).get().build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
                loadingPages.remove(page);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                List<User> users = parseUsers(resp);
                int count = parseTotalCount(resp);
                requireActivity().runOnUiThread(() -> {
                    adapter.append(users);
                    followingCount.setText("我的关注（ " + (count > 0 ? count : totalUsers) + "人）");
                    swipeRefreshLayout.setRefreshing(false);

                    if (adapter.getItemCount() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }
                    // 更新当前页
                    if (page >= currentPage) {
                        currentPage = page + 1;
                    }
                });
                loadingPages.remove(page);
            }
        });
    }

    private List<User> parseUsers(String json) {
        Gson gson = new Gson();
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        JsonArray arr = obj.getAsJsonArray("data");
        return gson.fromJson(arr, new com.google.gson.reflect.TypeToken<List<User>>(){}.getType());
    }
    private int parseTotalCount(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        if(obj.has("total")) return obj.get("total").getAsInt();
        return 0;
    }

    private void showBottomSheetMenu(Context context, User user) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.menu_following_item, null);
        View followview = LayoutInflater.from(context).inflate(R.layout.item_user, null);
        TextView specialLabel = followview.findViewById(R.id.menu_user_special);
        specialLabel.setVisibility(user.isSpecial ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();

        // 设置数据
        if(user.remark!= null && !user.remark.isEmpty()){
            ((TextView) sheetView.findViewById(R.id.menu_user_name)).setText(user.remark);
            ((TextView) sheetView.findViewById(R.id.menu_user_desc)).setText("用户名：" + user.name +"    抖音号： " + user.id);
        } else{
            ((TextView) sheetView.findViewById(R.id.menu_user_name)).setText(user.name);
            ((TextView) sheetView.findViewById(R.id.menu_user_desc)).setText("抖音号： " + user.id);
        }

        // 特别关注
        Switch specialSwitch = sheetView.findViewById(R.id.menu_special_switch);
        specialSwitch.setChecked(user.isSpecial);
        specialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            specialLabel.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            adapter.notifyDataSetChanged();
            new Thread(() -> {
                UserDao userDao = AppDatabase.getInstance(context).userDao();
                userDao.updateSpecialStatus(user.id, isChecked);
            }).start();
            user.isSpecial = isChecked;
        });

        // 备注编辑
        sheetView.findViewById(R.id.menu_remark_edit).setOnClickListener(v -> {
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
}