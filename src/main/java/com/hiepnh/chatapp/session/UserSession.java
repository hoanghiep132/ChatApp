package com.hiepnh.chatapp.session;

import com.hiepnh.chatapp.entities.UserEntity;
import lombok.Data;
import org.springframework.stereotype.Component;


public class UserSession {

    private static UserSession instance;

    private UserEntity user;

    private UserSession(){
        user = new UserEntity();
    }

    public static UserSession getInstance(){
        if(instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public UserEntity getUser(){
        return this.user;
    }

    public void setUser(UserEntity user){
        this.user = user;
    }
}
