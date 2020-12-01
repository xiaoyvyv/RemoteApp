package com.xiaoyv.busines.utils;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;

/**
 * ScanUtils
 *
 * @author why
 * @since 2020/12/02
 **/
public class ScanUtils {

    public static class ScanIpTask extends ThreadUtils.SimpleTask<String> {


        @Override
        public String doInBackground() throws Throwable {
            if (!NetworkUtils.isWifiConnected()) {
                return null;
            }
            String wifi = NetworkUtils.getIpAddressByWifi();
            if (StringUtils.isEmpty(wifi)) {
                return null;
            }
            LogUtils.e(wifi);
            return null;
        }

        @Override
        public void onSuccess(String result) {

        }
    }

}
