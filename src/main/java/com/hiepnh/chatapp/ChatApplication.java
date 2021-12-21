package com.hiepnh.chatapp;

import com.hiepnh.chatapp.entities.UserEntity;
import com.hiepnh.chatapp.service.ApiService;
import com.hiepnh.chatapp.session.UserSession;
import com.hiepnh.chatapp.utils.AppUtils;
import javafx.application.Application;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;


@SpringBootApplication
public class ChatApplication {

    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }

}
