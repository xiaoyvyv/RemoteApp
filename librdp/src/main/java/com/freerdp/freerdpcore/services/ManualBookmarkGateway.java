package com.freerdp.freerdpcore.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import com.freerdp.freerdpcore.domain.BaseRdpBookmark;
import com.freerdp.freerdpcore.domain.RdpBookmark;

import java.util.ArrayList;

public class ManualBookmarkGateway extends BookmarkBaseGateway {

    public ManualBookmarkGateway(SQLiteOpenHelper bookmarkDB) {
        super(bookmarkDB);
    }

    @Override
    protected BaseRdpBookmark createBookmark() {
        return new RdpBookmark();
    }

    @Override
    protected String getBookmarkTableName() {
        return BookmarkDB.DB_TABLE_BOOKMARK;
    }

    @Override
    protected void addBookmarkSpecificColumns(BaseRdpBookmark bookmark, ContentValues columns) {
        RdpBookmark bm = (RdpBookmark) bookmark;
        columns.put(BookmarkDB.DB_KEY_BOOKMARK_HOSTNAME, bm.getHostname());
        columns.put(BookmarkDB.DB_KEY_BOOKMARK_PORT, bm.getPort());

        // gateway settings
        columns.put(BookmarkDB.DB_KEY_BOOKMARK_GW_ENABLE, bm.getEnableGatewaySettings());
        columns.put(BookmarkDB.DB_KEY_BOOKMARK_GW_HOSTNAME, bm.getGatewaySettings().getHostname());
        columns.put(BookmarkDB.DB_KEY_BOOKMARK_GW_PORT, bm.getGatewaySettings().getPort());
        columns.put(BookmarkDB.DB_KEY_BOOKMARK_GW_USERNAME, bm.getGatewaySettings().getUsername());
        columns.put(BookmarkDB.DB_KEY_BOOKMARK_GW_PASSWORD, bm.getGatewaySettings().getPassword());
        columns.put(BookmarkDB.DB_KEY_BOOKMARK_GW_DOMAIN, bm.getGatewaySettings().getDomain());
    }

    @Override
    protected void addBookmarkSpecificColumns(ArrayList<String> columns) {
        columns.add(BookmarkDB.DB_KEY_BOOKMARK_HOSTNAME);
        columns.add(BookmarkDB.DB_KEY_BOOKMARK_PORT);
        columns.add(BookmarkDB.DB_KEY_BOOKMARK_GW_ENABLE);
        columns.add(BookmarkDB.DB_KEY_BOOKMARK_GW_HOSTNAME);
        columns.add(BookmarkDB.DB_KEY_BOOKMARK_GW_PORT);
        columns.add(BookmarkDB.DB_KEY_BOOKMARK_GW_USERNAME);
        columns.add(BookmarkDB.DB_KEY_BOOKMARK_GW_PASSWORD);
        columns.add(BookmarkDB.DB_KEY_BOOKMARK_GW_DOMAIN);
    }

    @Override
    protected void readBookmarkSpecificColumns(BaseRdpBookmark bookmark, Cursor cursor) {
        RdpBookmark bm = (RdpBookmark) bookmark;
        bm.setHostname(
                cursor.getString(cursor.getColumnIndex(BookmarkDB.DB_KEY_BOOKMARK_HOSTNAME)));
        bm.setPort(cursor.getInt(cursor.getColumnIndex(BookmarkDB.DB_KEY_BOOKMARK_PORT)));

        bm.setEnableGatewaySettings(
                cursor.getInt(cursor.getColumnIndex(BookmarkDB.DB_KEY_BOOKMARK_GW_ENABLE)) != 0);
        readGatewaySettings(bm, cursor);
    }

    public BaseRdpBookmark findByLabelOrHostname(String pattern) {
        if (pattern.length() == 0)
            return null;

        Cursor cursor =
                queryBookmarks(BookmarkDB.DB_KEY_BOOKMARK_LABEL + " = '" + pattern + "' OR " +
                                BookmarkDB.DB_KEY_BOOKMARK_HOSTNAME + " = '" + pattern + "'",
                        BookmarkDB.DB_KEY_BOOKMARK_LABEL);
        BaseRdpBookmark bookmark = null;
        if (cursor.moveToFirst() && (cursor.getCount() > 0))
            bookmark = getBookmarkFromCursor(cursor);

        cursor.close();
        return bookmark;
    }

    public ArrayList<BaseRdpBookmark> findByLabelOrHostnameLike(String pattern) {
        Cursor cursor =
                queryBookmarks(BookmarkDB.DB_KEY_BOOKMARK_LABEL + " LIKE '%" + pattern + "%' OR " +
                                BookmarkDB.DB_KEY_BOOKMARK_HOSTNAME + " LIKE '%" + pattern + "%'",
                        BookmarkDB.DB_KEY_BOOKMARK_LABEL);
        ArrayList<BaseRdpBookmark> bookmarks = new ArrayList<BaseRdpBookmark>(cursor.getCount());

        if (cursor.moveToFirst() && (cursor.getCount() > 0)) {
            do {
                bookmarks.add(getBookmarkFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return bookmarks;
    }

    private void readGatewaySettings(RdpBookmark bookmark, Cursor cursor) {
        RdpBookmark.GatewaySettings gatewaySettings = bookmark.getGatewaySettings();
        gatewaySettings.setHostname(
                cursor.getString(cursor.getColumnIndex(BookmarkDB.DB_KEY_BOOKMARK_GW_HOSTNAME)));
        gatewaySettings.setPort(
                cursor.getInt(cursor.getColumnIndex(BookmarkDB.DB_KEY_BOOKMARK_GW_PORT)));
        gatewaySettings.setUsername(
                cursor.getString(cursor.getColumnIndex(BookmarkDB.DB_KEY_BOOKMARK_GW_USERNAME)));
        gatewaySettings.setPassword(
                cursor.getString(cursor.getColumnIndex(BookmarkDB.DB_KEY_BOOKMARK_GW_PASSWORD)));
        gatewaySettings.setDomain(
                cursor.getString(cursor.getColumnIndex(BookmarkDB.DB_KEY_BOOKMARK_GW_DOMAIN)));
    }
}
