package com.launcher.silverfish;

import com.launcher.silverfish.dbmodel.TabTable;

/**
 * Created by Stanislav Pintjuk on 8/12/16.
 * E-mail: stanislav.pintjuk@gmail.com
 */
public class TabInfo {
    public int id;
    public String tag;
    public String label;

    public TabInfo(TabTable tab){
        this.id = tab.id;
        this.label = tab.label;

        // let the tag simply be the id of the tab
        tag = Integer.toString(id);
    }

    public TabInfo(){}
}
