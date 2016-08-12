package com.launcher.silverfish;

import com.launcher.silverfish.dbmodel.TabTable;

/**
 * Created by Stanislav Pintjuk on 8/12/16.
 * E-mail: stanislav.pintjuk@gmail.com
 */
public interface TabButtonClickListener {
    void onClick(TabInfo tab, int position);
    boolean onLongClick(TabInfo tab, int position);
}
