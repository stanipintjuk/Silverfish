package com.launcher.silverfish;

import com.launcher.silverfish.dbmodel.TabTable;

/**
 * Created by Stanislav Pintjuk on 8/12/16.
 * E-mail: stanislav.pintjuk@gmail.com
 */
public class TabInfo {
    private int id;
    private String tag;
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
}
