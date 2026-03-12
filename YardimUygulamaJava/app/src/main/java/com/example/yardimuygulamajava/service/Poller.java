package com.example.yardimuygulamajava.service;

import android.os.Handler;
import android.os.Looper;

public class Poller {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long intervalMs;
    private final Runnable task;
    private boolean running = false;

    public Poller(long intervalMs, Runnable task) {
        this.intervalMs = intervalMs;
        this.task = task;
    }

    public void start() {
        if (running) return;
        running = true;
        handler.post(loop);
    }

    public void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
    }

    private final Runnable loop = new Runnable() {
        @Override public void run() {
            if (!running) return;
            task.run();
            handler.postDelayed(this, intervalMs);
        }
    };
}
