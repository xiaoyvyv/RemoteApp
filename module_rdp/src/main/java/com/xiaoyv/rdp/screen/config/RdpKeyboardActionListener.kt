package com.xiaoyv.rdp.screen.config

import com.hijamoya.keyboardview.KeyboardView

/**
 * OnKeyboardActionListener
 *
 * @author why
 * @since 2021/07/10
 **/
interface RdpKeyboardActionListener : KeyboardView.OnKeyboardActionListener {
    override fun onPress(p0: Int) {

    }

    override fun onRelease(p0: Int) {
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
    }

    override fun onText(p0: CharSequence) {
    }

    override fun swipeLeft() {

    }

    override fun swipeRight() {}

    override fun swipeDown() {}

    override fun swipeUp() {
    }
}