package com.hyj.dto;

/**
 * Created by Administrator on 2016/5/26 0026.
 */
public class FloorData {
    Integer floorId;
    String headImageUrl;
    String nickname;
    String createTime;
    String detail;
    Integer isReply;
    String replyWho;
    Integer replyFloorId;

    public FloorData() {}

    public FloorData(Integer floorId, String headImageUrl, String nickname, String createTime, String detail, Integer isReply, String replyWho, Integer replyFloorId) {
        this.floorId = floorId;
        this.headImageUrl = headImageUrl;
        this.nickname = nickname;
        this.createTime = createTime;
        this.detail = detail;
        this.isReply = isReply;
        this.replyWho = replyWho;
        this.replyFloorId = replyFloorId;
    }

    public Integer getFloorId() {
        return floorId;
    }

    public void setFloorId(Integer floorId) {
        this.floorId = floorId;
    }

    public Integer getReplyFloorId() {
        return replyFloorId;
    }

    public void setReplyFloorId(Integer replyFloorId) {
        this.replyFloorId = replyFloorId;
    }

    public String getHeadImageUrl() {
        return headImageUrl;
    }

    public void setHeadImageUrl(String headImageUrl) {
        this.headImageUrl = headImageUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Integer isReply() {
        return isReply;
    }

    public void setReply(Integer reply) {
        isReply = reply;
    }

    public String getReplyWho() {
        return replyWho;
    }

    public void setReplyWho(String replyWho) {
        this.replyWho = replyWho;
    }

}
