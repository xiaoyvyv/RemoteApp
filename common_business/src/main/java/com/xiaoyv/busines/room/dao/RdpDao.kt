package com.xiaoyv.busines.room.dao

import androidx.room.*
import com.xiaoyv.busines.room.entity.RdpEntity

/**
 * rdpDao
 *
 * @author why
 * @since 2020/11/29
 */
@Dao
interface RdpDao {
    @Query("SELECT * FROM rdp")
    fun queryAll(): List<RdpEntity>

    @Query("SELECT * FROM rdp WHERE id =:id LIMIT 1")
    fun queryById(id: Long): RdpEntity?

    @Query("SELECT * FROM rdp WHERE ip IN (:ip)")
    fun queryByIps(vararg ip: Int): List<RdpEntity>

    @Query("SELECT * FROM rdp WHERE label LIKE :label")
    fun queryByLabel(label: String): List<RdpEntity>

    @Query("SELECT * FROM rdp WHERE `group` IN (:group)")
    fun queryByGroup(vararg group: String): List<RdpEntity>

    @Query("SELECT DISTINCT `group` FROM rdp ORDER BY `group` COLLATE LOCALIZED ASC")
    fun queryGroup(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg rdpEntities: RdpEntity)

    @Update
    fun update(vararg rdpEntities: RdpEntity)

    @Delete
    fun delete(vararg rdpEntity: RdpEntity)
}