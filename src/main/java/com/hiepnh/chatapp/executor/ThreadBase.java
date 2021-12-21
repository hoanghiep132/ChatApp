package com.hiepnh.chatapp.executor;


public abstract class ThreadBase {
    public static final int NO_SLEEP = -1;
    public static final int EXCEPTION_SLEEP_TIME = 15000;
    private Thread thread = null;
    private String name;
    private Thread parentThread = null;
    private boolean needToDie = false;
    private Object sleepSync = new Object();
    private boolean sleeping = false;

    public ThreadBase(String name) {
        this.name = name;
    }

    public ThreadBase() {
        this.name = null;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        if (null != this.thread) {
            this.thread.setName(this.name);
        }

    }

    private boolean getNeedToDie() {
        return this.needToDie;
    }

    public synchronized void execute() {
        if (null == this.thread) {
            this.thread = new Thread(new ActionThreadImpl(this), this.name);
            this.parentThread = Thread.currentThread();
            this.needToDie = false;
            this.thread.setName(this.name);
            this.thread.start();

            while (!this.thread.isAlive()) {
                Thread.currentThread();
                Thread.yield();
            }
        }

    }

    public synchronized void kill() {
        if (null != this.thread) {
            this.needToDie = true;
            if (Thread.currentThread() != this.thread) {
                this.interrupt();
                this.waitToDie();
            }
            this.thread = null;
        }

    }

    private void interrupt() {
        this.thread.interrupt();
    }

    private void waitToDie() {
        try {
            this.thread.join();
        } catch (InterruptedException var2) {
        }

    }

    private void sleep(long milliseconds) {
        try {
            this.sleeping = true;
            Thread var10000 = this.thread;
            Thread.sleep(milliseconds);
        } catch (InterruptedException var7) {
        } finally {
            this.sleeping = false;
        }
    }

    protected abstract void onExecuting() throws Exception;

    protected abstract void onKilling();

    protected abstract void onException(Throwable th);

    protected abstract long sleepTime() throws Exception;

    protected abstract void action() throws Exception;

    private static class ActionThreadImpl implements Runnable {
        private final ThreadBase ref;

        public ActionThreadImpl(ThreadBase ref) {
            this.ref = ref;
        }

        public void run() {
            do {
                try {
                    this.ref.onExecuting();
                    while (!this.ref.getNeedToDie()) {
                        this.ref.action();
                        if (!this.ref.getNeedToDie()) {
                            long milliseconds = this.ref.sleepTime();
                            if (-1L != milliseconds) {
                                this.ref.sleep(milliseconds);
                            }
                        }
                    }
                } catch (Throwable var4) {
                    this.ref.onException(var4);
                    if (!this.ref.getNeedToDie()) {
                        this.ref.sleep(15000L);
                    }
                }
            } while (!this.ref.getNeedToDie());

            this.ref.onKilling();
        }
    }
}

