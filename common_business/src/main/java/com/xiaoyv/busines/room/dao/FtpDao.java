package com.xiaoyv.busines.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.xiaoyv.busines.room.entity.FtpEntity;

import java.util.List;

/**
 * ftpDao
 *
 * @author why
 * @since 2020/11/29
 **/
@Dao
public interface FtpDao {
    @Query("SELECT * FROM ftp")
    List<FtpEntity> getAll();

    @Query("SELECT * FROM ftp WHERE ip IN (:ip)")
    List<FtpEntity> loadAllByIps(int... ip);

    @Query("SELECT * FROM ftp WHERE label LIKE :label")
    List<FtpEntity> findAllByLabel(String label);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(FtpEntity... ftpEntities);

    @Update
    void update(FtpEntity... ftpEntities);

    @Delete
    void delete(FtpEntity... FtpEntity);
}
