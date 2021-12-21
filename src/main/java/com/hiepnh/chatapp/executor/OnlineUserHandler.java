package com.hiepnh.chatapp.executor;

import com.hiepnh.chatapp.model.UserModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OnlineUserHandler {

    private List<UserModel> onlineUserList;

    public OnlineUserHandler() {
        this.onlineUserList = Collections.synchronizedList(new ArrayList<>());
    }

    public void add(UserModel userModel){
        synchronized (this){
            this.onlineUserList.add(userModel);
        }
    }
}
