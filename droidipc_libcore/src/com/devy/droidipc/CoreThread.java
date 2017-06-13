package com.devy.droidipc;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * CoreThread主要提供异步的消息处理
 */
final class CoreThread extends HandlerThread {
    private static CoreThread sInstance;
    private static Handler sHandler;

    private CoreThread() {
        super("dropipc.core.thread", android.os.Process.THREAD_PRIORITY_DEFAULT);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new CoreThread();
            sInstance.start();
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static CoreThread get() {
        synchronized (CoreThread.class) {
            ensureThreadLocked();
            return sInstance;
        }
    }

    public static Handler getHandler() {
        synchronized (CoreThread.class) {
            ensureThreadLocked();
            return sHandler;
        }
    }
}
