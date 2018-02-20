package com.launcher.silverfish.dbmodel;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

@Entity(indexes = {
        @Index(value = "activityName", unique = true)
})
public class ShortcutTable {
    @Id(autoincrement = true)
    private Long id;
    @NotNull
    private String packageName;
    @NotNull
    private String activityName;


    @Generated(hash = 336387105)
    public ShortcutTable(Long id, @NotNull String packageName, @NotNull String activityName) {
        this.id = id;
        this.packageName = packageName;
        this.activityName = activityName;
    }

    @Generated(hash = 2116092840)
    public ShortcutTable() {
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

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
}
