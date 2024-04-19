package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 私信这个nav里的tab
    // 查询当前用户的私信列表 - 某一页的数据, 针对每个会话只返回一条最新的私信
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前登录用户总会话数量
    int selectAllConversations(int userId);


    // 私信详情
    // 查询某个对话所包含的私信列表
    List<Message> selectDMs(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量
    int selectDMCount(String conversationId);

    // 查询未读消息数量
    int selectDMUnread(int userId, String conversationId);

    // 新增私信 - 给别人发dm
     int insertDm(Message message);

     // 未读变成已读/删除 - 修改状态, 同时修改多个用户（需要id集合）
    int updateStatus(List<Integer>ids, int status);

    // 查询某个主题下最新的通知
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题所包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读的通知的数量
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询某个主题所包含的通知列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);

}
