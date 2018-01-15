package com.example.dev.chatapplication;

/**
 * Created by Dev on 1/15/2018.
 */

public class Message {
    private String content, userName;

    public Message() {
    }

    public Message(String content, String userName) {
        this.content = content;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
