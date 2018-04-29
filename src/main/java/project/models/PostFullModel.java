package project.models;

import com.fasterxml.jackson.annotation.JsonInclude;

public class PostFullModel {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PostModel post;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ThreadModel thread;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserModel author;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ForumModel forum;


    public PostFullModel(PostModel post, ThreadModel thread, UserModel author, ForumModel forum) {
        this.post = post;
        this.thread = thread;
        this.author = author;
        this.forum = forum;
    }

    public PostFullModel(PostModel post) {
        this.post = post;
    }

    public PostModel getPost() {
        return post;
    }

    public void setPost(PostModel post) {
        this.post = post;
    }

    public ThreadModel getThread() {
        return thread;
    }

    public void setThread(ThreadModel thread) {
        this.thread = thread;
    }

    public UserModel getAuthor() {
        return author;
    }

    public void setAuthor(UserModel author) {
        this.author = author;
    }

    public ForumModel getForum() {
        return forum;
    }

    public void setForum(ForumModel forum) {
        this.forum = forum;
    }
}
