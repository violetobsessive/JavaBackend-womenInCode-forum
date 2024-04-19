package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    //offset是每一页起始行的号码
    //limit是煤业最多显示几条数据
    //这两个参数是为了分页 - pagination
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit,int orderMode);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    // 这个方法是为了count table里总共有多少条数据 - pagination
    //这两个方法都是查询
    int selectDiscussPostRows(@Param("userId") int userId);

    //增加帖子, 返回增加的行数
    int insertDiscussPost(DiscussPost discussPost);

    //查询帖子详情
    DiscussPost selectPostDetailById(int userId);

    //更新评论数量
    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);
}
