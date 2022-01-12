package com.hiepnh.chatapp.netty.indbound;

import com.hiepnh.chatapp.model.TlvPackage;
import com.hiepnh.chatapp.utils.AppUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseProcessor extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        TlvPackage tlvPackage = (TlvPackage) msg;
        String content = AppUtils.convertByteArrayToString(tlvPackage.getValues());
    }

}
