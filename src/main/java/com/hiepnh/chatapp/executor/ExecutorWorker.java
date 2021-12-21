package com.hiepnh.chatapp.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class ExecutorWorker extends ThreadBase{

    private final Logger logger = LoggerFactory.getLogger(getName());

    private final BlockingQueue<Object> queue;


    public ExecutorWorker(String name, BlockingQueue<Object> queue) {
        super(name);
        this.queue = queue;
    }

    @Override
    protected void onExecuting() throws Exception {
        System.out.println("Thread " + getName() + " started");
    }

    @Override
    protected void onKilling() {
        this.kill();
        System.out.println("Thread " + getName() + " killed");
    }

    @Override
    protected void onException(Throwable th) {
        System.err.println(th.getMessage());
    }

    @Override
    protected long sleepTime() throws Exception {
        return 100;
    }

    @Override
    protected void action() throws Exception {

    }
}
