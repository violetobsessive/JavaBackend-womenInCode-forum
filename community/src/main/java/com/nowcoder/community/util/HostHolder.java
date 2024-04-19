package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 相当于容器container
 * 持有用户数据，用来代替session对象
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<User>();

    public void setUser(User user){
        users.set(user);
    }
    public User getUser(){
        return users.get();
    }
    public void removeUser(){
        users.remove();
    }
}
