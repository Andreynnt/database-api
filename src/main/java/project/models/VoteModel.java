package project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VoteModel {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nickname;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String thread;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer voice;

    @JsonCreator
    public VoteModel(@JsonProperty("nickname") String nickname,
                     @JsonProperty("thread")String thread,
                     @JsonProperty("voice")Integer voice) {
        this.nickname = nickname;
        this.thread = thread;
        this.voice = voice;
    }


    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public Integer getVoice() {
        return voice;
    }

    public void setVoice(Integer voice) {
        this.voice = voice;
    }
}
