package com.launcher.silverfish;

/**
 * Created by Stanislav Pintjuk on 7/19/16.
 * E-mail: stanislav.pintjuk@gmail.com
 */
public class ShortcutDetail {
    public long id;
    public String name;

    @Override
    public boolean equals(Object o){
        if (o == null){
            return false;

        }else if (o instanceof ShortcutDetail){
            ShortcutDetail s = (ShortcutDetail) o;
            return s.id == this.id;

        }else{
            return false;
        }
    }
}
