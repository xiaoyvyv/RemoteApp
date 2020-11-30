package com.xiaoyv.busines.room.database;

import androidx.room.Room;

import com.blankj.utilcode.util.Utils;
import com.xiaoyv.busines.room.dao.FtpDao;
import com.xiaoyv.busines.room.dao.RdpDao;
import com.xiaoyv.busines.room.dao.SshDao;

/**
 * DateBaseUtils
 *
 * @author why
 * @since 2020/11/29
 **/
public class DateBaseManger {
    private static volatile DateBaseManger dateBaseManger;
    private final DateBase dateBase;
    private final RdpDao rdpDao;
    private final SshDao sshDao;
    private final FtpDao ftpDao;

    private DateBaseManger() {
        dateBase = Room.databaseBuilder(Utils.getApp(), DateBase.class, DateBaseConfig.NAME).build();
        rdpDao = dateBase.rdpDao();
        sshDao = dateBase.sshDao();
        ftpDao = dateBase.ftpDao();
    }

    public static DateBaseManger get() {
        if (dateBaseManger == null) {
            synchronized (DateBaseManger.class) {
                if (dateBaseManger == null) {
                    dateBaseManger = new DateBaseManger();
                }
            }
        }
        return dateBaseManger;
    }

    public DateBase getDateBase() {
        return dateBase;
    }

    public RdpDao getRdpDao() {
        return rdpDao;
    }

    public SshDao getSshDao() {
        return sshDao;
    }

    public FtpDao getFtpDao() {
        return ftpDao;
    }
}
