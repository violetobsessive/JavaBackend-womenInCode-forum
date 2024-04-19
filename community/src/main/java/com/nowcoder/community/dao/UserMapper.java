package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper //myBatis的default DI, 让Spring container装配这个bean

public interface UserMapper {

    //查找的methods - Read
    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    //增加一个用户，返回插入数据的行数
    int insertUser(User user);

    //修改用户，返回修改了几条数据
    int updateStatus(int id, int status);

    //更新头像的路径
    int updateHeader(int id, String headerUrl);

    //更新密码
    int updatePassword(int id, String password);

}
