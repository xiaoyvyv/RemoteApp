package com.xiaoyv.busines.utils;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.xiaoyv.ui.listener.SimpleResultListener;

import java.io.File;
import java.io.InputStream;

/**
 * SelectUtils
 *
 * @author why
 * @since 2020/12/09
 **/
public class SelectUtils {
    public static final String SELECT_SAVE_PATH = PathUtils.getExternalAppCachePath() + "/select";

    public static void copySelectFile(@NonNull Uri uri, @NonNull SimpleResultListener<File> listener) {
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<File>() {
            @Override
            public File doInBackground() throws Throwable{
                InputStream inputStream = Utils.getApp().getContentResolver().openInputStream(uri);
                String fileName = FileUtils.getFileName(uri.getPath());
                String newFile = SELECT_SAVE_PATH + "/" + fileName;
                FileUtils.createFileByDeleteOldFile(newFile);
                FileIOUtils.writeFileFromIS(newFile, inputStream);
                return new File(newFile);
            }

            @Override
            public void onSuccess(File result) {
                listener.onResult(result);
            }
        });
    }
}
