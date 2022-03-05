package com.xiaoyv.business.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.blankj.utilcode.util.TimeUtils
import com.xiaoyv.business.room.database.DateBaseConfig
import java.io.Serializable

/**
 * RdpEntity
 *
 * @author why
 * @since 2020/11/29
 */
@Entity(tableName = DateBaseConfig.TABLE_FTP, indices = [Index(value = ["ip"], unique = true)])
class FtpEntity : Serializable {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @JvmField
    var lastTime: Long = TimeUtils.getNowMills()

    @JvmField
    var label: String = ""

    @JvmField
    var group: String = ""

    @JvmField
    var ip: String = ""

    @JvmField
    var port: String = ""

    @JvmField
    var account: String = ""

    @JvmField
    var password: String = ""

    @JvmField
    var domain: String = ""

    @JvmField
    var setting: String = ""
}