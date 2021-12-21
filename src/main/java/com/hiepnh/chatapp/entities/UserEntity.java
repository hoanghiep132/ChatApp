package com.hiepnh.chatapp.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class UserEntity {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer id;

    private String username;

    private String password;

    private String fullName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Byte[] avatar;


}
