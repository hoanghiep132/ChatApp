package com.hiepnh.chatapp.controller;

import com.hiepnh.chatapp.StageListener;
import com.hiepnh.chatapp.entities.UserEntity;
import com.hiepnh.chatapp.service.ApiService;
import com.hiepnh.chatapp.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    @FXML
    void forgotPassword(ActionEvent event) {

    }


    @FXML
    void login(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        UserEntity user = ApiService.login(username, password);
        if(user != null){
            UserSession.getInstance().setUser(user);
            Parent root;
            try {
                root =  FXMLLoader.load(getClass().getResource("/ui/app.fxml"));
                Scene scene = new Scene(root);
                StageListener.stage.close();
                StageListener.stage.setScene(scene);
                StageListener.stage.show();
            }
            catch (IOException e) {
                logger.error("Login error: ", e);
            }
        }else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Tên tài khoản hoặc mật khẩu không chính xác");
            alert.showAndWait();
            logger.info("Login failed");
        }
    }

    @FXML
    void signUp(ActionEvent event) {
        Parent root;
        try {
            root =  FXMLLoader.load(getClass().getResource("/ui/signup.fxml"));
            Scene scene = new Scene(root);
            StageListener.stage.close();
            StageListener.stage.setScene(scene);
            StageListener.stage.show();
        }
        catch (IOException e) {
            logger.error("Login error: ", e);
        }
    }

}
