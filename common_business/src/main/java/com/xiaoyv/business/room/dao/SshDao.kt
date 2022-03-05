package com.xiaoyv.business.room.dao

import androidx.room.*
import com.xiaoyv.business.room.entity.SshEntity

/**
 * sshDao
 *
 * @author why
 * @since 2020/11/29
 */
@Dao
interface SshDao {
    @Query("SELECT * FROM ssh")
    fun queryAll(): List<SshEntity>

    @Query("SELECT * FROM ssh WHERE id =:id LIMIT 1")
    fun queryById(id: Long): SshEntity?

    @Query("SELECT * FROM ssh WHERE ip IN (:ip)")
    fun queryByIps(vararg ip: Int): List<SshEntity>

    @Query("SELECT * FROM ssh WHERE label LIKE :label")
    fun queryLabel(label: String): List<SshEntity>

    @Query("SELECT * FROM ssh WHERE `group` IN (:group)")
    fun queryByGroup(vararg group: String): List<SshEntity>

    @Query("SELECT DISTINCT `group` FROM ssh ORDER BY `group` COLLATE LOCALIZED ASC")
    fun queryGroup(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg sshEntities: SshEntity)

    @Update
    fun update(vararg sshEntities: SshEntity)

    @Delete
    fun delete(vararg SshEntity: SshEntity)
}