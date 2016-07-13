package com.softwaredesign.microbar.model;

/**
 * Created by mac on 16/6/3.
 */
public class Floor {

    private int floorId;
    private String headImageUrl;
    private String nickname;
    private String createTime;
    private String detail;
    private int replyed;
    private String replyWho;
    private String replyFloorId;

    public Floor() {
    }

    public Floor(int floorId, String headImageUrl, String nickname, String createTime, String detail, int replyed, String replyWho, String replyFloorId) {
        this.floorId = floorId;
        this.headImageUrl = headImageUrl;
        this.nickname = nickname;
        this.createTime = createTime;
        this.detail = detail;
        this.replyed = replyed;
        this.replyWho = replyWho;
        this.replyFloorId = replyFloorId;
    }

    public int getFloorId() {
        return floorId;
    }

    public void setFloorId(int floorId) {
        this.floorId = floorId;
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

    public int getReplyed() {
        return replyed;
    }

    public void setReplyed(int replyed) {
        this.replyed = replyed;
    }

    public String getReplyWho() {
        return replyWho;
    }

    public void setReplyWho(String replyWho) {
        this.replyWho = replyWho;
    }

    public String getReplyFloorId() {
        return replyFloorId;
    }

    public void setReplyFloorId(String replyFloorId) {
        this.replyFloorId = replyFloorId;
    }

    @Override
    public String toString() {
        return "Floor{" +
                "floorId=" + floorId +
                ", headImageUrl='" + headImageUrl + '\'' +
                ", nickname='" + nickname + '\'' +
                ", createTime='" + createTime + '\'' +
                ", detail='" + detail + '\'' +
                ", replyed=" + replyed +
                ", replyWho='" + replyWho + '\'' +
                ", replyFloorId='" + replyFloorId + '\'' +
                '}';
    }
}
