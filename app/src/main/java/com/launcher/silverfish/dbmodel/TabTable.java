package com.launcher.silverfish.dbmodel;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {
        @Index(value = "label")
})
public class TabTable {
    @Id
    private Integer id;

    @NotNull
    private String label;

    @Generated(hash = 1452784914)
    public TabTable(Integer id, @NotNull String label) {
        this.id = id;
        this.label = label;
    }

    @Generated(hash = 1198679566)
    public TabTable() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
