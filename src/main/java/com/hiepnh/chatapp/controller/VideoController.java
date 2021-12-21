package com.hiepnh.chatapp.controller;

import com.hiepnh.chatapp.StageListener;
import com.hiepnh.chatapp.common.MessageType;
import com.hiepnh.chatapp.executor.ExecutorWorker;
import com.hiepnh.chatapp.executor.PackHandler;
import com.hiepnh.chatapp.model.Message;
import com.hiepnh.chatapp.netty.NettyClient;
import com.hiepnh.chatapp.session.UserSession;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ResourceBundle;
import java.util.concurrent.*;

@Component
public class VideoController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    private ScheduledExecutorService videoExecutor;
    private ExecutorWorker executorWorker;
    private BlockingQueue<Object> queueSender;

    private static int cameraId = 0;
    private FrameGrabber grabber;
    private boolean cameraRunning = false;
    private Java2DFrameConverter converter;
    private PackHandler packHandler;

    private NettyClient nettyClient;

    private boolean accepted = false;

    @FXML
    private ImageView otherView;

    @FXML
    private ImageView yourView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.load("/opt/opencv-4.5.2/build/lib/libopencv_java452.so");
        grabber = new OpenCVFrameGrabber(cameraId);
        converter = new Java2DFrameConverter();

        nettyClient = NettyClient.getInstance();
        packHandler = (PackHandler) nettyClient.getWatcher();

        videoExecutor = Executors.newSingleThreadScheduledExecutor();
        queueSender = new LinkedBlockingQueue<>();
        executorWorker = new ExecutorWorker("ExcutorWorker", queueSender);
        executorWorker.execute();
        startCamera();

        listenQueue();
    }

    @FXML
    void endCall(ActionEvent event) {
        stopExecutor();
        Parent root;
        try {
            root =  FXMLLoader.load(getClass().getResource("/ui/app.fxml"));
            Scene scene = new Scene(root);
            StageListener.stage.close();
            StageListener.stage.setScene(scene);
            UserSession.getInstance().setUser(null);
            StageListener.stage.show();
        }
        catch (IOException e) {
        }
    }

    private void startCamera(){
        if(cameraRunning){
            cameraRunning = false;
            stopExecutor();
            return;
        }else {
            cameraRunning = true;
            Thread recordThread = new Thread(() -> {
                try {
                    grabber.start();
                    while (!videoExecutor.isShutdown()) {
                        logger.info("Time : {}", System.currentTimeMillis());
                        Frame frame = grabber.grab();
                        if (frame.image != null) {
                            updateViewImage(frame);
                        }else if(accepted && frame.samples != null){
                            final ShortBuffer channelSamplesShortBuffer = (ShortBuffer) frame.samples[0];
                            channelSamplesShortBuffer.rewind();
                            ByteBuffer outBuffer = ByteBuffer.allocate(channelSamplesShortBuffer.capacity() * 2);
                            for (int i = 0; i < channelSamplesShortBuffer.capacity(); i++) {
                                short val = channelSamplesShortBuffer.get(i);
                                outBuffer.putShort(val);
                            }
                            byte[] data = new byte[outBuffer.remaining()];
                            outBuffer.get(data);
                            Message videoMsg = new Message();
                            videoMsg.setTag(MessageType.CALL);
                            videoMsg.setVideoData(data);
                            nettyClient.sendMessage(videoMsg);
                        }
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            recordThread.start();
//            videoExecutor.scheduleAtFixedRate(recordThread, 0, 33, TimeUnit.MILLISECONDS);
        }
    }

    private void stopExecutor() {
        if (videoExecutor != null && !videoExecutor.isShutdown()) {
            try {
                // stop the timer
                videoExecutor.shutdown();
                videoExecutor.awaitTermination(33, TimeUnit.MILLISECONDS);

                // stop grabber
                grabber.stop();
                grabber.close();
            } catch (InterruptedException | FrameGrabber.Exception e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }
    }

    private void updateViewImage(Frame frame){
        final Image image = SwingFXUtils.toFXImage(converter.convert(frame), null);
        Platform.runLater(() -> yourView.setImage(image));
    }

    private void listenQueue(){
        new Thread(() -> {
            while (true){
                Message newMsg = packHandler.messageQueue.poll();
                if(newMsg == null){
                    continue;
                }
                if(newMsg.getTag() == MessageType.CALL_ACCEPT){
                    accepted = true;
                }else if(newMsg.getTag() == MessageType.CALL_REJECT){

                }else if(newMsg.getTag() == MessageType.USER_UNAVAILABLE){

                }else if(newMsg.getTag() == MessageType.CALL_END){

                }else if(newMsg.getTag() == MessageType.CALL){

                }
            }
        }).start();

    }
}
