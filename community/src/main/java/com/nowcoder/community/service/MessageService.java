package com.nowcoder.community.service;


import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    public List<Message> findConversations(int userid, int offset, int limit){
        return messageMapper.selectConversations(userid, offset, limit);
    }

    public int findConvsersationCount(int userid){
        return messageMapper.selectAllConversations(userid);
    }

    public List<Message> findDMs(String conversationId, int offset, int limit){
        return messageMapper.selectDMs(conversationId,offset,limit);
    }
    public int findDMsCount(String conversationId){
        return messageMapper.selectDMCount(conversationId);
    }

    public int countUnreadDMs(int userid, String conversationId){
        return messageMapper.selectDMUnread(userid, conversationId);
    }

    public int addDm(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        return messageMapper.insertDm(message);
    }

    // 私信设置成已读
    public int readDm(List<Integer> ids){
        return messageMapper.updateStatus(ids, 1);
    }

    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}
