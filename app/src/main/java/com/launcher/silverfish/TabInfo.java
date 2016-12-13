package com.launcher.silverfish;

import com.launcher.silverfish.dbmodel.TabTable;

/**
 * Created by Stanislav Pintjuk on 8/12/16.
 * E-mail: stanislav.pintjuk@gmail.com
 */
public class TabInfo {
    private final int id;
    private final String tag;
    private String label;

    public TabInfo(TabTable tab){
        this.id = tab.id;
        this.label = tab.label;

        // let the tag simply be the id of the tab
        tag = Integer.toString(id);
    }

    public int getId(){
        return id;
    }

    public String getTag(){
        return tag;
    }

    public String getLabel(){
        return label;
    }

    public void rename(String new_name) {
        label = new_name;
    }
}
