package com.freerdp.freerdpcore.utils

object RdpMouse {
    private const val PTR_FLAGS_LEFT_BUTTON = 0x1000
    private const val PTR_FLAGS_RIGHT_BUTTON = 0x2000
    private const val PTR_FLAGS_DOWN = 0x8000
    private const val PTR_FLAGS_MOVE = 0x0800
    private const val PTR_FLAGS_WHEEL = 0x0200
    private const val PTR_FLAGS_WHEEL_NEGATIVE = 0x0100


    @JvmStatic
    fun getLeftButtonEvent(down: Boolean): Int {
        return PTR_FLAGS_LEFT_BUTTON or if (down) PTR_FLAGS_DOWN else 0
    }

    @JvmStatic
    fun getRightButtonEvent(down: Boolean): Int {
        return PTR_FLAGS_RIGHT_BUTTON or if (down) PTR_FLAGS_DOWN else 0
    }

    @JvmStatic
    fun getMoveEvent(): Int {
        return PTR_FLAGS_MOVE
    }

    @JvmStatic
    fun getScrollEvent(down: Boolean): Int {
        var flags = PTR_FLAGS_WHEEL
        flags = if (down) {
            flags or (PTR_FLAGS_WHEEL_NEGATIVE or 0x0088)
        } else {
            flags or 0x0078
        }
        return flags
    }
}