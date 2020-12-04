package com.xiaoyv.busines.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.xiaoyv.busines.room.entity.RdpEntity;

import java.util.List;

/**
 * rdpDao
 *
 * @author why
 * @since 2020/11/29
 **/
@Dao
public interface RdpDao {
    @Query("SELECT * FROM rdp")
    List<RdpEntity> getAll();

    @Query("SELECT * FROM rdp WHERE id =:id LIMIT 1")
    RdpEntity getById(long id);

    @Query("SELECT * FROM rdp WHERE ip IN (:ip)")
    List<RdpEntity> getAllByIps(int... ip);

    @Query("SELECT * FROM rdp WHERE `group` IN (:group)")
    List<RdpEntity> getAllByGroup(String... group);

    @Query("SELECT * FROM rdp WHERE label LIKE :label")
    List<RdpEntity> findAllByLabel(String label);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RdpEntity... rdpEntities);

    @Update
    void update(RdpEntity... rdpEntities);

    @Delete
    void delete(RdpEntity... rdpEntity);
}
