package project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class ForumModel {

    private Long posts;
    private String slug;
    private Integer threads;
    private String title;
    private String user;

    @JsonCreator
    public ForumModel(@JsonProperty("posts") Long posts,
                      @JsonProperty("slug") String slug,
                      @JsonProperty("threads") Integer threads,
                      @JsonProperty("title") String title,
                      @JsonProperty("user") String user) {
        this.posts = Optional.ofNullable(posts).orElse(0L);
        this.threads = Optional.ofNullable(threads).orElse(0);
        this.slug = slug;
        this.title = title;
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Long getPosts() {
        return posts;
    }

    public void setPosts(Long posts) {
        this.posts = posts;
    }
}
