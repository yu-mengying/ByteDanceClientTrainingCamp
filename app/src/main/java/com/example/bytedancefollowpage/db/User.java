package com.example.bytedancefollowpage.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String id;
    public String name;
    public int avatarResId;    // drawable 资源ID
    public String avatarUrl;    // 头像URL
    public boolean isSpecial;  // 特别关注
    public boolean isFollowing; // 是否已关注
    public String remark;      // 备注
    public String relationType; // "mutual", "following", "follower", "friend"
    public boolean isVerified;
}
