package com.hiepnh.chatapp.controller;

import com.hiepnh.chatapp.StageListener;
import com.hiepnh.chatapp.common.MessageType;
import com.hiepnh.chatapp.model.Message;
import com.hiepnh.chatapp.netty.NettyClient;
import com.hiepnh.chatapp.session.DataStorage;
import com.hiepnh.chatapp.session.UserSession;
import com.hiepnh.chatapp.utils.AppUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

@Component
public class CallRequestController implements Initializable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String friendName;

    private byte[] friendImgData;

    @FXML
    private ImageView friendImg;

    @FXML
    private Label friendNameLabel;

    private NettyClient nettyClient;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nettyClient = NettyClient.getInstance();
        String username = AppUtils.parseString(DataStorage.getInstance().getAttribute("call_username"));
        logger.info("open view : {}", username);
        setFriendName(username);


        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        accept();
    }

    private void accept(){
        Message message = new Message();
        message.setTag(MessageType.CALL_ACCEPT);
        nettyClient.sendMessage(message);
        Parent root;
        try {
            root =  FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/ui/incomingVideo.fxml")));
            Scene scene = new Scene(root);
            StageListener.stage.close();
            StageListener.stage.setScene(scene);
            StageListener.stage.show();
        }
        catch (IOException e) {
            logger.error("acceptCall error: ", e);
        }
    }

    @FXML
    void acceptCall(ActionEvent event) {
        accept();
    }

    @FXML
    void rejectCall(ActionEvent event) {
        Message message = new Message();
        message.setTag(MessageType.CALL_REJECT);
        nettyClient.sendMessage(message);
        Parent root;
        try {
            root =  FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/ui/videoView.fxml")));
            Scene scene = new Scene(root);
            StageListener.stage.close();
            StageListener.stage.setScene(scene);
            StageListener.stage.show();
        }
        catch (Exception e) {
            logger.error("rejectCall error: ", e);
        }
    }

    public void setFriendName(String friendName) {
        friendNameLabel.setText(friendName);
    }

    public void setFriendImgData(byte[] friendImgData) {
        this.friendImgData = friendImgData;
    }
}
