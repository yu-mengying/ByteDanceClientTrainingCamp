package com.example.bytedancefollowpage.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "following")
public class Following {
    @PrimaryKey
    @NonNull
    public String id;

    public String name;
    public int avatarResId;    // drawable资源ID
    public boolean isSpecial;  // 特别关注
    public String remark;      // 备注
    public boolean isVerified;
}