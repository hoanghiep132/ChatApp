package com.hiepnh.chatapp.netty.indbound;

import com.hiepnh.chatapp.common.BufferConstant;
import com.hiepnh.chatapp.common.Constant;
import com.hiepnh.chatapp.common.MessageType;
import com.hiepnh.chatapp.common.StateDecoder;
import com.hiepnh.chatapp.executor.PackHandler;
import com.hiepnh.chatapp.executor.Watcher;
import com.hiepnh.chatapp.model.TlvPackage;
import com.hiepnh.chatapp.netty.NettyClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

//public class ResponseDecoder extends ReplayingDecoder<Object> {
public class ResponseDecoder extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ByteBuf tmp;

    private static int flag = 1;

    private Watcher watcher;

    private TlvPackage tlvPackage = new TlvPackage();

    private NettyClient nettyClient;

    public ResponseDecoder(){}

    public ResponseDecoder(Watcher watcher, NettyClient nettyClient) {
        this.watcher = watcher;
        this.nettyClient = nettyClient;
    }

    public ResponseDecoder(Watcher watcher) {
        this.watcher = watcher;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        tmp = ctx.alloc().buffer(BufferConstant.KB);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Disconnect");
        nettyClient.start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        tmp.writeBytes(buf);
        buf.release();
        int byteNumber = tmp.readableBytes();
        int start = 0;
        while (byteNumber > 0){
            switch (flag){
                case StateDecoder.TAG_FLAG:
                    byte tagByte = tmp.getByte(start++);
                    tlvPackage.setTag(tagByte);
                    byteNumber -= StateDecoder.TAG_LENGTH;
                    flag = StateDecoder.LENGTH_FLAG;
                    break;
                case StateDecoder.LENGTH_FLAG:
                    if(byteNumber < StateDecoder.LENGTH){
                        return;
                    }
                    int length = tmp.getInt(start);
                    tlvPackage.setLength(length);
                    start += StateDecoder.LENGTH;
                    byteNumber -= StateDecoder.LENGTH;
                    flag = StateDecoder.VALUE_FLAG;
                    break;
                case StateDecoder.VALUE_FLAG:
                    int valueLength = tlvPackage.getLength() - tlvPackage.getCurrent();
                    if(byteNumber < valueLength){
                        byte[] values = new byte[byteNumber];
                        tmp.getBytes(start, values);
                        tlvPackage.setData(values);
                        tmp.clear();
                        return;
                    }
                    byte[] values = new byte[valueLength];
                    tmp.getBytes(start, values);
                    tlvPackage.setData(values);
                    if(tlvPackage.getTag() == MessageType.CALL_REQUEST){
                        logger.info("aaaaaaaaaaaaaaaaaaa");
                    }
                    watcher.addPackage(tlvPackage);
                    start += valueLength;
                    byteNumber -= valueLength;
                    tlvPackage.reset();
                    flag = StateDecoder.TAG_FLAG;
                    break;
            }
        }
        tmp.clear();
    }

//    @Override
//    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buf, List<Object> list) throws Exception {
//        tmp.writeBytes(buf);
//        buf.release();
//        logger.info("1");
//        int byteNumber = tmp.readableBytes();
//        int start = 0;
//        while (byteNumber > 0){
//            switch (flag){
//                case StateDecoder.TAG_FLAG:
//                    byte tagByte = tmp.getByte(start++);
//                    tlvPackage.setTag(tagByte);
//                    byteNumber--;
//                    flag++;
//                    break;
//                case StateDecoder.LENGTH_FLAG:
//                    if(byteNumber < 4){
//                        return;
//                    }
//                    int length = tmp.getInt(start);
//                    tlvPackage.setLength(length);
//                    start += 4;
//                    byteNumber -= 4;
//                    flag++;
//                    break;
//                case StateDecoder.VALUE_FLAG:
//                    int valueLength = tlvPackage.getLength();
//                    if(byteNumber < valueLength){
//                        return;
//                    }
//                    byte[] values = new byte[valueLength];
//                    tmp.getBytes(start, values);
//                    String m = new String(values, StandardCharsets.UTF_8);
//                    logger.info("Message: {}", m);
//                    tlvPackage.setValues(values);
//
//                    // xử lý message
//                    watcher.addPackage(tlvPackage);
//
//                    flag = StateDecoder.TAG_FLAG;
//
//                    byteNumber -= valueLength;
//                    start += valueLength;;
//                    tlvPackage.reset();
//                    break;
//            }
//        }
//        tmp.clear();
//    }
}
