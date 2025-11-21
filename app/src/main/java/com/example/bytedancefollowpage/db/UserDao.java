package com.example.bytedancefollowpage.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE relationType=:type")
    List<User> getUsersByType(String type);

    @Query("SELECT * FROM users WHERE isSpecial = 1")
    List<User> getSpecialUsers();

    @Query("SELECT * FROM users WHERE isFollowing = 1")
    List<User> getFollowingUsers();

    //查询关注人数
    @Query("SELECT COUNT(*) FROM users WHERE isFollowing = 1")
    int getFollowingCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Delete
    void delete(User user);

    @Update
    void update(User user);

    @Query("UPDATE users SET remark = :remark WHERE id = :id")
    void updateRemark(String id, String remark);

    @Query("UPDATE users SET isFollowing = :isFollowing WHERE id = :id")
    void updateFollowingStatus(String id, boolean isFollowing);
    @Query("UPDATE users SET isSpecial = :isChecked WHERE id = :id")
    void updateSpecialStatus(String id, boolean isChecked);
}
