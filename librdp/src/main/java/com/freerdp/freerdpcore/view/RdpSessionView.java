package com.freerdp.freerdpcore.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.freerdp.freerdpcore.application.RdpSessionState;
import com.freerdp.freerdpcore.utils.DoubleGestureDetector;
import com.freerdp.freerdpcore.utils.GestureDetector;

import java.util.Stack;

/**
 * 远程连接自定义视图
 */
public class RdpSessionView extends View {
    public static final float MAX_SCALE_FACTOR = 3.0f;
    public static final float MIN_SCALE_FACTOR = 1.0f;
    private static final float SCALE_FACTOR_DELTA = 0.0001f;
    private static final float TOUCH_SCROLL_DELTA = 10.0f;
    private int width;
    private int height;
    private BitmapDrawable surface;
    private Stack<Rect> invalidRegions;
    private int touchPointerPaddingWidth = 0;
    private int touchPointerPaddingHeight = 0;
    private SessionViewListener sessionViewListener = null;
    // 扩展手势处理的助手
    private float scaleFactor = 1.0f;
    private Matrix scaleMatrix;
    private Matrix invScaleMatrix;
    private RectF invalidRegionF;
    private GestureDetector gestureDetector;
    private RdpSessionState currentSession;

    private DoubleGestureDetector doubleGestureDetector;

    public RdpSessionView(Context context) {
        super(context);
        initSessionView(context);
    }

    public RdpSessionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSessionView(context);
    }

    public RdpSessionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initSessionView(context);
    }

    private void initSessionView(Context context) {
        invalidRegions = new Stack<>();
        gestureDetector = new GestureDetector(context, new SessionGestureListener(), null, true);
        doubleGestureDetector = new DoubleGestureDetector(context, null, new SessionDoubleGestureListener());
        scaleFactor = 1.0f;
        scaleMatrix = new Matrix();
        invScaleMatrix = new Matrix();
        invalidRegionF = new RectF();
    }

    public void setScaleGestureDetector(ScaleGestureDetector scaleGestureDetector) {
        doubleGestureDetector.setScaleGestureDetector(scaleGestureDetector);
    }

    public void setSessionViewListener(SessionViewListener sessionViewListener) {
        this.sessionViewListener = sessionViewListener;
    }

    public void addInvalidRegion(Rect invalidRegion) {
        // 根据当前缩放比例正确变换无效区域
        invalidRegionF.set(invalidRegion);
        scaleMatrix.mapRect(invalidRegionF);
        invalidRegionF.roundOut(invalidRegion);

        invalidRegions.add(invalidRegion);
    }

    public void invalidateRegion() {
        invalidate(invalidRegions.pop());
    }

    public void onSurfaceChange(RdpSessionState session) {
        surface = session.getSurface();
        Bitmap bitmap = surface.getBitmap();
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        surface.setBounds(0, 0, width, height);

        setMinimumWidth(width);
        setMinimumHeight(height);

        requestLayout();
        currentSession = session;
    }

    public float getZoom() {
        return scaleFactor;
    }

    public void setZoom(float factor) {
        // 计算比例尺矩阵和逆比例尺矩阵（以正确地变换视图和Moue坐标）
        scaleFactor = factor;
        scaleMatrix.setScale(scaleFactor, scaleFactor);
        invScaleMatrix.setScale(1.0f / scaleFactor, 1.0f / scaleFactor);

        // 更新布局
        requestLayout();
    }

    public boolean isNotAtMaxZoom() {
        return (!(scaleFactor > (MAX_SCALE_FACTOR - SCALE_FACTOR_DELTA)));
    }

    public boolean isNotAtMinZoom() {
        return (!(scaleFactor < (MIN_SCALE_FACTOR + SCALE_FACTOR_DELTA)));
    }

    public boolean zoomIn(float factor) {
        boolean res = true;
        scaleFactor += factor;
        if (scaleFactor > (MAX_SCALE_FACTOR - SCALE_FACTOR_DELTA)) {
            scaleFactor = MAX_SCALE_FACTOR;
            res = false;
        }
        setZoom(scaleFactor);
        return res;
    }

    public boolean zoomOut(float factor) {
        boolean res = true;
        scaleFactor -= factor;
        if (scaleFactor < (MIN_SCALE_FACTOR + SCALE_FACTOR_DELTA)) {
            scaleFactor = MIN_SCALE_FACTOR;
            res = false;
        }
        setZoom(scaleFactor);
        return res;
    }

    public void setTouchPointerPadding(int width, int height) {
        touchPointerPaddingWidth = width;
        touchPointerPaddingHeight = height;
        requestLayout();
    }

    public int getTouchPointerPaddingWidth() {
        return touchPointerPaddingWidth;
    }

    public int getTouchPointerPaddingHeight() {
        return touchPointerPaddingHeight;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.setMeasuredDimension((int) (width * scaleFactor) + touchPointerPaddingWidth, (int) (height * scaleFactor) + touchPointerPaddingHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.concat(scaleMatrix);
        if (surface != null) {
            surface.draw(canvas);
        }
        canvas.restore();
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            ((Activity) this.getContext()).onBackPressed();
        }
        return super.dispatchKeyEventPreIme(event);
    }

    // 根据当前缩放比例在触摸事件的坐标上执行映射
    private MotionEvent mapTouchEvent(MotionEvent event) {
        MotionEvent mappedEvent = MotionEvent.obtain(event);
        float[] coordinates = {mappedEvent.getX(), mappedEvent.getY()};
        invScaleMatrix.mapPoints(coordinates);
        mappedEvent.setLocation(coordinates[0], coordinates[1]);
        return mappedEvent;
    }

    // 根据当前缩放比例在两次触摸事件的坐标上执行映射
    private MotionEvent mapDoubleTouchEvent(MotionEvent event) {
        MotionEvent mappedEvent = MotionEvent.obtain(event);
        float[] coordinates = {(mappedEvent.getX(0) + mappedEvent.getX(1)) / 2, (mappedEvent.getY(0) + mappedEvent.getY(1)) / 2};
        invScaleMatrix.mapPoints(coordinates);
        mappedEvent.setLocation(coordinates[0], coordinates[1]);
        return mappedEvent;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean res = gestureDetector.onTouchEvent(event);
        res |= doubleGestureDetector.onTouchEvent(event);
        return res;
    }

    public interface SessionViewListener {
        void onSessionViewBeginTouch();

        void onSessionViewEndTouch();

        void onSessionViewLeftTouch(int x, int y, boolean down);

        void onSessionViewRightTouch(int x, int y, boolean down);

        void onSessionViewMove(int x, int y);

        void onSessionViewScroll(boolean down);
    }

    private class SessionGestureListener extends GestureDetector.SimpleOnGestureListener {
        boolean longPressInProgress = false;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onUp(MotionEvent e) {
            sessionViewListener.onSessionViewEndTouch();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            MotionEvent mappedEvent = mapTouchEvent(e);
            sessionViewListener.onSessionViewBeginTouch();
            sessionViewListener.onSessionViewLeftTouch((int) mappedEvent.getX(), (int) mappedEvent.getY(), true);
            longPressInProgress = true;
        }

        @Override
        public void onLongPressUp(MotionEvent e) {
            MotionEvent mappedEvent = mapTouchEvent(e);
            sessionViewListener.onSessionViewLeftTouch((int) mappedEvent.getX(), (int) mappedEvent.getY(), false);
            longPressInProgress = false;
            sessionViewListener.onSessionViewEndTouch();
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (longPressInProgress) {
                MotionEvent mappedEvent = mapTouchEvent(e2);
                sessionViewListener.onSessionViewMove((int) mappedEvent.getX(), (int) mappedEvent.getY());
                return true;
            }

            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // 发送双击事件
            MotionEvent mappedEvent = mapTouchEvent(e);
            sessionViewListener.onSessionViewLeftTouch((int) mappedEvent.getX(), (int) mappedEvent.getY(), true);
            sessionViewListener.onSessionViewLeftTouch((int) mappedEvent.getX(), (int) mappedEvent.getY(), false);
            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // 发送单击事件
            MotionEvent mappedEvent = mapTouchEvent(e);
            sessionViewListener.onSessionViewBeginTouch();
            sessionViewListener.onSessionViewLeftTouch((int) mappedEvent.getX(), (int) mappedEvent.getY(), true);
            sessionViewListener.onSessionViewLeftTouch((int) mappedEvent.getX(), (int) mappedEvent.getY(), false);
            sessionViewListener.onSessionViewEndTouch();
            return true;
        }
    }

    private class SessionDoubleGestureListener implements DoubleGestureDetector.OnDoubleGestureListener {
        private MotionEvent prevEvent = null;

        @Override
        public boolean onDoubleTouchDown(MotionEvent e) {
            sessionViewListener.onSessionViewBeginTouch();
            prevEvent = MotionEvent.obtain(e);
            return true;
        }

        @Override
        public boolean onDoubleTouchUp(MotionEvent e) {
            if (prevEvent != null) {
                prevEvent.recycle();
                prevEvent = null;
            }
            sessionViewListener.onSessionViewEndTouch();
            return true;
        }

        @Override
        public boolean onDoubleTouchScroll(MotionEvent e1, MotionEvent e2) {
            // 计算用户向上或向下滚动（或根本没有滚动）
            float deltaY = e2.getY() - prevEvent.getY();
            if (deltaY > TOUCH_SCROLL_DELTA) {
                sessionViewListener.onSessionViewScroll(true);
                prevEvent.recycle();
                prevEvent = MotionEvent.obtain(e2);
            } else if (deltaY < -TOUCH_SCROLL_DELTA) {
                sessionViewListener.onSessionViewScroll(false);
                prevEvent.recycle();
                prevEvent = MotionEvent.obtain(e2);
            }
            return true;
        }

        @Override
        public boolean onDoubleTouchSingleTap(MotionEvent e) {
            // 发送点击
            MotionEvent mappedEvent = mapDoubleTouchEvent(e);
            sessionViewListener.onSessionViewRightTouch((int) mappedEvent.getX(), (int) mappedEvent.getY(), true);
            sessionViewListener.onSessionViewRightTouch((int) mappedEvent.getX(), (int) mappedEvent.getY(), false);
            return true;
        }
    }
}
