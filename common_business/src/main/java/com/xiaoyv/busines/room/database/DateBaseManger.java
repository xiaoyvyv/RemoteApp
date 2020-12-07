package com.xiaoyv.busines.room.database;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.xiaoyv.busines.room.dao.FtpDao;
import com.xiaoyv.busines.room.dao.RdpDao;
import com.xiaoyv.busines.room.dao.SshDao;
import com.xiaoyv.busines.room.entity.RdpEntity;
import com.xiaoyv.busines.room.entity.SshEntity;

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
        LogUtils.json(rdpEntity);
        if (rdpEntity.id < 0) {
            getRdpDao().insert(rdpEntity);
            return;
        }
        RdpEntity entity = getRdpDao().getById(rdpEntity.id);
        if (entity == null) {
            getRdpDao().insert(rdpEntity);
            return;
        }
        getRdpDao().update(entity);
    }

    public SshDao getSshDao() {
        return sshDao;
    }


    /**
     * 保存或更新 Rdp
     *
     * @param sshEntity rdp 信息
     */
    public void saveSsh(@NonNull SshEntity sshEntity) {
        LogUtils.json(sshEntity);
        if (sshEntity.id < 0) {
            getSshDao().insert(sshEntity);
            return;
        }
        SshEntity entity = getSshDao().getById(sshEntity.id);
        if (entity == null) {
            getSshDao().insert(sshEntity);
            return;
        }
        getSshDao().update(entity);
    }


    public FtpDao getFtpDao() {
        return ftpDao;
    }
}
