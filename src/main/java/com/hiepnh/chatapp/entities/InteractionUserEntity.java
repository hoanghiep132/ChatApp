package com.hiepnh.chatapp.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
public class InteractionUserEntity {

    private Integer id;

    private String content;

    private Long time;

    private UserEntity user;

    private UserEntity interaction;
}
