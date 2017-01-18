package com.launcher.silverfish.dbmodel;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {
        @Index(value = "packageName", unique = true)
})
public class AppTable {
    @Id
    private Integer id;

    @NotNull
    private String packageName;

    @NotNull
    private Integer tabId;

    @Generated(hash = 178348991)
    public AppTable(Integer id, @NotNull String packageName,
            @NotNull Integer tabId) {
        this.id = id;
        this.packageName = packageName;
        this.tabId = tabId;
    }

    @Generated(hash = 639376780)
    public AppTable() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Integer getTabId() {
        return this.tabId;
    }

    public void setTabId(Integer tabId) {
        this.tabId = tabId;
    }
}
