package com.hiepnh.chatapp.executor;

import org.bytedeco.javacv.Frame;

public interface WebcamListener {

    public void hasImage(Frame frame);
}
