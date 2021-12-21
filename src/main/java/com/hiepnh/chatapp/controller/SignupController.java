package com.hiepnh.chatapp.controller;

import com.hiepnh.chatapp.StageListener;
import com.hiepnh.chatapp.common.Constant;
import com.hiepnh.chatapp.entities.UserEntity;
import com.hiepnh.chatapp.service.ApiService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SignupController implements Initializable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField fileInput;

    @FXML
    private ImageView avatarView;

    private File imgFile;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        avatarView.setFitWidth(150);
        avatarView.setFitHeight(150);
        imgFile = new File(Constant.DEFAULT_AVATAR_PATH);
        Image image = new Image(imgFile.toURI().toString());
        avatarView.setImage(image);
    }

    @FXML
    void selectInput(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileInput.clear();
        imgFile = fileChooser.showOpenDialog(new Stage());
        if (imgFile != null) {
            fileInput.setText(imgFile.getAbsolutePath());
            Image image = new Image(imgFile.toURI().toString());
            avatarView.setImage(image);
        }
    }

    @FXML
    void signUp(ActionEvent event) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(usernameField.getText());
        userEntity.setPassword(passwordField.getText());
        userEntity.setFullName(nameField.getText());
        boolean rs = ApiService.signup(userEntity, imgFile);
        if(rs){
            success();
        }else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Hệ thống xảy ra lỗi");
            alert.showAndWait();
        }
    }

    @FXML
    void back(ActionEvent event) {
        Parent root;
        try {
            root =  FXMLLoader.load(getClass().getResource("/ui/login.fxml"));
            Scene scene = new Scene(root);
            StageListener.stage.close();
            StageListener.stage.setScene(scene);
            StageListener.stage.show();
        }
        catch (IOException e) {
            logger.error(" error: ", e);
        }
    }

    private void success(){
        Parent root;
        try {
            root =  FXMLLoader.load(getClass().getResource("/ui/login.fxml"));
            Scene scene = new Scene(root);
            StageListener.stage.close();
            StageListener.stage.setScene(scene);
            StageListener.stage.show();
        }
        catch (IOException e) {
            logger.error("signup error: ", e);
        }
    }

}
