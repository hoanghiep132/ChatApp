package com.hiepnh.chatapp.netty;


import com.hiepnh.chatapp.common.MessageType;
import com.hiepnh.chatapp.executor.PackHandler;
import com.hiepnh.chatapp.executor.Watcher;
import com.hiepnh.chatapp.model.Message;
import com.hiepnh.chatapp.session.UserSession;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NettyClient {
    private static boolean running = false;
    private static Channel channel;
    private final String HOST = "localhost";
    private final int PORT = 29000;
    private final int SCHEDULER_TIME = 5;
    private final int THREAD_NUMBER = 1;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Watcher watcher;

    private static NettyClient instance;

    private NettyClient(){
        watcher = new PackHandler();
    }

    public static NettyClient getInstance(){
        if(instance == null){
            instance = new NettyClient();
        }
        return instance;
    }

    public static void disconnect() {
        try {
            running = false;
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Watcher getWatcher() {
        return watcher;
    }

    public void start() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        boolean connected = false;
        while (!connected) {
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);

                ChannelInitializer channelInitializer = new ChatClientInitializer(watcher, this);
                b.handler(channelInitializer);
                channel = b.connect(HOST, PORT).sync().channel();

                ScheduledExecutorService executor = Executors.newScheduledThreadPool(THREAD_NUMBER);
                Runnable pingPongThread = new PingPongThread();
                executor.scheduleWithFixedDelay(pingPongThread, 0, SCHEDULER_TIME, TimeUnit.SECONDS);

                logger.info("Connect netty server successfully ");
                connected = true;
                running = true;
            } catch (Exception ex) {
                connected = false;
                logger.error("Connect netty server : ", ex);
            }
            if(!connected){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void connect() {
        new Thread(() -> {
            boolean connect = false;
            while (!connect) {
                try {
                    start();
                } catch (Throwable e) {
                    logger.error("Ex : ", e);
                }
                if (!connect) {
                    try {
                        Thread.sleep(2500);
                    } catch (Throwable e) {
                        logger.error("Ex: ", e);
                    }
                }
            }
        }).start();
    }

    public void sendMessage(Message message) {
        if (running) {
            channel.writeAndFlush(message);
        }
    }

    private class PingPongThread implements Runnable {
        @Override
        public void run() {
            Message connect = new Message();
            connect.setTag(MessageType.CONNECT);
            connect.setSender(UserSession.getInstance().getUser().getUsername());
            channel.writeAndFlush(connect);
        }
    }
}
