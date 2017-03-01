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
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String packageName;

    @NotNull
    private Long tabId;

    @Generated(hash = 1161766823)
    public AppTable(Long id, @NotNull String packageName, @NotNull Long tabId) {
        this.id = id;
        this.packageName = packageName;
        this.tabId = tabId;
    }

    @Generated(hash = 639376780)
    public AppTable() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Long getTabId() {
        return this.tabId;
    }

    public void setTabId(Long tabId) {
        this.tabId = tabId;
    }
}
