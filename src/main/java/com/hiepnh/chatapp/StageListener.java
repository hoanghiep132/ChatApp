package com.hiepnh.chatapp;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class StageListener implements ApplicationListener<JavaFxApplication.StageReadyEvent> {

    private final String applicationTitle;

    private final Resource fxml;

    private final ApplicationContext ac;

    public static Stage stage = null;

    private static Stage secondaryStage = null;

    public StageListener(@Value("${application.ui.title}") String applicationTitle,
                         @Value("classpath:/ui/login.fxml") Resource fxml, ApplicationContext ac) {
        this.applicationTitle = applicationTitle;
        this.fxml = fxml;
        this.ac = ac;
    }

    public static boolean show = false;

    public static Stage getSecondaryStage() {
        if(secondaryStage == null){
            secondaryStage = new Stage();
        }
        return secondaryStage;
    }

    @Override
    public void onApplicationEvent(JavaFxApplication.StageReadyEvent stageReadyEvent) {
        try {
            stage = stageReadyEvent.getStage();
            stage.focusedProperty().addListener(e -> {
                show = !show;
                System.out.println("Window : " + show);
            });
            URL url = fxml.getURL();

            FXMLLoader fxmlLoader = new FXMLLoader(url);
            fxmlLoader.setControllerFactory(ac::getBean);

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 400, 600);
            stage.setTitle(this.applicationTitle);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
