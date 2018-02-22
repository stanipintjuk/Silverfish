package com.launcher.silverfish.launcher.appdrawer;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.launcher.silverfish.R;
import com.launcher.silverfish.common.Constants;
import com.launcher.silverfish.common.Utils;
import com.launcher.silverfish.models.AppDetail;
import com.launcher.silverfish.shared.Settings;

import java.net.URISyntaxException;
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

                // Add data to the clipboard ...
                // Current ClipData.Item usage:
                //     0: package eg: 'com.launcher.silverfish'
                //     1: ArrayAdapter position eg: 0
                //     2: Fragment tag eg: "1" to represent tab 'OTHER'
                //     3: Application label eg: 'Silverfish'                10Feb2018
                // CAUTION: This package uses hard-coded offsets to reference ClipData items. Fixme!
                String[] mime_type = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData data = new ClipData(Constants.DRAG_APP_MOVE, mime_type, new ClipData.Item(app.packageName.toString()));
                data.addItem(new ClipData.Item(app.activityName.toString()));
                data.addItem(new ClipData.Item(Integer.toString(position)));
                data.addItem(new ClipData.Item(mTag));
                data.addItem(new ClipData.Item(app.label.toString()));

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
        final Context ctx = getContext();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                if (app.intentUri != null) {
                    try {
                        i = Intent.parseUri(app.intentUri.toString(), Intent.URI_INTENT_SCHEME);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else {
                    i.setAction(Intent.ACTION_MAIN);
                    i.setComponent(new ComponentName(app.packageName.toString(), app.activityName.toString()));
                }
                if (i != null) {
                    // Sanity check (application may have been uninstalled)
                    // TODO Remove it from the database
                    mActivity.startActivity(i);
                } else {
                    Toast.makeText(ctx, R.string.application_not_installed, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private class ViewHolder {
        ImageView appIcon;
        TextView appLabel;
    }
}
