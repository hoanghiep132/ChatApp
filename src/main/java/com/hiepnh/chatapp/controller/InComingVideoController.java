package com.hiepnh.chatapp.controller;

import com.hiepnh.chatapp.common.MessageType;
import com.hiepnh.chatapp.executor.PackHandler;
import com.hiepnh.chatapp.model.Message;
import com.hiepnh.chatapp.netty.NettyClient;
import com.hiepnh.chatapp.utils.AppUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

@Component
public class InComingVideoController implements Initializable {

    @FXML
    private ImageView otherView;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean running = false;

    private PackHandler packHandler;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("incomingVideo");
        running = true;
        packHandler = (PackHandler) NettyClient.getInstance().getWatcher();
        listenQueue();
    }



    private void listenQueue(){
//        new Thread(() -> {
//            while (true){
//                Message newMsg = packHandler.messageQueue.poll();
//                if(newMsg == null){
//                    continue;
//                }
//                if(newMsg.getTag() == MessageType.CALL_ACCEPT){
//                    accepted = true;
//                }else if(newMsg.getTag() == MessageType.CALL_REJECT){
//
//                }else if(newMsg.getTag() == MessageType.USER_UNAVAILABLE){
//
//                }else if(newMsg.getTag() == MessageType.CALL_END){
//                    accepted = false;
//                }else if(newMsg.getTag() == MessageType.CALL){
//                }
//            }
//        }).start();

        new Thread(() -> {
            while (running){
                Message newMsg = packHandler.videoQueue.poll();
                if(newMsg == null){
                    continue;
                }
                if(newMsg.getTag() == MessageType.CALL){
                    if(newMsg.getVideoData().length == 1){
                        final  Image image = new Image(getClass().getClassLoader().getResource("ui/img/dis_cam.png").toString(),600,600,true,true);
                        Platform.runLater(() -> otherView.setImage(image));
                    }else {
                        byte[] data = newMsg.getVideoData();
                        final  Image image = AppUtils.convertByteArrayToImage(data, 600, 600);
                        Platform.runLater(() -> otherView.setImage(image));
                    }
                }
            }
        }).start();
    }

    @FXML
    void endCall(ActionEvent event){
        running = false;
    }
}
