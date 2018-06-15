package com.github.queatz.slidescreen;

import android.os.Handler;

import java.util.Date;

/**
 * Created by jacob on 9/17/17.
 */
class SlideAnimation extends Handler {
    private SlideScreen slideScreen;
    private float from;
    private float to;
    private int duration;
    private long startOffsetTime;
    private Date startTime;
    private Runnable loopRunnable;
    private boolean alive;

    SlideAnimation(SlideScreen slideScreen, int to) {
        this.slideScreen = slideScreen;
        from = this.slideScreen.offset;
        this.to = to;
        duration = 150;
        startOffsetTime = 0;
        alive = false;

        loopRunnable = new Runnable() {
            @Override
            public void run() {
                loop();
            }
        };
    }

    boolean isAlive() {
        return alive;
    }

    void start() {
        start(0);
    }

    void stop() {
        alive = false;
        removeCallbacks(loopRunnable);
    }

    private void start(float startDelta) {
        startTime = new Date();
        startOffsetTime = (long) (duration * startDelta);
        alive = true;
        post(loopRunnable);
    }

    private float interpolate(float dt) {
        return (float) Math.abs((Math.sin((dt - .5) * Math.PI) + 1) / 2f);
    }

    private float getDelta() {
        return Math.min(1.0f, Math.max(0.0f, (float) (new Date().getTime() + startOffsetTime - startTime.getTime()) / (float) duration));
    }

    private void loop() {
        float dt = getDelta();

        apply(interpolate(dt));

        if (dt >= 1.0f) {
            stop();
            return;
        }

        postDelayed(loopRunnable, 0);
    }

    private void apply(float time) {
        slideScreen.setOffset(to * time + from * (1.0f - time));
    }
}

