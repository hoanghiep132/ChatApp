package com.hiepnh.chatapp.executor;

import com.hiepnh.chatapp.model.Message;
import com.hiepnh.chatapp.model.TlvPackage;

import java.util.List;

public interface Watcher {

    void addPackage(TlvPackage tlvPackage);

}
