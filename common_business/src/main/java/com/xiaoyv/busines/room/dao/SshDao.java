package com.xiaoyv.busines.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.xiaoyv.busines.room.entity.SshEntity;

import java.util.List;

/**
 * sshDao
 *
 * @author why
 * @since 2020/11/29
 **/
@Dao
public interface SshDao {
    @Query("SELECT * FROM ssh")
    List<SshEntity> getAll();

    @Query("SELECT * FROM ssh WHERE id =:id LIMIT 1")
    SshEntity getById(long id);

    @Query("SELECT * FROM ssh WHERE ip IN (:ip)")
    List<SshEntity> loadAllByIps(int... ip);

    @Query("SELECT * FROM ssh WHERE label LIKE :label")
    List<SshEntity> findAllByLabel(String label);

    @Query("SELECT * FROM ssh WHERE `group` IN (:group)")
    List<SshEntity> getAllByGroup(String... group);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SshEntity... sshEntities);

    @Update
    void update(SshEntity... sshEntities);

    @Delete
    void delete(SshEntity... SshEntity);
}
