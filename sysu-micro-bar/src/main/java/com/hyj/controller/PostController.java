package com.hyj.controller;

import com.alibaba.fastjson.JSON;
import com.hyj.dto.HistoryData;
import com.hyj.dto.PostData;
import com.hyj.service.HistoryMessageService;
import com.hyj.service.PostFloorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Administrator on 2016/5/18 0018.
 */
@Controller
public class PostController {
    private static final Logger logger = LoggerFactory
            .getLogger(PostController.class);
    @Resource
    private PostFloorService postFloorService;
    @Resource
    private HistoryMessageService historyMessageService;

    /**
     * 首页上拉刷新: 显示最新帖子
     * @param currentPostNum
     * @return
     */
    @RequestMapping(value = "/getPostList", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<PostData> getPostData(@RequestParam("currentPostNum") int currentPostNum) {
        logger.info(JSON.toJSONString(postFloorService.getPostDataList(currentPostNum)));
        return postFloorService.getPostDataList(currentPostNum);
    }


    /**
     * 首页下拉刷新
     * @param firstPostId
     * @return
     */
    @RequestMapping(value = "/getPostUpdated", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<PostData> getPostUpdated(@RequestParam("firstPostId") int firstPostId) {
        logger.info(JSON.toJSONString(postFloorService.getPostDataUpdated(firstPostId)));
        return postFloorService.getPostDataUpdated(firstPostId);
    }


    /**
     * 首页个人消息: 显示是否有最新消息
     * @param accountId
     * @return
     */
    @RequestMapping(value = "/checkMessage", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public boolean checkMessage(@RequestParam("accountId") int accountId) {
        logger.info("是否有消息" + historyMessageService.checkMessage(accountId));
        return historyMessageService.checkMessage(accountId);
    }

    /**
     * 查看消息下拉刷新: 显示最新消息
     * @param accountId
     * @param currentMessageNum
     * @return
     */
    @RequestMapping(value = "/loadMessage", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<HistoryData> loadMessage(@RequestParam("accountId") int accountId, @RequestParam("currentMessageNum") int currentMessageNum) {
        logger.info("消息为" + historyMessageService.loadMessage(accountId, currentMessageNum));
        return historyMessageService.loadMessage(accountId, currentMessageNum);
    }

    /**
     * 搜索帖子: 仅仅返回固定条目的帖子
     * @param title
     * @param tag
     * @return
     */
    @RequestMapping(value = "/searchPostList", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<PostData> searchPostList(@RequestParam("title") String title, @RequestParam("tag") int tag) {
        logger.info(JSON.toJSONString(postFloorService.searchPostData(title, tag)));
        return postFloorService.searchPostData(title, tag);
    }



}
