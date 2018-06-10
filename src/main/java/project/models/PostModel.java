package project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class PostModel {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String author;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String created;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String forum;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isEdited;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer parent;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer thread;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer authorId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer forumId;



    @JsonCreator
    public PostModel(@JsonProperty("author") String author,
                       @JsonProperty("created") String created,
                       @JsonProperty("forum") String forum,
                       @JsonProperty("id") Integer id,
                       @JsonProperty("isEdited") Boolean isEdited,
                       @JsonProperty("message") String message,
                       @JsonProperty("parent") Integer parent,
                       @JsonProperty("thread") Integer thread) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.message = message;
        this.thread = thread;
        this.parent = parent;
        if (isEdited == null) {
            this.isEdited = false;
        } else {
            this.isEdited = isEdited;
        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean edited) {
        isEdited = edited;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }

    public static PostModel getPost(ResultSet rs, int amount) throws SQLException {
        final Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return new PostModel(
                rs.getString("author"),
                dateFormat.format(timestamp.getTime()),
                rs.getString("forum"),
                rs.getInt("id"),
                rs.getBoolean("isEdited"),
                rs.getString("message"),
                rs.getInt("parent"),
                rs.getInt("thread")
        );
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public Integer getForumId() {
        return forumId;
    }

    public void setForumId(Integer forumId) {
        this.forumId = forumId;
    }
}
