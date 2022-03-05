package com.xiaoyv.business.room.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.xiaoyv.business.room.dao.FtpDao;
import com.xiaoyv.business.room.dao.RdpDao;
import com.xiaoyv.business.room.dao.SshDao;
import com.xiaoyv.business.room.entity.FtpEntity;
import com.xiaoyv.business.room.entity.RdpEntity;
import com.xiaoyv.business.room.entity.SshEntity;

/**
 * DateBase
 *
 * @author why
 * @since 2020/11/29
 **/
@Database(entities = {
        RdpEntity.class,
        SshEntity.class,
        FtpEntity.class
}, version = DateBaseConfig.VERSION)
public abstract class DateBase extends RoomDatabase {
    public abstract RdpDao rdpDao();

    public abstract SshDao sshDao();

    public abstract FtpDao ftpDao();
}
