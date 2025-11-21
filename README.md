# ByteDanceClientTrainingCamp

## 项目简介

本项目为Android客户端开发训练营的练习项目，主要功能围绕「社交关注系统」实现。用户可以在应用中管理自己的关注、粉丝、朋友和互相关注的列表，并支持特别关注、备注编辑等功能。项目完全采用Java编写，并基于Room数据库进行用户数据管理。

## 主要功能

- **关注与粉丝管理**：支持显示和管理关注、粉丝、互关和朋友分组。
- **用户分组展示**：通过TabLayout与ViewPager2实现页面切换，包括互关、关注、粉丝、朋友四个板块。
- **用户数据库**：使用Room ORM框架管理本地用户数据，支持新增、删除、修改（备注、特别关注、关注状态等）。
- **RecyclerView用户列表**：各分组下用户以RecyclerView方式展示，支持更多操作（弹出菜单、特别关注等）。
- **特别关注及备注**：支持对关注用户设置特别关注，以及编辑用户备注。
- **更多弹窗**：支持在更多弹窗里，设置对关注人的设置备注，以及更多信息查看等操作。

## 主要代码结构

```  
app/src/main/java/com/example/bytedancefollowpage/  
├── MainActivity.java                 // 主页面，负责顶部Tab切换与碎片管理  
├── db/  
│   ├── AppDatabase.java              // Room数据库入口  
│   ├── User.java                     // 用户实体类  
│   ├── UserDao.java                  // 用户数据操作接口  
├── fragment/  
│   ├── MutualFragment.java           // "互关"页面  
│   ├── FollowingFragment.java        // "关注"页面  
│   ├── FollowerFragment.java         // "粉丝"页面  
│   ├── FriendFragment.java           // "朋友"页面  
├── adapter/  
│   ├── UserAdapter.java              // RecyclerView适配器，负责各列表数据展示与操作  
```  

## 项目运行

1. **环境要求**：
    - Android Studio
    - Java 8+
    - Gradle
    - 依赖库：Room、Material Components（部分）

2. **运行方式**：
    - 克隆本项目至本地。
    - 用Android Studio打开，并编译运行至模拟器或真机。
    - 默认已集成测试用户，支持基本的关注/备注/特别关注等操作。  
