package com.xiaoyv.busines.room.dao

import androidx.room.*
import com.xiaoyv.busines.room.entity.FtpEntity

/**
 * ftpDao
 *
 * @author why
 * @since 2020/11/29
 */
@Dao
interface FtpDao {
    @Query("SELECT * FROM ftp")
    fun queryAll(): List<FtpEntity>

    @Query("SELECT * FROM ftp WHERE id =:id LIMIT 1")
    fun queryById(id: Long): FtpEntity?

    @Query("SELECT * FROM ftp WHERE ip IN (:ip)")
    fun queryByIps(vararg ip: Int): List<FtpEntity>

    @Query("SELECT * FROM ftp WHERE label LIKE :label")
    fun queryLabel(label: String): List<FtpEntity>

    @Query("SELECT DISTINCT `group` FROM ftp ORDER BY `group` COLLATE LOCALIZED ASC")
    fun queryGroup(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg ftpEntities: FtpEntity)

    @Update
    fun update(vararg ftpEntities: FtpEntity)

    @Delete
    fun delete(vararg FtpEntity: FtpEntity)
}