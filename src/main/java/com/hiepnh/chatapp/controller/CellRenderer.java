package com.hiepnh.chatapp.controller;

import com.hiepnh.chatapp.model.FriendHistory;
import com.hiepnh.chatapp.session.UserSession;
import com.hiepnh.chatapp.utils.AppUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.apache.commons.lang.ArrayUtils;

import java.io.InputStream;
import java.util.Objects;


class CellRenderer implements Callback<ListView<FriendHistory>,ListCell<FriendHistory>>{
        @Override
    public ListCell<FriendHistory> call(ListView<FriendHistory> p) {

        ListCell<FriendHistory> cell = new ListCell<>(){
            @Override
            protected void updateItem(FriendHistory user, boolean bln) {
                super.updateItem(user, bln);
                setGraphic(null);
                setText(null);
                if (user != null) {
                    HBox hBox = new HBox();
                    Text name = new Text(user.getInteractionUser().getInteraction().getUsername());
                    name.setStyle("-fx-font: 20 arial;");

                    ImageView statusImageView = new ImageView();
                    int status = user.getStatus();
                    Image statusImage;
                    if(status == 1){
                        statusImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("ui/icons/online.png")).toString(), 16, 16,true,true);
                    }else{
                        statusImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("ui/icons/away.png")).toString(), 16, 16,true,true);
                    }
                    statusImageView.setImage(statusImage);
                    ImageView pictureImageView = new ImageView();
                    byte[] data;
                    try {
                        data = ArrayUtils.toPrimitive(user.getInteractionUser().getInteraction().getAvatar());
                    }catch (Exception ex){
                        data = null;
                    }
                    Image image = AppUtils.convertByteArrayToImage(data, 50, 50);

//                    Image image = new Image(getClass().getClassLoader().getResource("ui/icons/default.png").toString(),50,50,true,true);
//                    circle.setFill(new ImagePattern(image));
                    pictureImageView.setImage(image);
                    VBox vBox = new VBox();

                    HBox msgBox = new HBox();
                    Text message = new Text(user.getInteractionUser().getContent());
                    name.setStyle("-fx-font: 14 arial;");
                    Long timeValue = Long.valueOf(user.getInteractionUser().getTime());
                    Text time = new Text("  " + AppUtils.convertTimeToStringMessage2(timeValue));
                    time.setStyle("-fx-font: 14 arial;");
                    msgBox.getChildren().addAll(message, time);

                    vBox.setAlignment(Pos.CENTER_LEFT);
                    vBox.getChildren().addAll(name, msgBox);
                    vBox.setMinWidth(100);


                    hBox.getChildren().addAll(pictureImageView, statusImageView, vBox);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    HBox.setMargin(vBox, new Insets(0, 0, 0, 15));

                    setGraphic(hBox);
                }
            }
        };
        return cell;
    }
}