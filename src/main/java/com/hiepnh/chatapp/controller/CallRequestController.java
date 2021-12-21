package com.hiepnh.chatapp.controller;

import com.hiepnh.chatapp.StageListener;
import com.hiepnh.chatapp.common.MessageType;
import com.hiepnh.chatapp.model.Message;
import com.hiepnh.chatapp.netty.NettyClient;
import com.hiepnh.chatapp.session.UserSession;
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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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


    }

    @FXML
    void acceptCall(ActionEvent event) {
        Message message = new Message();
        message.setTag(MessageType.CALL_ACCEPT);
        nettyClient.sendMessage(message);
        Parent root;
        try {
            root =  FXMLLoader.load(getClass().getResource("/ui/videoView.fxml"));
            Scene scene = new Scene(root);
            StageListener.stage.close();
            StageListener.stage.setScene(scene);
            UserSession.getInstance().setUser(null);
            StageListener.stage.show();
        }
        catch (IOException e) {
            logger.error("log out error: ", e);
        }
    }

    @FXML
    void rejectCall(ActionEvent event) {
        Message message = new Message();
        message.setTag(MessageType.CALL_REJECT);
        nettyClient.sendMessage(message);
        Parent root;
        try {
//            root =  FXMLLoader.load(getClass().getResource("/ui/videoView.fxml"));
//            Scene scene = new Scene(root);
            StageListener.stage.close();
//            StageListener.stage.setScene(scene);
            UserSession.getInstance().setUser(null);
            StageListener.stage.show();
        }
        catch (Exception e) {
            logger.error("log out error: ", e);
        }
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public void setFriendImgData(byte[] friendImgData) {
        this.friendImgData = friendImgData;
    }
}
