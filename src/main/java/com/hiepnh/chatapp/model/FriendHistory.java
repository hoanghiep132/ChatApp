package com.hiepnh.chatapp.model;

import com.hiepnh.chatapp.entities.InteractionUserEntity;
import lombok.Data;

@Data
public class FriendHistory {

    private InteractionUserEntity interactionUser;

    private Integer status;

    private Long lastUpdate;
}
