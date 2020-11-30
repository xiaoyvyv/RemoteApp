package com.xiaoyv.busines.room.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.xiaoyv.busines.room.database.DateBaseConfig;

import java.io.Serializable;

/**
 * RdpEntity
 *
 * @author why
 * @since 2020/11/29
 **/
@Entity(tableName = DateBaseConfig.TABLE_SSH, indices = {@Index(value = {"ip"}, unique = true)})
public class SshEntity implements Serializable {
    private static final long serialVersionUID = 8962195813711670211L;
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String label;
    public String group;
    public String ip;
    public String port;
    public String account;
    public String password;
    public String domain;
    public String setting;
}
