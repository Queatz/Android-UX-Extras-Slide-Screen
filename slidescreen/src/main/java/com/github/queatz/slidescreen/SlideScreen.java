package com.github.queatz.slidescreen;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by jacob on 10/19/14.
 */
public class SlideScreen extends ViewGroup {
    private static class SlideAsChild {
        private int position;
        private Fragment fragment;

        private SlideAsChild(int position, Fragment fragment) {
            this.position = position;
            this.fragment = fragment;
        }
    }

    public interface OnSlideCallback {
        void onSlide(int currentSlide, float offsetPercentage);
        void onSlideChange(int currentSlide);
    }

    private SparseArray<SlideAsChild> slides;
    private int slide;
    protected float offset;
    private OnSlideCallback onSlideCallback;
    private SlideScreenAdapter adapter;
    private SlideAnimation animation;
    private ExposeAnimation exposeAnimation;
    private float flingDeltaX;
    private float downX, downY;
    private boolean isSnatched, isUnsnatchable;
    private boolean childIsUsingMotion;
    private int slopRadius;
    private int gap;
    protected boolean expose = false;
    protected float currentScale = 1;

    public SlideScreen(Context context) {
        super(context);
        init(context);
    }

    public SlideScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlideScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        slopRadius = (int) (96 * context.getResources().getDisplayMetrics().density);
        gap = (int) (128 * context.getResources().getDisplayMetrics().density);

        slides = new SparseArray<>();
        isSnatched = false;
        isUnsnatchable = false;
    }

    public void setOnSlideCallback(OnSlideCallback onSlideCallback) {
        this.onSlideCallback = onSlideCallback;
    }

    public void setAdapter(SlideScreenAdapter adapter) {
        this.adapter = adapter;
        populate();
    }

    public SlideScreenAdapter getAdapter() {
        return adapter;
    }

    public void setSlide(int slide) {
        hideKeyboard();
        smoothSlideTo(slide);
    }

    public int getSlide() {
        return slide;
    }

    public Fragment getSlideFragment(int slide) {
        return slides.get(slide).fragment;
    }

    public void expose(boolean expose) {
        this.expose = expose;

        if(exposeAnimation != null) {
            exposeAnimation.stop();
        }

        exposeAnimation = new ExposeAnimation(this, this.expose);
        exposeAnimation.start();
    }

    public boolean isExpose() {
        return expose;
    }

    private void smoothSlideTo(int slide) {
        if(animation != null) {
            animation.stop();
        }
        animation = new SlideAnimation(this, slide);
        animation.start();

        if(onSlideCallback != null)
            onSlideCallback.onSlideChange(slide);
    }

    protected void setOffset(float offset) {
        this.offset = Math.max(0, Math.min(adapter.getCount() - 1, offset));
        slide = Math.round(this.offset);
        positionChildren();

        if(onSlideCallback != null) {
            onSlideCallback.onSlide(slide, this.offset);
        }
    }

    protected void setScale(float scale) {
        this.currentScale = scale;
        positionChildren();
    }

    private String getFragName(Object slide) {
        return "slidescreen:" + getId() + ":" + slide;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    private void populate() {
        removeAllViews();

        FragmentTransaction transaction = adapter.getFragmentManager().beginTransaction();

        for(int slide = 0; slide < adapter.getCount(); slide++) {
            Fragment fragment = adapter.getSlide(slide);
            slides.append(slide, new SlideAsChild(slide, fragment));
            transaction.add(getId(), fragment, getFragName(slide));
        }

        transaction.commitAllowingStateLoss();
    }

    private SlideAsChild slideFromView(View view) {
        for(int x = 0; x < slides.size(); x++) {
            SlideAsChild child = slides.valueAt(x);
            if(child.fragment.getView() == view)
                return child;
        }

        return null;
    }

    private void positionChildren() {
        int fr = (int) Math.floor(offset);
        int to = fr + 1;

        int width = getWidth() + (int) (gap * (1 - currentScale));

        for(int c = 0; c < getChildCount(); c++) {
            View view = getChildAt(c);
            SlideAsChild child = slideFromView(view);

            if(child == null)
                continue;

            int previousVisibility = view.getVisibility();

            int expfrto = currentScale >= 1 ? 0 : 1;

            view.setVisibility(
                    (child.position < fr - expfrto) ||
                            (child.position > ( offset == 0 ? fr : to) + expfrto) ?
                            View.GONE :
                            View.VISIBLE
            );

            if (view.getVisibility() != previousVisibility) {
                if (previousVisibility == View.GONE) {
                    child.fragment.onResume();
                } else {
                    child.fragment.onPause();
                }
            }

            view.setScaleX(currentScale);
            view.setScaleY(currentScale);

            int l = (int) (currentScale * (width * child.position - (int) (offset * (float) width)));

            try {
                view.layout(l, 0, l + getWidth(), getHeight());
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        try {
            positionChildren();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        for(int c = 0; c < getChildCount(); c++) {
            getChildAt(c).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xdiff = 0;

        if(event.getHistorySize() > 0) {
            xdiff = event.getX() - event.getHistoricalX(0);
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(animation != null && animation.isAlive())
                    animation.stop();

                isSnatched = false;
                isUnsnatchable = false;
                downX = event.getRawX();
                downY = event.getRawY();
                flingDeltaX = 0;
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                setOffset(offset - xdiff / getWidth());

                if (event.getHistorySize() > 0) {
                    int h = Math.min(10, event.getHistorySize() - 1);
                    flingDeltaX = (
                            (event.getHistoricalX(h) - event.getX()) /
                                    ((float) (event.getEventTime() - event.getHistoricalEventTime(h)) / 1000.0f)
                    );
                }

                if(!isSnatched && !isUnsnatchable) {
                    if(shouldLetGo(event)) {
                        resolve(false);
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    }
                    else {
                        isSnatched = shouldSnatch(event);
                        isUnsnatchable = isUnsnatchable(event);

                        if(isSnatched) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);

                if (expose && Math.abs(event.getX() - downX) < slopRadius && Math.abs(event.getY() - downY) < slopRadius) {
                    expose(false);
                }

                resolve(true);
                return false;
        }

        return true;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        childIsUsingMotion = disallowIntercept;

        if(getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isSnatched = false;
                isUnsnatchable = false;
                downX = event.getRawX();
                downY = event.getRawY();
                flingDeltaX = 0;

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                childIsUsingMotion = false;
                isUnsnatchable = false;
                isSnatched = false;

                break;
            case MotionEvent.ACTION_MOVE:
                if(isSnatched || childIsUsingMotion || isUnsnatchable)
                    return isSnatched;

                isSnatched = shouldSnatch(event);
                isUnsnatchable = isUnsnatchable(event);

                break;
        }

        return isSnatched;
    }

    private boolean isUnsnatchable(MotionEvent event) {
        if (isSnatched) {
            return false;
        }

        float xdif = event.getRawX() - downX;
        float ydif = event.getRawY() - downY;

        return Math.abs(xdif) < Math.abs(ydif) && Math.abs(ydif) > 16;
    }

    private boolean shouldSnatch(MotionEvent event) {
        float xdif = event.getRawX() - downX;
        float ydif = event.getRawY() - downY;

        boolean edged = (xdif < 0 && slide >= adapter.getCount() - 1) ||
                (xdif > 0 && slide <= 0);

        return Math.abs(xdif) > Math.abs(ydif) && Math.abs(xdif) > 16 && !edged;
    }

    private boolean shouldLetGo(MotionEvent event) {
        float xdif = event.getRawX() - downX;
        float ydif = event.getRawY() - downY;
        return Math.abs(ydif) > Math.abs(xdif) && Math.abs(ydif) > 16;
    }

    private void resolve(boolean fling) {
        isSnatched = false;

        int slide;

        if (fling && Math.abs(flingDeltaX) > 15) {
            if (flingDeltaX < 0)
                slide = (int) Math.floor(offset);
            else
                slide = (int) Math.ceil(offset);
        } else {
            slide = Math.round(offset);
        }

        setSlide(slide);
    }
}