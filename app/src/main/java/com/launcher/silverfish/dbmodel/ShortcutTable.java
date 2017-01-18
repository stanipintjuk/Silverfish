package com.launcher.silverfish.dbmodel;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {
        @Index(value = "packageName", unique = true)
})
public class ShortcutTable {
    @Id
    private Integer id;

    @NotNull
    private String packageName;

    @Generated(hash = 1339096776)
    public ShortcutTable(Integer id, @NotNull String packageName) {
        this.id = id;
        this.packageName = packageName;
    }

    @Generated(hash = 2116092840)
    public ShortcutTable() {
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
}
