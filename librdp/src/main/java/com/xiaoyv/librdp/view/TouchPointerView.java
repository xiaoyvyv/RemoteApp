package com.xiaoyv.librdp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.freerdp.freerdpcore.utils.GestureDetector;
import com.xiaoyv.librdp.R;

/**
 * 模拟鼠标自定义视图
 */
public class TouchPointerView extends AppCompatImageView {

    private static final int POINTER_ACTION_CURSOR = 0;
    private static final int POINTER_ACTION_CLOSE = 3;

    // 触摸指针由9个象限组成，具有以下功能：
    //
    // -------------
    // | 0 | 1 | 2 |
    // -------------
    // | 3 | 4 | 5 |
    // -------------
    // | 6 | 7 | 8 |
    // -------------
    //
    // 0 ... 包含实际指针（尖端必须在象限的中心）
    // 1 ... 留空
    // 2, 3, 5, 6, 7, 8 ... 发出回调的功能象限
    // 4 ... 中心位置，用于左键单击和拖动指针
    private static final int POINTER_ACTION_RCLICK = 2;
    private static final int POINTER_ACTION_LCLICK = 4;
    private static final int POINTER_ACTION_MOVE = 4;
    private static final int POINTER_ACTION_SCROLL = 5;
    private static final int POINTER_ACTION_RESET = 6;
    private static final int POINTER_ACTION_KEYBOARD = 7;
    private static final int POINTER_ACTION_EXTKEYBOARD = 8;
    private static final float SCROLL_DELTA = 10.0f;
    private static final int DEFAULT_TOUCH_POINTER_RESTORE_DELAY = 150;
    private RectF pointerRect;
    private final RectF[] pointerAreaRectArray = new RectF[9];
    private Matrix translationMatrix;
    private boolean pointerMoving = false;
    private boolean pointerScrolling = false;
    private TouchPointerListener listener = null;
    private final UIHandler uiHandler = new UIHandler(Looper.getMainLooper());
    // 手势检测
    private GestureDetector gestureDetector;

    public TouchPointerView(Context context) {
        super(context);
        initTouchPointer(context);
    }

    public TouchPointerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTouchPointer(context);
    }

    public TouchPointerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initTouchPointer(context);
    }

    private void initTouchPointer(Context context) {
        gestureDetector =
                new GestureDetector(context, new TouchPointerGestureListener(), null, true);
        gestureDetector.setLongPressTimeout(500);
        translationMatrix = new Matrix();
        setScaleType(ScaleType.MATRIX);
        setImageMatrix(translationMatrix);

        // 初始化
        final float rectSizeWidth = (float) getDrawable().getIntrinsicWidth() / 3.0f;
        final float rectSizeHeight = (float) getDrawable().getIntrinsicWidth() / 3.0f;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int left = (int) (j * rectSizeWidth);
                int top = (int) (i * rectSizeHeight);
                int right = left + (int) rectSizeWidth;
                int bottom = top + (int) rectSizeHeight;
                pointerAreaRectArray[i * 3 + j] = new RectF(left, top, right, bottom);
            }
        }
        pointerRect =
                new RectF(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
    }

    public void setTouchPointerListener(TouchPointerListener listener) {
        this.listener = listener;
    }

    public int getPointerWidth() {
        return getDrawable().getIntrinsicWidth();
    }

    public int getPointerHeight() {
        return getDrawable().getIntrinsicHeight();
    }

    public float[] getPointerPosition() {
        float[] curPos = new float[2];
        translationMatrix.mapPoints(curPos);
        return curPos;
    }

    private void movePointer(float deltaX, float deltaY) {
        translationMatrix.postTranslate(deltaX, deltaY);
        setImageMatrix(translationMatrix);
    }

    private void ensureVisibility(int screen_width, int screen_height) {
        float[] curPos = new float[2];
        translationMatrix.mapPoints(curPos);

        if (curPos[0] > (screen_width - pointerRect.width()))
            curPos[0] = screen_width - pointerRect.width();
        if (curPos[0] < 0)
            curPos[0] = 0;
        if (curPos[1] > (screen_height - pointerRect.height()))
            curPos[1] = screen_height - pointerRect.height();
        if (curPos[1] < 0)
            curPos[1] = 0;

        translationMatrix.setTranslate(curPos[0], curPos[1]);
        setImageMatrix(translationMatrix);
    }

    private void displayPointerImageAction(int resId) {
        setPointerImage(resId);
        uiHandler.sendEmptyMessageDelayed(0, DEFAULT_TOUCH_POINTER_RESTORE_DELAY);
    }

    private void setPointerImage(int resId) {
        setImageResource(resId);
    }

    // 返回具有当前转换矩阵的指针区域
    @SuppressWarnings("SameParameterValue")
    private RectF getCurrentPointerArea(int area) {
        RectF transRect = new RectF(pointerAreaRectArray[area]);
        translationMatrix.mapRect(transRect);
        return transRect;
    }

    private boolean pointerAreaTouched(MotionEvent event, int area) {
        RectF transRect = new RectF(pointerAreaRectArray[area]);
        translationMatrix.mapRect(transRect);
        return transRect.contains(event.getX(), event.getY());
    }

    private boolean pointerTouched(MotionEvent event) {
        RectF transRect = new RectF(pointerRect);
        translationMatrix.mapRect(transRect);
        return transRect.contains(event.getX(), event.getY());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 检查指针是否正在移动，或者我们是否处于滚动模式，或者是否触摸了指针
        if (!pointerMoving && !pointerScrolling && !pointerTouched(event)) {
            return false;
        }
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // 确保触摸指针可见
        if (changed) {
            ensureVisibility(right - left, bottom - top);
        }
    }

    // 触摸指针侦听器-如果操作字段为
    public interface TouchPointerListener {
        void onTouchPointerClose();

        void onTouchPointerLeftClick(int x, int y, boolean down);

        void onTouchPointerRightClick(int x, int y, boolean down);

        void onTouchPointerMove(int x, int y);

        void onTouchPointerScroll(boolean down);

        void onTouchPointerToggleKeyboard();

        void onTouchPointerToggleExtKeyboard();

        void onTouchPointerResetScrollZoom();
    }

    private class UIHandler extends Handler {

        public UIHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            setPointerImage(R.drawable.touch_pointer_default);
        }
    }

    private class TouchPointerGestureListener extends GestureDetector.SimpleOnGestureListener {

        private MotionEvent prevEvent = null;

        public boolean onDown(MotionEvent e) {
            if (pointerAreaTouched(e, POINTER_ACTION_MOVE)) {
                prevEvent = MotionEvent.obtain(e);
                pointerMoving = true;
            } else if (pointerAreaTouched(e, POINTER_ACTION_SCROLL)) {
                prevEvent = MotionEvent.obtain(e);
                pointerScrolling = true;
                setPointerImage(R.drawable.touch_pointer_scroll);
            }

            return true;
        }

        public boolean onUp(MotionEvent e) {
            if (prevEvent != null) {
                prevEvent.recycle();
                prevEvent = null;
            }

            if (pointerScrolling)
                setPointerImage(R.drawable.touch_pointer_default);

            pointerMoving = false;
            pointerScrolling = false;
            return true;
        }

        public void onLongPress(MotionEvent e) {
            if (pointerAreaTouched(e, POINTER_ACTION_LCLICK)) {
                setPointerImage(R.drawable.touch_pointer_active);
                pointerMoving = true;
                RectF rect = getCurrentPointerArea(POINTER_ACTION_CURSOR);
                listener.onTouchPointerLeftClick((int) rect.centerX(), (int) rect.centerY(), true);
            }
        }

        public void onLongPressUp(MotionEvent e) {
            if (pointerMoving) {
                setPointerImage(R.drawable.touch_pointer_default);
                pointerMoving = false;
                RectF rect = getCurrentPointerArea(POINTER_ACTION_CURSOR);
                listener.onTouchPointerLeftClick((int) rect.centerX(), (int) rect.centerY(), false);
            }
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (pointerMoving) {
                // 移动指针图形
                movePointer((int) (e2.getX() - prevEvent.getX()),
                        (int) (e2.getY() - prevEvent.getY()));
                prevEvent.recycle();
                prevEvent = MotionEvent.obtain(e2);

                // 发送移动通知
                RectF rect = getCurrentPointerArea(POINTER_ACTION_CURSOR);
                listener.onTouchPointerMove((int) rect.centerX(), (int) rect.centerY());
                return true;
            } else if (pointerScrolling) {
                // 计算用户向上或向下滚动（或根本没有滚动）
                float deltaY = e2.getY() - prevEvent.getY();
                if (deltaY > SCROLL_DELTA) {
                    listener.onTouchPointerScroll(true);
                    prevEvent.recycle();
                    prevEvent = MotionEvent.obtain(e2);
                } else if (deltaY < -SCROLL_DELTA) {
                    listener.onTouchPointerScroll(false);
                    prevEvent.recycle();
                    prevEvent = MotionEvent.obtain(e2);
                }
                return true;
            }
            return false;
        }

        public boolean onSingleTapUp(MotionEvent e) {
            // 看什么地方被触摸，并采取相应的行动
            if (pointerAreaTouched(e, POINTER_ACTION_CLOSE))
                listener.onTouchPointerClose();
            else if (pointerAreaTouched(e, POINTER_ACTION_LCLICK)) {
                displayPointerImageAction(R.drawable.touch_pointer_lclick);
                RectF rect = getCurrentPointerArea(POINTER_ACTION_CURSOR);
                listener.onTouchPointerLeftClick((int) rect.centerX(), (int) rect.centerY(), true);
                listener.onTouchPointerLeftClick((int) rect.centerX(), (int) rect.centerY(), false);
            } else if (pointerAreaTouched(e, POINTER_ACTION_RCLICK)) {
                displayPointerImageAction(R.drawable.touch_pointer_rclick);
                RectF rect = getCurrentPointerArea(POINTER_ACTION_CURSOR);
                listener.onTouchPointerRightClick((int) rect.centerX(), (int) rect.centerY(), true);
                listener.onTouchPointerRightClick((int) rect.centerX(), (int) rect.centerY(), false);
            } else if (pointerAreaTouched(e, POINTER_ACTION_KEYBOARD)) {
                displayPointerImageAction(R.drawable.touch_pointer_keyboard);
                listener.onTouchPointerToggleKeyboard();
            } else if (pointerAreaTouched(e, POINTER_ACTION_EXTKEYBOARD)) {
                displayPointerImageAction(R.drawable.touch_pointer_extkeyboard);
                listener.onTouchPointerToggleExtKeyboard();
            } else if (pointerAreaTouched(e, POINTER_ACTION_RESET)) {
                displayPointerImageAction(R.drawable.touch_pointer_reset);
                listener.onTouchPointerResetScrollZoom();
            }

            return true;
        }

        public boolean onDoubleTap(MotionEvent e) {
            // 如果在中心象限中执行，则发出双击通知
            if (pointerAreaTouched(e, POINTER_ACTION_LCLICK)) {
                RectF rect = getCurrentPointerArea(POINTER_ACTION_CURSOR);
                listener.onTouchPointerLeftClick((int) rect.centerX(), (int) rect.centerY(), true);
                listener.onTouchPointerLeftClick((int) rect.centerX(), (int) rect.centerY(), false);
            }
            return true;
        }
    }
}
