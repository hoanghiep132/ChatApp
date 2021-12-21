package com.hiepnh.chatapp.controller;

import com.hiepnh.chatapp.StageListener;
import com.hiepnh.chatapp.bubble.BubbleSpec;
import com.hiepnh.chatapp.bubble.BubbledLabel;
import com.hiepnh.chatapp.common.Constant;
import com.hiepnh.chatapp.common.MessageType;
import com.hiepnh.chatapp.entities.InteractionUserEntity;
import com.hiepnh.chatapp.entities.MessageEntity;
import com.hiepnh.chatapp.executor.PackHandler;
import com.hiepnh.chatapp.model.FriendHistory;
import com.hiepnh.chatapp.model.Message;
import com.hiepnh.chatapp.netty.NettyClient;
import com.hiepnh.chatapp.service.ApiService;
import com.hiepnh.chatapp.session.UserSession;
import com.hiepnh.chatapp.traynotifications.animations.AnimationType;
import com.hiepnh.chatapp.traynotifications.notification.TrayNotification;
import com.hiepnh.chatapp.utils.AppUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ChatController implements Initializable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String currentFriend = null;

    private final String defaultPath = "ui/icons/default.png";

    private List<FriendHistory> currentOnlineUserList = new ArrayList<>();

    private NettyClient nettyClient;

    private int offset = 0;
    private int limit = 0;

    @FXML
    private Label typingLabel;

    @FXML
    private TextField messageInput;

    @FXML
    private ListView<HBox> messageList;

    @FXML
    private Label usernameLabel;

    private PackHandler packHandler;

    @FXML
    private ListView<FriendHistory> onlineUserList;

    @FXML
    private Label friendNameLabel;

    @FXML
    private Circle yourCircleImg;

    @FXML
    private Circle friendCircleImg;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initCurrentOnlineUserList();
        initNetty();
        updateMessageTask();
        initCircle();
        setYourImg();
        setYourUsernameLabel();
    }

    @FXML
    void videoCall(ActionEvent event){
        if(currentFriend.isBlank()){
            return;
        }
        Message message = new Message();
        message.setTag(MessageType.CALL_REQUEST);
        message.setReceiver(currentFriend);
        nettyClient.sendMessage(message);

        Parent root;
        try {
            root =  FXMLLoader.load(getClass().getResource("/ui/videoView.fxml"));
            Scene scene = new Scene(root);
//            StageListener.stage.close();
            StageListener.stage.setScene(scene);
            UserSession.getInstance().setUser(null);
            StageListener.stage.show();
        }
        catch (IOException e) {
            logger.error("log out error: ", e);
        }
    }

    @FXML
    void logout(ActionEvent event) {
        Parent root;
        try {
            root =  FXMLLoader.load(getClass().getResource("/ui/login.fxml"));
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
    void sendMsg(MouseEvent event) {
        String msg = messageInput.getText();
        if (!msg.isBlank()) {
            Message message = new Message();
            message.setContent(msg);
            message.setSender(UserSession.getInstance().getUser().getUsername());
            message.setReceiver(currentFriend);
            message.setTime(System.currentTimeMillis());
            message.setTag(MessageType.MESSAGE);
            addToChat(message);
            updateLeftBar(message, currentFriend);
            nettyClient.sendMessage(message);
            messageInput.clear();
        }
    }

    @FXML
    void sendMsgByKey(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            String msg = messageInput.getText();
            if (msg.isBlank()) {
               return;
            }
            Message message = new Message();
            message.setContent(msg);
            message.setSender(UserSession.getInstance().getUser().getUsername());
            message.setReceiver(currentFriend);
            message.setTag(MessageType.MESSAGE);
            message.setTime(System.currentTimeMillis());
            addToChat(message);
            updateLeftBar(message, currentFriend);
            nettyClient.sendMessage(message);
            messageInput.clear();
        }else {
            if(messageInput.getText().isBlank()){
               return;
            }
            Message message = new Message();
            message.setSender(UserSession.getInstance().getUser().getUsername());
            message.setReceiver(Constant.FRIEND);
            message.setTag(MessageType.TYPING);
            nettyClient.sendMessage(message);
        }
    }

    private void updateMessageList(int userId, int friendId){
        List<MessageEntity> messageEntities = ApiService.getListMessage(userId, friendId,offset , offset + 15);
        if(messageEntities == null || messageEntities.isEmpty()){
            logger.info("Conversation empty");
            return;
        }
//        addListChat(messageEntities);
        Image otherImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResource(defaultPath)).toString());
        byte[] imgData = ArrayUtils.toPrimitive(UserSession.getInstance().getUser().getAvatar());
        Image yourImage = AppUtils.convertByteArrayToImage(imgData, 80, 80);
        limit += messageEntities.size();
        for(MessageEntity messageEntity: messageEntities){
            Message message = new Message();
            message.setSender(messageEntity.getSender().getUsername());
            message.setReceiver(messageEntity.getReceiver().getUsername());
            message.setTime(messageEntity.getTime());
            message.setContent(messageEntity.getContent());
            addToChat(message, otherImage, yourImage);
            offset++;
        }
    }

    @FXML
    void chooseCurrentUser(MouseEvent event) {
        if (onlineUserList.getSelectionModel().getSelectedItems().isEmpty()) {
            return;
        }
        FriendHistory friendHistory = onlineUserList.getSelectionModel().getSelectedItems().get(0);
        this.currentFriend = friendHistory.getInteractionUser().getInteraction().getUsername();
        byte[] data;
        try {
            data = ArrayUtils.toPrimitive(friendHistory.getInteractionUser().getInteraction().getAvatar());
        }catch (Exception ex){
            data = null;
        }
        Image image = AppUtils.convertByteArrayToImage(data, 80, 80);
        friendCircleImg.setFill(new ImagePattern(image));
        setFriendInfo();
        int userId = UserSession.getInstance().getUser().getId();
        int friendId = friendHistory.getInteractionUser().getInteraction().getId();
        messageList.getItems().clear();
        offset = 0;
        limit = 0;
        updateMessageList(userId, friendId);
        logger.info("offset : {}", offset);
    }

    private synchronized void initCurrentOnlineUserList() {
        List<Map> entities = ApiService.getInteraction(UserSession.getInstance().getUser().getId());
        if (entities == null || entities.isEmpty()) {
            return;
        }
        for (Map item : entities) {
            FriendHistory fh = new FriendHistory();
            InteractionUserEntity entity = AppUtils.convertMapToObject(item, InteractionUserEntity.class);
            fh.setInteractionUser(entity);
            currentOnlineUserList.add(fh);
        }
        logger.info("initCurrentOnlineUserList done!");
    }

    private void initCircle() {
        yourCircleImg.setStroke(Color.SEAGREEN);
        yourCircleImg.setEffect(new DropShadow(+25d, 0d, +2d, Color.DARKSEAGREEN));

        friendCircleImg.setStroke(Color.SEAGREEN);
        friendCircleImg.setEffect(new DropShadow(+25d, 0d, +2d, Color.DARKSEAGREEN));

        File file = new File("src/main/resources/" + defaultPath);
        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error("load avatar error : ", e);
            return;
        }
        Image img = new Image(is);
        friendCircleImg.setFill(new ImagePattern(img));
    }

    private synchronized void addToChat(Message message, Image otherImg, Image yourImg) {
        Task<HBox> othersMessages = new Task<HBox>() {
            @Override
            public HBox call() throws Exception {
                ImageView profileImage = new ImageView(otherImg);
                profileImage.setFitHeight(40);
                profileImage.setFitWidth(40);

                BubbledLabel bl6 = new BubbledLabel();
                bl6.setStyle("-fx-font: 18 arial;");
                bl6.setText(message.getContent());
                bl6.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, null, null)));
                bl6.setBubbleSpec(BubbleSpec.FACE_LEFT_CENTER);
                HBox x = new HBox();
                VBox vBox = new VBox();
                String time = AppUtils.convertTimeToStringMessage(message.getTime());
                Text timeText = new Text(time);
                timeText.setTextAlignment(TextAlignment.RIGHT);
                timeText.setStyle("-fx-font: 12 arial;");

                vBox.getChildren().addAll(bl6, timeText);
                x.getChildren().addAll(profileImage, vBox);
                return x;
            }
        };
        othersMessages.setOnSucceeded(event -> messageList.getItems().add(limit-offset,othersMessages.getValue()));

        Task<HBox> yourMessages = new Task<>() {
            @Override
            public HBox call(){
                ImageView profileImage = new ImageView(yourImg);
                profileImage.setFitHeight(40);
                profileImage.setFitWidth(40);

                BubbledLabel bl6 = new BubbledLabel();
                bl6.setText(message.getContent());
                bl6.setStyle("-fx-font: 18 arial;");
                bl6.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE,
                        null, null)));

                HBox x = new HBox();
                x.setMaxWidth(messageList.getWidth() - 20);
                x.setAlignment(Pos.TOP_RIGHT);

                VBox vBox = new VBox();
                String time = AppUtils.convertTimeToStringMessage(message.getTime());
                Text timeText = new Text(time);
                timeText.setTextAlignment(TextAlignment.LEFT);
                timeText.setStyle("-fx-font: 12 arial;");

                vBox.getChildren().addAll(bl6, timeText);
                vBox.setAlignment(Pos.CENTER_RIGHT);

                bl6.setAlignment(Pos.TOP_RIGHT);
                bl6.setStyle("-fx-font: 16 arial;");

                x.setSpacing(10);
                x.getChildren().addAll(vBox, profileImage);
                return x;
            }
        };
        yourMessages.setOnSucceeded(event -> messageList.getItems().add(limit -offset, yourMessages.getValue()));

        if (message.getSender().equals(usernameLabel.getText())) {
            Thread t2 = new Thread(yourMessages);
            t2.setDaemon(true);
            t2.start();
        } else {
            Thread t = new Thread(othersMessages);
            t.setDaemon(true);
            t.start();
        }
    }

    private synchronized void addToChat(Message message) {
        Task<HBox> othersMessages = new Task<HBox>() {
            @Override
            public HBox call() throws Exception {
                Image image = new Image(Objects.requireNonNull(getClass().getClassLoader().getResource(defaultPath)).toString());
                ImageView profileImage = new ImageView(image);
                profileImage.setFitHeight(32);
                profileImage.setFitWidth(32);
                BubbledLabel bl6 = new BubbledLabel();
                bl6.setText(message.getContent());
                bl6.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, null, null)));
                bl6.setBubbleSpec(BubbleSpec.FACE_LEFT_CENTER);
                bl6.setCursor(Cursor.HAND);
                HBox x = new HBox();
                VBox vBox = new VBox();
                String time = AppUtils.convertTimeToStringMessage(message.getTime());
                Text timeText = new Text(time);
                timeText.setTextAlignment(TextAlignment.RIGHT);
                timeText.setStyle("-fx-font: 12 arial;");

                vBox.getChildren().addAll(bl6, timeText);
                x.getChildren().addAll(profileImage, vBox);
                return x;
            }
        };
        othersMessages.setOnSucceeded(event -> messageList.getItems().add(othersMessages.getValue()));

        Task<HBox> yourMessages = new Task<HBox>() {
            @Override
            public HBox call() throws Exception {
                Image image = new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("ui/img/hiep31.jpg")).toString());
                ImageView profileImage = new ImageView(image);
                profileImage.setFitHeight(32);
                profileImage.setFitWidth(32);

                BubbledLabel bl6 = new BubbledLabel();
                bl6.setText(message.getContent());

                bl6.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE,
                        null, null)));
                HBox x = new HBox();
                x.setMaxWidth(messageList.getWidth() - 20);
                x.setAlignment(Pos.TOP_RIGHT);

                VBox vBox = new VBox();
                String time = AppUtils.convertTimeToStringMessage(message.getTime());
                Text timeText = new Text(time);
                timeText.setTextAlignment(TextAlignment.LEFT);
                timeText.setStyle("-fx-font: 12 arial;");

                vBox.getChildren().addAll(bl6, timeText);
                vBox.setAlignment(Pos.CENTER_RIGHT);

                bl6.setAlignment(Pos.TOP_RIGHT);
                bl6.setStyle("-fx-font: 16 arial;");

                x.setSpacing(10);
                x.getChildren().addAll(vBox, profileImage);
                return x;
            }
        };
        yourMessages.setOnSucceeded(event -> messageList.getItems().add(yourMessages.getValue()));

        if (message.getSender().equals(usernameLabel.getText())) {
            Thread t2 = new Thread(yourMessages);
            t2.setDaemon(true);
            t2.start();
        } else {
            Thread t = new Thread(othersMessages);
            t.setDaemon(true);
            t.start();
        }
    }

    private synchronized void updateListUserOnline(){
        List<FriendHistory> friendHistories = this.currentOnlineUserList.stream().peek(item -> {
            long time = System.currentTimeMillis();
            item.setStatus(0);
            item.setLastUpdate(time);
        }).collect(Collectors.toList());
        this.currentOnlineUserList = friendHistories;
        Platform.runLater(() -> {
            ObservableList<FriendHistory> users = FXCollections.observableList(this.currentOnlineUserList);
            onlineUserList.setItems(users);
            onlineUserList.setCellFactory(new CellRenderer());
        });
    }

    private synchronized void updateOnlineList(String input) {
        if (input.isBlank() ) {
            updateListUserOnline();
            return;
        }
        List<String> userList = Arrays.asList(input.split(";"));
        if (userList.isEmpty()) {
            updateListUserOnline();
            return;
        }
        List<FriendHistory> friendHistories = this.currentOnlineUserList.stream().peek(item -> {
            String username = item.getInteractionUser().getInteraction().getUsername();
            long time = System.currentTimeMillis();
            if (userList.contains(username)) {
                item.setStatus(1);
                item.setLastUpdate(time);
            } else {
                item.setStatus(0);
                if (item.getLastUpdate() == null) {
                    item.setLastUpdate(time);
                }
            }
        }).collect(Collectors.toList());
        this.currentOnlineUserList = friendHistories;
        Platform.runLater(() -> {
            ObservableList<FriendHistory> users = FXCollections.observableList(this.currentOnlineUserList);
            onlineUserList.setItems(users);
            onlineUserList.setCellFactory(new CellRenderer());
        });
    }

    private synchronized void updateLeftBar(Message newMsg, String username){
        this.currentOnlineUserList.forEach(
                e -> {
                    if(e.getInteractionUser().getInteraction().getUsername().equals(username)){
                        e.getInteractionUser().setContent(newMsg.getContent());
                        e.getInteractionUser().setTime(newMsg.getTime());
                    }
                }
        );
        Comparator<FriendHistory> comparator;
        comparator = Comparator.comparing(h -> h.getInteractionUser().getTime());
        List<FriendHistory> friendHistories = this.currentOnlineUserList.stream()
                .sorted(comparator.reversed())
                .collect(Collectors.toList());
        this.currentOnlineUserList = friendHistories;
        Platform.runLater(() -> {
            ObservableList<FriendHistory> users = FXCollections.observableList(this.currentOnlineUserList);
            onlineUserList.setItems(users);
            onlineUserList.setCellFactory(new CellRenderer());
        });
    }

    public void setYourUsernameLabel() {
        this.usernameLabel.setText(UserSession.getInstance().getUser().getUsername());
    }

    public void setYourImg() {
        try {
            byte[] data = ArrayUtils.toPrimitive(UserSession.getInstance().getUser().getAvatar());
            Image img = AppUtils.convertByteArrayToImage(data);
            yourCircleImg.setFill(new ImagePattern(img));
        }catch (Exception ex){
            logger.error("Load image : ", ex);
        }

    }

    public void setFriendInfo() {
        Optional<FriendHistory> friendHistoryOptional = currentOnlineUserList.stream()
                .filter(e -> e.getInteractionUser().getInteraction().getUsername().equals(currentFriend))
                .findFirst();
        if(friendHistoryOptional.isEmpty()){
            return;
        }
        Byte[] data = friendHistoryOptional.get().getInteractionUser().getInteraction().getAvatar();
        friendNameLabel.setText(currentFriend);
        Image img = AppUtils.convertByteArrayToImage(ArrayUtils.toPrimitive(data));
        if(img == null || img.isError()){
            img = new Image(getClass().getClassLoader().getResource(defaultPath).toString());
        }
        try {
            friendCircleImg.setFill(new ImagePattern(img));
        }catch (Exception ex){
            logger.error("load img : ", ex);
        }
    }

    private void updateMessageTask() {
        Thread task = new Thread(() -> {
            Message newMsg = packHandler.messageQueue.poll();
            if (newMsg != null) {
                if(newMsg.getTag() == MessageType.TYPING){
                    Platform.runLater(() -> typingLabel.setText(Constant.TYPING_TEXT));
                }else {
                    Platform.runLater(() -> typingLabel.setText(""));
                }
                if (newMsg.getTag() == MessageType.MESSAGE) {
                    Message newChatMsg = newMsg;
                    if(currentFriend != null && !currentFriend.equals("")){
                        if(StageListener.show){
                            if (newChatMsg.getSender().equals(currentFriend)) {
                                Platform.runLater(() -> addToChat(newChatMsg));
                            }
                        }else {
                            Platform.runLater(() -> addToChat(newChatMsg));
                            newMsgNotification(newChatMsg);
                        }
                    }
                    updateLeftBar(newChatMsg, newChatMsg.getSender());
                } else if (newMsg.getTag() == MessageType.ONLINE) {
                    Message newChatMsg = newMsg;
                    updateOnlineList(newChatMsg.getContent());
                }else if(newMsg.getTag() == MessageType.CALL_REQUEST){
                    Parent root;
                    try {
                        root =  FXMLLoader.load(getClass().getResource("/ui/callRequestView.fxml"));
                        Scene scene = new Scene(root);
//                        StageListener.stage.close();
                        StageListener.stage.setScene(scene);
                        UserSession.getInstance().setUser(null);
                        StageListener.stage.show();
                    }
                    catch (IOException e) {
                        logger.error("log out error: ", e);
                    }
                }

            }
        });
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(task, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void newMsgNotification(Message msg) {
        Platform.runLater(() -> {
            TrayNotification tray = new TrayNotification();
            tray.setTitle("Tin nhắn mới!");
            Optional<FriendHistory> friendHistoryOptional = currentOnlineUserList.stream()
                    .filter(e -> e.getInteractionUser().getInteraction().getUsername().equals(msg.getSender()))
                    .findFirst();
            if(!friendHistoryOptional.isPresent()){
                return;
            }
            Byte[] data;
            try{
                data = friendHistoryOptional.get().getInteractionUser().getInteraction().getAvatar();
            }catch (Exception ex){
                data = null;
            }
            Image img = AppUtils.convertByteArrayToImage(ArrayUtils.toPrimitive(data));
            tray.setImage(img);
            tray.setMessage(msg.getSender() + " : " + msg.getContent());
            tray.setRectangleFill(Paint.valueOf("#2C3E50"));
            tray.setAnimationType(AnimationType.POPUP);
            tray.showAndDismiss(Duration.seconds(5));
        });
    }

    private void initNetty() {
        nettyClient = NettyClient.getInstance();
        packHandler = (PackHandler) nettyClient.getWatcher();
        nettyClient.start();
    }

    private void showLoading(){
    }


}