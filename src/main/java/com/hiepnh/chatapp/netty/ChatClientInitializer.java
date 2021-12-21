package com.hiepnh.chatapp.netty;

import com.hiepnh.chatapp.executor.Watcher;
import com.hiepnh.chatapp.netty.indbound.ResponseProcessor;
import com.hiepnh.chatapp.netty.outbound.RequestEncoder;
import com.hiepnh.chatapp.netty.indbound.ResponseDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ChatClientInitializer extends ChannelInitializer<SocketChannel> {

    private Watcher watcher;

    private NettyClient nettyClient;

    public ChatClientInitializer(Watcher watcher, NettyClient nettyClient) {
        this.watcher = watcher;
        this.nettyClient = nettyClient;
    }

    public ChatClientInitializer(Watcher watcher) {
        this.watcher = watcher;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new ResponseDecoder(watcher, nettyClient));
//        pipeline.addLast(new ResponseProcessor());
        pipeline.addLast(new RequestEncoder());
    }
}
