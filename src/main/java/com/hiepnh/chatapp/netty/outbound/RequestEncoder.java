package com.hiepnh.chatapp.netty.outbound;

import com.hiepnh.chatapp.common.MessageType;
import com.hiepnh.chatapp.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class RequestEncoder extends MessageToByteEncoder<Message> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        StringBuffer sb = new StringBuffer();
        if(message.getTag() == MessageType.MESSAGE){
            sb.append(message.getSender())
                    .append(";")
                    .append(message.getReceiver())
                    .append(";")
                    .append(message.getContent());
        }else if(message.getTag() == MessageType.CONNECT || message.getTag() == MessageType.DISCONNECT){
            sb.append(message.getSender());
        }else if(message.getTag() == MessageType.TYPING){
            sb.append(message.getSender())
                    .append(";")
                    .append(message.getReceiver());
        }else if(message.getTag() == MessageType.CALL_REQUEST){
            sb.append(message.getReceiver());
        }else if(message.getTag() == MessageType.CALL_ACCEPT){

        }else if(message.getTag() == MessageType.CALL_REJECT){

        }else if(message.getTag() == MessageType.CALL){
            int length = message.getVideoData().length;
            byte[] request = new byte[length + 5];
            request[0] = MessageType.CALL;
            request[1] = (byte) ((length >> 24) & 0xff);
            request[2] = (byte) ((length >> 16) & 0xff);
            request[3] = (byte) ((length >> 8) & 0xff);
            request[4] = (byte) ((length) & 0xff);
            byte[] contentBytes = message.getVideoData();
            System.arraycopy(contentBytes, 0, request, 5, length);
            byteBuf.writeBytes(request);
            return;
        }
        int length = sb.toString().getBytes(StandardCharsets.UTF_8).length;
        byte[] request = new byte[length + 5];
        request[0] = message.getTag();
        request[1] = (byte) ((length >> 24) & 0xff);
        request[2] = (byte) ((length >> 16) & 0xff);
        request[3] = (byte) ((length >> 8) & 0xff);
        request[4] = (byte) ((length) & 0xff);
        byte[] contentBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        System.arraycopy(contentBytes, 0, request, 5, contentBytes.length);
        byteBuf.writeBytes(request);
    }


}
