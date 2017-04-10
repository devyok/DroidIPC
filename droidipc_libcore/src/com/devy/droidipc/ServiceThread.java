package com.devy.droidipc;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * service thread for the Get and execute remote service method.  
 */
public final class ServiceThread extends HandlerThread {
    private static ServiceThread sInstance;
    private static Handler sHandler;

    private ServiceThread() {
        super("dropipc.service.thread", android.os.Process.THREAD_PRIORITY_DEFAULT);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new ServiceThread();
            sInstance.start();
            sHandler = new Handler(sInstance.getLooper());
        }
    }
    
    public static Handler getHandler() {
        synchronized (ServiceThread.class) {
            ensureThreadLocked();
            return sHandler;
        }
    }
    
    
}
