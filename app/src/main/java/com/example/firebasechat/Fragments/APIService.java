package com.example.firebasechat.Fragments;

import com.example.firebasechat.Notifications.MyResponse;
import com.example.firebasechat.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Authorization:key=AAAAibW2aH4:APA91bGOAapNRr4jaQgolri0_3Mku7XYHZGBRHttXveNNTsgzF2ThiqoGXQ76LME83eJXKrz9NpBE1Lf8HWV6AKk8aTfvC_5Y_urngvVCDIgdbGgeWyCVm1UPWP6ivPD3JNgGqOxM9dd",
            "Content-Type:application/json"})
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
