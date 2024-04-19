package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;

import java.util.ArrayList;
import java.util.List;

public interface CommentMapper {

    // 查询一个comment object
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    // 查询所有comment的数量
    int selectSumofCommentsByEntity(int entityType, int entityId);

    // 增加数据评论
    int insertComment(Comment comment);

    Comment selectCommentById(int id);

}
