package com.hiepnh.chatapp.model;

import lombok.Data;

@Data
public class MessageUserModel {

    private UserModel userModel;

    private String message;

    private String time;
}
