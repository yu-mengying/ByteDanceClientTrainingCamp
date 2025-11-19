package com.example.bytedancefollowpage.adapter;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bytedancefollowpage.R;
import com.example.bytedancefollowpage.db.User;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private OnUserMenuClickListener menuClickListener;

    public interface OnUserMenuClickListener {
        void onMoreClick(User user, View anchor);
        // 可扩展：取关/备注/特别关注等

    }

    public UserAdapter(List<User> userList, OnUserMenuClickListener listener) {
        this.userList = userList;
        this.menuClickListener = listener;
    }

    public void update(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder vh, int position) {
        User user = userList.get(position);
        vh.tvName.setText(user.name);
        vh.ivAvatar.setImageResource(user.avatarResId);

        // 显示认证图标
        if (user.isVerified) {
            vh.ivVerified.setVisibility(View.VISIBLE);
        } else {
            vh.ivVerified.setVisibility(View.GONE);
        }

        vh.tvName.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "已选中：" + user.name, Toast.LENGTH_SHORT).show()
        );

        // “已关注”
        vh.btnFollowed.setEnabled(true);
        //点击关注按钮切换样式
        vh.btnFollowed.setOnClickListener(v -> {
            if(user.isFollowing){
                user.isFollowing = false;
                vh.btnFollowed.setText("关注");
                vh.btnFollowed.setTextColor("#FFFFFF".equals(vh.btnFollowed.getTextColors().toString()) ? 0xFF000000 : 0xFFFFFFFF);
                vh.btnFollowed.setBackgroundResource(R.drawable.bg_follow_button);
            }else{
                user.isFollowing = true;
                vh.btnFollowed.setText("已关注");
                vh.btnFollowed.setTextColor("#000000".equals(vh.btnFollowed.getTextColors().toString()) ? 0xFFFFFFFF : 0xFF000000);
                vh.btnFollowed.setBackgroundResource(R.drawable.bg_followed_button);
            }

        });

        // 更多（三点）按钮
        vh.ivMore.setOnClickListener(v -> {
            if (menuClickListener != null) {
                menuClickListener.onMoreClick(user, vh.ivMore);
            }
        });

        vh.ivspecial.setVisibility(user.isSpecial ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivVerified, ivMore;
        TextView tvName,ivspecial;
        Button btnFollowed;

        UserViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            ivVerified = itemView.findViewById(R.id.iv_verified);
            ivMore = itemView.findViewById(R.id.iv_more);
            tvName = itemView.findViewById(R.id.tv_name);
            btnFollowed = itemView.findViewById(R.id.btn_followed);
            ivspecial = itemView.findViewById(R.id.menu_user_special);
        }
    }
}