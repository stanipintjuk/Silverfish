package com.launcher.silverfish.models;

/**
 * Created by Stanislav Pintjuk on 7/19/16.
 * E-mail: stanislav.pintjuk@gmail.com
 */
public class ShortcutDetail {

    //region Fields

    public long id;
    public String name;

    //endregion

    //region Overrides

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;

        } else if (o instanceof ShortcutDetail) {
            ShortcutDetail s = (ShortcutDetail) o;
            return s.id == this.id;

        } else {
            return false;
        }
    }

    //endregion
}
