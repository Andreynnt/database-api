package project.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageModel {

    @JsonProperty
    private String message;

    MessageModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
