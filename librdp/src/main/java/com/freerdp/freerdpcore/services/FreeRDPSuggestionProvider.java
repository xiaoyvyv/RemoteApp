package com.freerdp.freerdpcore.services;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.freerdp.freerdpcore.application.RdpApp;
import com.freerdp.freerdpcore.domain.BaseRdpBookmark;
import com.freerdp.freerdpcore.domain.ConnectionReference;
import com.freerdp.freerdpcore.domain.RdpBookmark;
import com.xiaoyv.librdp.R;

import java.util.ArrayList;

public class FreeRDPSuggestionProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://com.xiaoyv.rdp.services.provider");

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.item/vnd.freerdp.remote";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        String query = (selectionArgs != null && selectionArgs.length > 0) ? selectionArgs[0] : "";

        // 搜索历史记录
        ArrayList<BaseRdpBookmark> history =
                RdpApp.getQuickConnectHistoryGateway().findHistory(query);

        // 搜索书签
        ArrayList<BaseRdpBookmark> manualBookmarks;
        if (query.length() > 0)
            manualBookmarks = RdpApp.getManualBookmarkGateway().findByLabelOrHostnameLike(query);
        else
            manualBookmarks = RdpApp.getManualBookmarkGateway().findAll();

        return createResultCursor(history, manualBookmarks);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private void addBookmarksToCursor(ArrayList<BaseRdpBookmark> bookmarks, MatrixCursor resultCursor) {
        Object[] row = new Object[5];
        for (BaseRdpBookmark bookmark : bookmarks) {
            row[0] = bookmark.getId();
            row[1] = bookmark.getLabel();
            row[2] = bookmark.<RdpBookmark>get().getHostname();
            row[3] = ConnectionReference.getManualBookmarkReference(bookmark.getId());
            row[4] = "android.resource://" + getContext().getPackageName() + "/" + R.drawable.icon_star_on;
            resultCursor.addRow(row);
        }
    }

    private void addHistoryToCursor(ArrayList<BaseRdpBookmark> history, MatrixCursor resultCursor) {
        Object[] row = new Object[5];
        for (BaseRdpBookmark bookmark : history) {
            row[0] = 1;
            row[1] = bookmark.getLabel();
            row[2] = bookmark.getLabel();
            row[3] = ConnectionReference.getHostnameReference(bookmark.getLabel());
            row[4] = "android.resource://" + getContext().getPackageName() + "/" + R.drawable.icon_star_off;
            resultCursor.addRow(row);
        }
    }

    private Cursor createResultCursor(ArrayList<BaseRdpBookmark> history,
                                      ArrayList<BaseRdpBookmark> manualBookmarks) {

        // 创建结果矩阵游标
        int totalCount = history.size() + manualBookmarks.size();
        String[] columns = {android.provider.BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                SearchManager.SUGGEST_COLUMN_ICON_2};
        MatrixCursor matrixCursor = new MatrixCursor(columns, totalCount);

        // 填充结果矩阵
        if (totalCount > 0) {
            addHistoryToCursor(history, matrixCursor);
            addBookmarksToCursor(manualBookmarks, matrixCursor);
        }
        return matrixCursor;
    }
}
