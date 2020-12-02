package com.xiaoyv.librdp.mapper;

import android.content.Context;

import com.freerdp.freerdpcore.presentation.ApplicationSettingsActivity;

public class MouseMapper {

    private final static int PTRFLAGS_LBUTTON = 0x1000;
    private final static int PTRFLAGS_RBUTTON = 0x2000;

    private final static int PTRFLAGS_DOWN = 0x8000;
    private final static int PTRFLAGS_MOVE = 0x0800;

    private final static int PTRFLAGS_WHEEL = 0x0200;
    private final static int PTRFLAGS_WHEEL_NEGATIVE = 0x0100;

    public static int getLeftButtonEvent(Context context, boolean down) {
        if (ApplicationSettingsActivity.getSwapMouseButtons(context)) {
            return (PTRFLAGS_RBUTTON | (down ? PTRFLAGS_DOWN : 0));
        } else {
            return (PTRFLAGS_LBUTTON | (down ? PTRFLAGS_DOWN : 0));
        }
    }

    public static int getRightButtonEvent(Context context, boolean down) {
        if (ApplicationSettingsActivity.getSwapMouseButtons(context)) {
            return (PTRFLAGS_LBUTTON | (down ? PTRFLAGS_DOWN : 0));
        } else {
            return (PTRFLAGS_RBUTTON | (down ? PTRFLAGS_DOWN : 0));
        }
    }

    public static int getMoveEvent() {
        return PTRFLAGS_MOVE;
    }

    public static int getScrollEvent(Context context, boolean down) {
        int flags = PTRFLAGS_WHEEL;

        // invert scrolling?
        if (ApplicationSettingsActivity.getInvertScrolling(context))
            down = !down;

        if (down) {
            flags |= (PTRFLAGS_WHEEL_NEGATIVE | 0x0088);
        } else {
            flags |= 0x0078;
        }
        return flags;
    }
}
