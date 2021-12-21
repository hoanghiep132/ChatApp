package com.hiepnh.chatapp.executor;

import com.hiepnh.chatapp.common.MessageType;
import com.hiepnh.chatapp.model.Message;
import com.hiepnh.chatapp.model.TlvPackage;
import com.hiepnh.chatapp.utils.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PackHandler implements Watcher{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlockingQueue<Message> messageQueue;

    public PackHandler() {
        messageQueue = new LinkedBlockingQueue<>();
    }

    public void addPackage(TlvPackage tlvPackage){
        String value = AppUtils.convertByteArrayToString(tlvPackage.getValues());
        if(tlvPackage.getTag() == MessageType.MESSAGE){
            String[] data = value.split(";", 3);
            if(data.length != 3){
                return;
            }
            String sender = data[0];
            String receiver = data[1];
            String content = data[2];
            Message message = new Message();
            message.setTag(MessageType.MESSAGE);
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setContent(content);
            message.setTime(System.currentTimeMillis());
            messageQueue.offer(message);
        }else if(tlvPackage.getTag() == MessageType.ONLINE){
            Message message = new Message();
            message.setTag(MessageType.ONLINE);
            message.setContent(value);
            logger.info("UserOnline : {}", value);
            messageQueue.offer(message);
        }else if(tlvPackage.getTag() == MessageType.TYPING){
            Message message = new Message();
            message.setTag(MessageType.TYPING);
            message.setContent(value);
            messageQueue.offer(message);
        }else if(tlvPackage.getTag() == MessageType.CALL){

        }else if(tlvPackage.getTag() == MessageType.CALL_REQUEST){
            logger.info("call request");
            Message message = new Message();
            message.setTag(MessageType.CALL_REQUEST);
            message.setContent(value);
            messageQueue.offer(message);
        }
    }

}
