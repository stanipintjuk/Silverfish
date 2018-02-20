package com.launcher.silverfish.dbmodel;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

@Entity(indexes = {
        @Index(value = "activityName", unique = true)
})
public class AppTable {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String packageName;
    @NotNull
    private String activityName;


    @NotNull
    private Long tabId;

    @Generated(hash = 1498711068)
    public AppTable(Long id, @NotNull String packageName, @NotNull String activityName, @NotNull Long tabId) {
        this.id = id;
        this.packageName = packageName;
        this.activityName = activityName;
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

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
}
