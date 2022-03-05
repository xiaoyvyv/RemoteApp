package com.xiaoyv.business.room.database;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.xiaoyv.business.room.dao.FtpDao;
import com.xiaoyv.business.room.dao.RdpDao;
import com.xiaoyv.business.room.dao.SshDao;
import com.xiaoyv.business.room.entity.FtpEntity;
import com.xiaoyv.business.room.entity.RdpEntity;
import com.xiaoyv.business.room.entity.SshEntity;

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

    /**
     * 保存或更新 Rdp
     *
     * @param rdpEntity rdp 信息
     */
    public void saveRdp(@NonNull RdpEntity rdpEntity) {
        if (rdpEntity.id < 0) {
            getRdpDao().insert(rdpEntity);
            return;
        }

        RdpEntity entity = getRdpDao().queryById(rdpEntity.id);
        if (entity == null) {
            getRdpDao().insert(rdpEntity);
            return;
        }
        getRdpDao().update(rdpEntity);
    }

    public SshDao getSshDao() {
        return sshDao;
    }

    public FtpDao getFtpDao() {
        return ftpDao;
    }

    /**
     * 保存或更新 ssh
     *
     * @param sshEntity ssh 信息
     */
    public void saveSsh(@NonNull SshEntity sshEntity) {
        LogUtils.json(sshEntity);
        if (sshEntity.id < 0) {
            getSshDao().insert(sshEntity);
            return;
        }
        SshEntity entity = getSshDao().queryById(sshEntity.id);
        if (entity == null) {
            getSshDao().insert(sshEntity);
            return;
        }
        getSshDao().update(sshEntity);
    }


    /**
     * 保存或更新 ftp
     *
     * @param ftpEntity ftp 信息
     */
    public void saveFtp(@NonNull FtpEntity ftpEntity) {
        LogUtils.json(ftpEntity);
        if (ftpEntity.id < 0) {
            getFtpDao().insert(ftpEntity);
            return;
        }
        FtpEntity entity = getFtpDao().queryById(ftpEntity.id);
        if (entity == null) {
            getFtpDao().insert(ftpEntity);
            return;
        }
        getFtpDao().update(ftpEntity);
    }


}
