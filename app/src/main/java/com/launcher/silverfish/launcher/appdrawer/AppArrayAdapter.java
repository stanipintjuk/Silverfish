package com.launcher.silverfish.launcher.appdrawer;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.launcher.silverfish.R;
import com.launcher.silverfish.common.Constants;
import com.launcher.silverfish.common.Utils;
import com.launcher.silverfish.models.AppDetail;
import com.launcher.silverfish.shared.Settings;

import java.util.List;

/**
 * Created by stani on 2016-12-15.
 */

public class AppArrayAdapter extends ArrayAdapter<AppDetail> {
    private final Activity mActivity;
    private final Settings mSettings;
    private final PackageManager mPackageManager;
    private final String mTag;

    public AppArrayAdapter(Activity activity, int resource, List<AppDetail> appsList, String tag) {
        super(activity, resource, appsList);
        mActivity = activity;
        mSettings = new Settings(activity);
        mPackageManager = activity.getPackageManager();
        mTag = tag;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final AppDetail app = getItem(position);
        final View view;
        final ViewHolder viewHolder;
        if (convertView == null) {
            view = mActivity.getLayoutInflater().inflate(R.layout.list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.appIcon = (ImageView) view.findViewById(R.id.item_app_icon);
            viewHolder.appLabel = (TextView) view.findViewById(R.id.item_app_label);
            viewHolder.appLabel.setTextColor(mSettings.getFontFgColor());
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        // load the app icon in an async task
        Utils.loadAppIconAsync(mPackageManager, app.packageName.toString(), viewHolder.appIcon);

        //final TextView appLabel = (TextView) view.findViewById(R.id.item_app_label);
        viewHolder.appLabel.setText(app.label);

        // Start a drag action when icon is long clicked
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                // Add data to the clipboard
                String[] mime_type = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData data = new ClipData(Constants.DRAG_APP_MOVE, mime_type, new ClipData.Item(app.packageName.toString()));
                data.addItem(new ClipData.Item(Integer.toString(position)));
                data.addItem(new ClipData.Item(mTag));

                // The drag shadow is simply the app's  icon
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view.findViewById(R.id.item_app_icon));

                // "This method was deprecated in API level 24. Use startDragAndDrop()
                // for newer platform versions."
                if (Build.VERSION.SDK_INT < 24) {
                    view.startDrag(data, shadowBuilder, view, 0);
                } else {
                    view.startDragAndDrop(data, shadowBuilder, view, 0);
                }
                return true;

            }
        });

        // Start the app activity when icon is clicked.
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = mPackageManager.getLaunchIntentForPackage(app.packageName.toString());
                mActivity.startActivity(i);
            }
        });

        return view;
    }

    private class ViewHolder {
        ImageView appIcon;
        TextView appLabel;
    }
}
