package com.hiepnh.chatapp.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hiepnh.chatapp.executor.WebcamListener;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

/**
 *
 * @author son
 */
public class Camera implements Runnable, WebcamListener {

    final private int FRAME_RATE = 25;
    final private int GOP_LENGTH_IN_FRAMES = 10;
    public long startTime = 0;
    private long videoTS = 0;
    public boolean isCall;
    private FrameGrabber grabber;
    private FFmpegFrameRecorder recorder;
    private List<WebcamListener> listeners = new ArrayList<>();
    private SourceDataLine soundLine;
    private OutputStream os;
    private InputStream is;
    public boolean recording = false;
    AudioFormat audioFormat = null;
    DataLine.Info info = null;

    //Webcams
    public Camera() {
        grabber = new OpenCVFrameGrabber(0);
        try {
            grabber.start();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Camera IP
    public Camera(String mis) {
        grabber = new FFmpegFrameGrabber(mis);
        try {
            grabber.start();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Camera(InputStream is, ImageView iv) {
        Thread aThread = new Thread(() -> {
            try {
                final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(is);
                //final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("rtsp://127.0.0.1:8443/live/livestream?deviceid=123abcdef32153421");
                //final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("rtsp://admin:kiosk123@192.168.68.108");
                grabber.start();

                final Java2DFrameConverter converter = new Java2DFrameConverter();

                ExecutorService executor = Executors.newSingleThreadExecutor();

                while (!Thread.interrupted()) {
                    Frame frame = grabber.grab();
                    if (frame == null) {
                        break;
                    }
                    if (frame.image != null) {
                        final Image image = SwingFXUtils.toFXImage(converter.convert(frame), null);
                        Platform.runLater(new Runnable() {
                            public void run() {
                                iv.setImage(image);
                            }
                        });
                    } else if (frame.samples != null) {

                        final ShortBuffer channelSamplesShortBuffer = (ShortBuffer) frame.samples[0];
                        channelSamplesShortBuffer.rewind();

                        final ByteBuffer outBuffer = ByteBuffer.allocate(channelSamplesShortBuffer.capacity() * 2);

                        for (int i = 0; i < channelSamplesShortBuffer.capacity(); i++) {
                            short val = channelSamplesShortBuffer.get(i);
                            outBuffer.putShort(val);
                        }

                        try {
                            executor.submit(new Runnable() {
                                public void run() {
                                    //soundLine.write(outBuffer.array(), 0, outBuffer.capacity());
                                    outBuffer.clear();
                                }
                            }).get();
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                        }

                    }
                }
                executor.shutdownNow();
                executor.awaitTermination(10, TimeUnit.SECONDS);

                grabber.stop();
                grabber.release();
                Platform.exit();
            } catch (InterruptedException | ExecutionException | FrameGrabber.Exception exception) {
                Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, exception);
                //System.exit(1);
            }
        });
        aThread.start();
    }

    public void initRecorder(OutputStream os, ImageView iv, int audioChannels) {
        // (int) iv.getBoundsInLocal().getWidth(), (int) iv.getBoundsInLocal().getHeight(),
        this.os = os;
        recorder = new FFmpegFrameRecorder(os, 480, 320, audioChannels);

        recorder.setInterleaved(true);
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "ultrafast");
//        recorder.setVideoOption("crf", "28");
//        recorder.setVideoBitrate(2000000);

        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        //recorder.setVideoCodec(avcodec.AV_CODEC_ID_H265);
        //recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
        //recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG2VIDEO);
        recorder.setFormat("mpegts");
        // FPS (frames per second)
        recorder.setFrameRate(FRAME_RATE);
        recorder.setGopSize(GOP_LENGTH_IN_FRAMES);
        recorder.setAudioOption("crf", "0");

        // Highest quality
        if (audioChannels > 0) {
            recorder.setAudioQuality(0);
            recorder.setAudioBitrate(44000);
            recorder.setSampleRate(22050);
            recorder.setAudioChannels(audioChannels);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        }

        try {
            recorder.start();

        } catch (FrameRecorder.Exception ex) {
            Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addListener(WebcamListener wl) {
        listeners.add(wl);
    }

    @Override
    public void run() {
        this.addListener(this);
        Frame capturedFrame = null;
        while (true) {
            try {
                if ((capturedFrame = grabber.grab()) == null) {
                    break;
                }
            } catch (FrameGrabber.Exception ex) {
                Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (WebcamListener listener : listeners) {
                listener.hasImage(capturedFrame);
            }
        }
        try {
            recorder.stop();
            grabber.stop();
        } catch (FrameRecorder.Exception | FrameGrabber.Exception ex) {
            Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void record(Frame fr) {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        videoTS = 1000 * (System.currentTimeMillis() - startTime);
        if (videoTS > recorder.getTimestamp()) {
            System.out.println(
                    "Lip-flap correction: "
                            + videoTS + " : "
                            + recorder.getTimestamp() + " -> "
                            + (videoTS - recorder.getTimestamp()));

            // We tell the recorder to write this frame at this timestamp
            recorder.setTimestamp(videoTS);
        }
        try {
            // Send the frame to the org.bytedeco.javacv.FFmpegFrameRecorder
            recorder.record(fr);
            recording = true;
        } catch (FrameRecorder.Exception ex) {
            System.err.println("Cannot write, check network connection");
        }
    }

    public void stop() {
        try {
            grabber.stop();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void hasImage(Frame fr) {
        if (isCall && recorder != null) {
            record(fr);
        }
    }
}
