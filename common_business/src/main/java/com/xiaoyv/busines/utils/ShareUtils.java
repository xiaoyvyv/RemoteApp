package com.xiaoyv.busines.utils;

import android.content.Intent;
import android.net.Uri;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.UriUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ShareUtils
 *
 * @author why
 * @since 2020/12/12
 **/
public class ShareUtils {
    public static final String ASSETS_COPY_TEMP = PathUtils.getExternalAppCachePath() + File.separator + "assets";

    private ShareUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * 分享Assets目录文件
     *
     * @param assetsPath 路径
     */
    public static void shareAssets(String assetsPath) {
        File file = new File(ASSETS_COPY_TEMP + File.separator + FileUtils.getFileName(assetsPath));
        if (FileUtils.isFileExists(file)) {
            shareFile(file);
            return;
        }
        boolean b = ResourceUtils.copyFileFromAssets(assetsPath, file.getAbsolutePath());
        if (b) {
            shareFile(file);
        }
    }

    /**
     * 分享文件
     *
     * @param filePath 文件路径
     */
    public static void shareFile(String... filePath) {
        File[] files = new File[filePath.length];
        for (int i = 0; i < filePath.length; i++) {
            files[i] = new File(filePath[i]);
        }
        shareFile(files);
    }

    /**
     * 分享文件
     *
     * @param file 文件
     */
    public static void shareFile(File... file) {
        Intent intent = shareFile(ArrayUtils.asArrayList(file));
        ActivityUtils.startActivity(intent);
    }

    /**
     * 分享文件
     *
     * @param files 文件
     */
    public static Intent shareFile(List<File> files) {
        ArrayList<Uri> uris = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                Uri uri = UriUtils.file2Uri(file);
                if (uri != null) {
                    uris.add(uri);
                }
            }
        }
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_TEXT, "Share");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.setType("*/*");
        intent = Intent.createChooser(intent, "");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
