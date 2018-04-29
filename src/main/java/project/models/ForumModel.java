package project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class ForumModel {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer posts;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String slug;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer threads;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String user;

    @JsonCreator
    public ForumModel(@JsonProperty("posts") Integer posts,
                      @JsonProperty("slug") String slug,
                      @JsonProperty("threads") Integer threads,
                      @JsonProperty("title") String title,
                      @JsonProperty("user") String user) {
        this.posts = posts;
        this.threads = threads;
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

    public Integer getPosts() {
        return posts;
    }

    public void setPosts(Integer posts) {
        this.posts = posts;
    }
}
