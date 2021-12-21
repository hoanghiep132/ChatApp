package com.hiepnh.chatapp.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Data
public class MessageEntity {

    private Integer id;

    private Integer type;

    private String content;

    private Integer status;

    private Long time;

    private UserEntity sender;

    private UserEntity receiver;
}
