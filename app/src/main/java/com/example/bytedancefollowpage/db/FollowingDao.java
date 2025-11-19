package com.example.bytedancefollowpage.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface FollowingDao {
    @Insert
    void insert(Following following);

    @Update
    void update(Following following);

    @Delete
    void delete(Following following);

    @Query("SELECT * FROM following")
    List<Following> getAll();

    @Query("SELECT * FROM following WHERE isSpecial = 1")
    List<Following> getSpecial();

    @Query("UPDATE following SET remark = :remark WHERE id = :id")
    void updateRemark(String id, String remark);

    // 可扩展更多查询方法
}