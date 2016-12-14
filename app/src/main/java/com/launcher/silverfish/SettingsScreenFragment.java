package com.launcher.silverfish;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class SettingsScreenFragment extends Fragment  {

    //region Interfaces

    public interface SettingChanged {
        void onWidgetVisibilityChanged(boolean visible);
        void onWidgetChangeRequested();
    }

    //endregion

    //region Fields

    private Settings settings;
    private SettingChanged callback;

    //endregion

    //region Android lifecycle

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_settings, container, false);

        // Toggle widget visibility button
        rootView.findViewById(R.id.toggle_widget_visibility_button)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleWidgetVisibility();
            }
        });

        // Change widget button
        rootView.findViewById(R.id.change_widget_button)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeWidget();
            }
        });

        // Change wallpaper button
        rootView.findViewById(R.id.change_wallpaper_button)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeWallpaper();
            }
        });

        return rootView;
    }

    //endregion

    //region Actions

    private void toggleWidgetVisibility() {
        boolean visible = !settings.isWidgetVisible();
        settings.setWidgetVisible(visible);
        if (visible) {
            Toast.makeText(getContext(), R.string.hint_widget_now_visible, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.hint_widget_now_invisible, Toast.LENGTH_SHORT).show();
        }
        callback.onWidgetVisibilityChanged(visible);
    }

    private void changeWidget() {
        if (!settings.isWidgetVisible())
            toggleWidgetVisibility();

            callback.onWidgetChangeRequested();
    }

    private void changeWallpaper() {
        Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
        startActivity(Intent.createChooser(intent, getString(R.string.setting_select_wallpaper)));
    }

    //endregion

    //region Fragment communication

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Ensure the parent activity implements SettingChanged
        try {
            callback = (SettingChanged)getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement SettingChanged");
        }
    }

    @Override
    public void onDetach() {
        callback = null;
        super.onDetach();
    }

    //endregion
}
