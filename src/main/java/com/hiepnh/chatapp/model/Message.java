package com.hiepnh.chatapp.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message implements Serializable {

    private byte tag;

    private long time;

    private byte[] videoData;

    private String sender;

    private String receiver;

    private String content;

}
