package com.xiaoyv.business.room.database;

/**
 * RoomConfig
 *
 * @author why
 * @since 2020/11/29
 **/
public class DateBaseConfig {
    /**
     * 数据库版本号
     */
    public static final int VERSION = 1;
    /**
     * 数据库名称
     */
    public static final String NAME = "remote_db";

    /**
     * 各个表名
     */
    public static final String TABLE_RDP = "rdp";
    public static final String TABLE_SSH = "ssh";
    public static final String TABLE_FTP = "ftp";
}
