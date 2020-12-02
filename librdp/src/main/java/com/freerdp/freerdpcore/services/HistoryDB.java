package com.freerdp.freerdpcore.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDB extends SQLiteOpenHelper {

    public static final String QUICK_CONNECT_TABLE_NAME = "quick_connect_history";
    public static final String QUICK_CONNECT_TABLE_COL_ITEM = "item";
    public static final String QUICK_CONNECT_TABLE_COL_TIMESTAMP = "timestamp";
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "history.db";

    public HistoryDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sqlQuickConnectHistory = "CREATE TABLE " + QUICK_CONNECT_TABLE_NAME + " (" +
                QUICK_CONNECT_TABLE_COL_ITEM + " TEXT PRIMARY KEY, " +
                QUICK_CONNECT_TABLE_COL_TIMESTAMP + " INTEGER"
                + ");";

        db.execSQL(sqlQuickConnectHistory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
