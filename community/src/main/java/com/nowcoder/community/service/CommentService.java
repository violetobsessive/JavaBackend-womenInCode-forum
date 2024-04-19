package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

public class CommentService implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostService discussPostService;

    //当前页的所有comments
    public List<Comment> findCommentsByEntity (int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset,limit);
    }

    //数据库里的所有comment数量
    public int findAllCommentsCount(int entityType, int entityId){
        return commentMapper.selectSumofCommentsByEntity(entityType,entityId);
    }

    // 增加comment的业务
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("Parameters can not be null");
        }
        // 过滤html标签并把评论添加入库
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        int rowsInserted = commentMapper.insertComment(comment);

        // 更新帖子的评论数量
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            // 查询所有帖子的数量
            int count = commentMapper.selectSumofCommentsByEntity(ENTITY_TYPE_POST, comment.getEntityId());

            // 更新帖子数量到discuss table里的comment_count列
            int newCommentCount = discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rowsInserted;
    }
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

}
