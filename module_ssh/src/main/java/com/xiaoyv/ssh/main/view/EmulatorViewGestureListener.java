package com.xiaoyv.ssh.main.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

import java.security.Key;
import java.util.List;

import jackpal.androidterm.emulatorview.EmulatorView;

public class EmulatorViewGestureListener extends GestureDetector.SimpleOnGestureListener {
    private EmulatorView view;

    public EmulatorViewGestureListener(EmulatorView view) {
        this.view = view;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // Let the EmulatorView handle taps if mouse tracking is active
        if (view.isMouseTrackingActive()) return false;

        //Check for link at tap location
        String link = view.getURLat(e.getX(), e.getY());
        if (link != null)
            execURL(link);
        else
            doUIToggle((int) e.getX(), (int) e.getY(), view.getVisibleWidth(), view.getVisibleHeight());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float absVelocityX = Math.abs(velocityX);
        float absVelocityY = Math.abs(velocityY);
        if (absVelocityX > Math.max(1000.0f, 2.0 * absVelocityY)) {
            // Assume user wanted side to side movement
            if (velocityX > 0) {
                // Left to right swipe -- previous window
                //mViewFlipper.showPrevious();
                LogUtils.e("mViewFlipper.showPrevious();");
            } else {
                // Right to left swipe -- next window
                //mViewFlipper.showNext();
                LogUtils.e("mViewFlipper.showNext();");
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Send a URL up to Android to be handled by a browser.
     *
     * @param link The URL to be opened.
     */
    private void execURL(String link) {
        Uri webLink = Uri.parse(link);
        Intent openLink = new Intent(Intent.ACTION_VIEW, webLink);
        PackageManager pm = Utils.getApp().getPackageManager();
        List<ResolveInfo> handlers = pm.queryIntentActivities(openLink, 0);
        if (handlers.size() > 0)
            ActivityUtils.startActivity(openLink);
    }


    private void doUIToggle(int x, int y, int width, int height) {
        KeyboardUtils.toggleSoftInput();
        view.requestFocus();
    }
}