package com.hiepnh.chatapp.controller;

import com.hiepnh.chatapp.StageListener;
import com.hiepnh.chatapp.common.MessageType;
import com.hiepnh.chatapp.executor.PackHandler;
import com.hiepnh.chatapp.model.Message;
import com.hiepnh.chatapp.netty.NettyClient;
import com.hiepnh.chatapp.session.UserSession;
import com.hiepnh.chatapp.utils.AppUtils;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class VideoController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);
    private static final int CAMERA_ID = 0;
    private final int FRAME_RATE = 20;
    private final int GOP_LENGTH_IN_FRAMES = 10;
    private final int captureWidth = 600;
    private final int captureHeight = 600;
    private ScheduledExecutorService videoExecutor;
    private FrameGrabber grabber;
    private FFmpegFrameRecorder recorder;
    private boolean cameraRunning = false;
    private Java2DFrameConverter converter;
    private PackHandler packHandler;
    private NettyClient nettyClient;
    private OutputStream os;
    private boolean accepted = false;

    private Boolean disableCamera = false;

    @FXML
    private ImageView otherView;

    @FXML
    private ImageView yourView;

    @FXML
    private Button toggleBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.load("/opt/opencv-4.5.2/build/lib/libopencv_java452.so");
        logger.info("video start");
        grabber = new OpenCVFrameGrabber(CAMERA_ID);
        grabber.setImageWidth(captureWidth);
        grabber.setImageHeight(captureHeight);


        try {
            grabber.start();

        } catch (Exception ex) {
            logger.error("grabber error: ", ex);
        }
        converter = new Java2DFrameConverter();

        nettyClient = NettyClient.getInstance();
        packHandler = (PackHandler) nettyClient.getWatcher();

        videoExecutor = Executors.newSingleThreadScheduledExecutor();
//        videoExecutor.schedule()

        startCamera();
    }

    @FXML
    void toggleCam(ActionEvent event) {
        disableCamera = !disableCamera;
        if(disableCamera){
            toggleBtn.setText("Enable Cam");
        }else{
            toggleBtn.setText("Disable Cam");
        }

    }

    private void startCamera() {
        Thread videoThread = new Thread(() -> {
            try {
                int i = 0;
                while (!Thread.interrupted()) {
                    Frame frame = grabber.grab();
                    if (frame.image != null) {
                        final Image image = SwingFXUtils.toFXImage(converter.convert(frame), null);
                        byte[] data = AppUtils.convertImageToByteArray(image);
                        if (data != null) {
                            Platform.runLater(() -> yourView.setImage(image));
//                        Buffer buff = frame.image[0];
//                        ByteBuffer byteBuffer = (ByteBuffer) buff;
//                        byte[] data = new byte[byteBuffer.remaining()];
//                        byteBuffer.get(data);

//                        Image img = AppUtils.convertByteArrayToImage(data, 640, 480);
//                        Platform.runLater(() -> otherView.setImage(img));

                            Message videoMsg = new Message();
                            videoMsg.setTag(MessageType.CALL);
                            logger.info("disableCamera: {}", disableCamera);
                            if(disableCamera){
                                byte[] data2 = new byte[1];
                                data2[0] = 0;
                                videoMsg.setVideoData(data2);
                            }else {
                                videoMsg.setVideoData(data);
                            }
                            nettyClient.sendMessage(videoMsg);
                        } else {
                            logger.error("data error");
                        }
                    }
                    Thread.sleep(50);
                }
            } catch (Exception ex) {
                logger.error("ex : ", ex);
            }
        });
        videoThread.start();


//            videoExecutor.scheduleAtFixedRate(recordThread, 0, 50, TimeUnit.MILLISECONDS);
    }


    private void stopExecutor() {
        if (videoExecutor != null && !videoExecutor.isShutdown()) {
            try {

                videoExecutor.shutdown();
                videoExecutor.awaitTermination(50, TimeUnit.MILLISECONDS);

                grabber.stop();
                grabber.close();
            } catch (InterruptedException | FrameGrabber.Exception e) {

                logger.error("Exception in stopping the frame capture, trying to release the camera now... ", e);
            }
        }
    }

    private void updateViewImage(Frame frame) {
        final Image image = SwingFXUtils.toFXImage(converter.convert(frame), null);
        Platform.runLater(() -> yourView.setImage(image));
    }

    @FXML
    void endCall(ActionEvent event) {
        stopExecutor();
        Parent root;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/ui/app.fxml")));
            Scene scene = new Scene(root);
            StageListener.stage.close();
            StageListener.stage.setScene(scene);
            UserSession.getInstance().setUser(null);
            StageListener.stage.show();
        } catch (IOException e) {
        }
    }

    private void initRecorder(OutputStream os) {
        recorder = new FFmpegFrameRecorder(os, 600, 600);
        recorder.setInterleaved(true);
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "ultrafast");

        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("mpegts");
        // FPS (frames per second)
        recorder.setFrameRate(FRAME_RATE);
        recorder.setGopSize(GOP_LENGTH_IN_FRAMES);
        recorder.setAudioOption("crf", "0");

        try {
            recorder.start();

        } catch (FrameRecorder.Exception ex) {

        }
    }

    private void test() {
        if (cameraRunning) {
            cameraRunning = false;
            stopExecutor();
            return;
        } else {
            cameraRunning = true;
            try {
                while (!Thread.interrupted()) {
                    Frame frame = grabber.grab();
                    if (frame.image != null) {

                        final Image image = SwingFXUtils.toFXImage(converter.convert(frame), null);
                        Platform.runLater(() -> yourView.setImage(image));

                        Buffer buff = frame.image[0];
                        ByteBuffer byteBuffer = (ByteBuffer) buff;
                        byte[] data = new byte[byteBuffer.remaining()];
                        byteBuffer.get(data);
                        logger.info("data : {}", data.length);
                        Image img = AppUtils.convertByteArrayToImage(data, 600, 600);
                        Platform.runLater(() -> otherView.setImage(img));

                        Message videoMsg = new Message();
                        videoMsg.setTag(MessageType.CALL);
                        videoMsg.setVideoData(data);
                        nettyClient.sendMessage(videoMsg);
                    }
                    Thread.sleep(50);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

}
