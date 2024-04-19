package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}") //域名
    private String domain;

    @Value("${server.servlet.context-path}") //项目路径名
    private String contextPath;
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    //用户注册返回的信息
    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();
        if(user == null){
            throw new IllegalArgumentException("The parameter can not be empty");
        }
        // check if username is blank
        if(StringUtils.isBlank(user.getUsername())){
            map.put("UsernameMessage","Username can't be empty");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("PasswordMessage","Password can't be empty");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("EmailMessage","Email can't be empty");
            return map;
        }
        // verify if username already existed in the database
        User user1 = userMapper.selectByName(user.getUsername());
        if(user1 != null){
            map.put("UsernameMessage","Username already existed!");
            return map;
        }

        User user2 = userMapper.selectByName(user.getEmail());
        if(user2 != null){
            map.put("EmailMessage","Email already existed!");
            return map;
        }

        // register new user
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+ user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

//        // activation email
//        Context context = new Context();
//        context.setVariable("email", user.getEmail());
//        // http://localhost:8080/community/activation/userID/activationCode
//        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
//        context.setVariable("url", url);
//        String content = templateEngine.process("/mail/activation", context);
//        mailClient.sendMail(user.getEmail(), "Activate account", content);

        return map;
    }

//    public int activation(int userId, String code) {
//        User user = userMapper.selectById(userId);
//        if (user.getStatus() == 1) {
//            return ACTIVATION_REPEAT;
//        } else if (user.getActivationCode().equals(code)) {
//            userMapper.updateStatus(userId, 1);
//            return ACTIVATION_SUCCESS;
//        } else {
//            return ACTIVATION_FAILURE;
//        }
//    }

    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "Username can not be empty!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "Password can not be empty!");
            return map;
        }
        //验证账号
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg", "User does not exist!");
        }

        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "Password incorrect!");
            return map;
        }

        //登陆成功 - 生成登录凭证，并存入database
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;

    }


    //退出登录
    public void logout(String ticket){

        // loginTicketMapper.updateStatus("ticket", 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    //查询登录凭证ticket
    public LoginTicket findloginTicket(String ticket){
        // return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    //更新修改头像的路径，返回更新的行数
    public int updateHeader(int userId, String headerUrl){
       //return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        return rows;
    }

    public User findUserIdByUsername(String username){
        return userMapper.selectByName(username);
    }
    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

}
