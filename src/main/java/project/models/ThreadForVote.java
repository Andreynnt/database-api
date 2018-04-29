package project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ThreadForVote {

    private Integer authorId;
    private Integer threadId;
    private Integer vote;

    @JsonCreator
    public ThreadForVote(@JsonProperty("author_id") Integer authorId,
                       @JsonProperty("id") Integer threadId,
                       @JsonProperty("votes") Integer vote) {
       this.authorId = authorId;
       this.threadId = threadId;
       this.vote = vote;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public Integer getThreadId() {
        return threadId;
    }

    public void setThreadId(Integer threadId) {
        this.threadId = threadId;
    }

    public Integer getVote() {
        return vote;
    }

    public void setVote(Integer vote) {
        this.vote = vote;
    }
}
