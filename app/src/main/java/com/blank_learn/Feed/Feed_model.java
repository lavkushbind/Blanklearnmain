package com.blank_learn.Feed;

public class Feed_model {

    String userid,downloads, share_on_media ,title,save ,click_on_profile, img, time, like, comments, share, seen, click, seen_time;

    public Feed_model() {
    }

    public Feed_model(String userid, String downloads, String share_on_media, String title, String save, String click_on_profile, String img, String time, String like, String comments, String share, String seen, String click, String seen_time) {
        this.userid = userid;
        this.downloads = downloads;
        this.share_on_media = share_on_media;
        this.title = title;
        this.save = save;
        this.click_on_profile = click_on_profile;
        this.img = img;
        this.time = time;
        this.like = like;
        this.comments = comments;
        this.share = share;
        this.seen = seen;
        this.click = click;
        this.seen_time = seen_time;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getDownloads() {
        return downloads;
    }

    public void setDownloads(String downloads) {
        this.downloads = downloads;
    }

    public String getShare_on_media() {
        return share_on_media;
    }

    public void setShare_on_media(String share_on_media) {
        this.share_on_media = share_on_media;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSave() {
        return save;
    }

    public void setSave(String save) {
        this.save = save;
    }

    public String getClick_on_profile() {
        return click_on_profile;
    }

    public void setClick_on_profile(String click_on_profile) {
        this.click_on_profile = click_on_profile;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getClick() {
        return click;
    }

    public void setClick(String click) {
        this.click = click;
    }

    public String getSeen_time() {
        return seen_time;
    }

    public void setSeen_time(String seen_time) {
        this.seen_time = seen_time;
    }

}