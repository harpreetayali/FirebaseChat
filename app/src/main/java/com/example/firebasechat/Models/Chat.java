package com.example.firebasechat.Models;

public class Chat
{
    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    private String imageURL;


    public Chat(String sender, String receiver, String message,boolean isseen,String imageURL) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.imageURL = imageURL;


    }

    public Chat() {
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
}
