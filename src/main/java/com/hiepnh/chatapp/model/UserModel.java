package com.hiepnh.chatapp.model;

import io.netty.channel.Channel;
import lombok.Data;

@Data
public class UserModel {

    private String username;

    private int status;

    private long time;
}
