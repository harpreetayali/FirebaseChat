package com.example.firebasechat.Models;

import com.example.firebasechat.Models.Data;
import com.google.gson.annotations.SerializedName;

public class Sender {



    @SerializedName("to")
    private String token;

    @SerializedName("notification")
    private Data data;

    public Sender(String token,Data data) {
        this.token = token;
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


}
