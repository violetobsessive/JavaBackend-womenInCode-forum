package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403, "No permission to visit");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        return CommunityUtil.getJSONString(0,"Post successful!");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String showDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {

        // 查询这个帖子
        DiscussPost post = discussPostService.findPostById(discussPostId);
        model.addAttribute("post", post);

        //查询帖子的账户
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //查询评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST,post.getId(),
                page.getOffset(), page.getLimit());

        // 构造map存comment和对应的userId
        // 评论：给post的评论
        // 回复：给帖子评论的回复
        List<Map<String, Object>> commentViewableObjectList = new ArrayList<>();
        if(commentList != null){
            for(Comment comment : commentList){
                // 一个评论的vo
                Map<String, Object> commentVO = new HashMap<>();
                commentVO.put("comment", comment);
                commentVO.put("user", userService.findUserById(comment.getUserId()));

                //回复列表
                List<Comment> replyLsit = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getEntityId(), 0, Integer.MAX_VALUE);

                // 回复VO列表
                List<Map<String, Object>> replyVOList = new ArrayList<>();
                if(replyVOList != null){
                    for(Comment reply : replyLsit){
                        // 一个回复的vo
                        Map<String, Object> replyVO = new HashMap<>();
                        replyVO.put("reply", reply);
                        replyVO.put("user", userService.findUserById(reply.getUserId()));

                        // 查找回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVO.put("target", target);
                        // replyVO 装到整个replyList
                        replyVOList.add(replyVO);
                    }
                }
                // replylist 装到CommentVO里
                commentVO.put("replies", replyVOList);

                // 回复数量
                int replyCount = commentService.findAllCommentsCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.put("replyCount", replyCount);

                commentViewableObjectList.add(commentVO);
            }

        }
        model.addAttribute("comments", commentViewableObjectList);

        return "/site/discuss-detail";
    }
    // 置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    // 加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    // 删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
