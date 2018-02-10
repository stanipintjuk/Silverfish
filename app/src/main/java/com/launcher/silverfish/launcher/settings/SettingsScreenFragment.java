package com.launcher.silverfish.launcher.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.launcher.silverfish.R;
import com.launcher.silverfish.common.LG;
import com.launcher.silverfish.shared.Settings;

import yuku.ambilwarna.AmbilWarnaDialog;

import static com.launcher.silverfish.common.LG.lg;

public class SettingsScreenFragment extends Fragment  {

    //region Interfaces

    public interface SettingChanged {
        void onWidgetVisibilityChanged(boolean visible);
        void onWidgetChangeRequested();
        void onColorChanged(int drawerBg, int widgetBg, int fontFg);
    }

    //endregion

    //region Fields

    private Settings settings;
    private SettingChanged callback;

    //endregion

    //region Android lifecycle

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        lg("Method begins...");
        super.onCreate(savedInstanceState);

        settings = new Settings(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        lg("Method begins...");
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

        // Change colors, app drawer background
        rootView.findViewById(R.id.change_drawer_color)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeDrawerColor();
                    }
                });

        // Change colors, widget background
        rootView.findViewById(R.id.change_widget_color)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeWidgetColor();
                    }
                });

        // Change colors, font foreground
        rootView.findViewById(R.id.change_font_color)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeFontColor();
                    }
                });

        // Enable debug logging
        Button toggle_logging = (Button)rootView.findViewById(R.id.toggle_logging);
        syncLogging(toggle_logging);    // Set (dynamic) button text
        toggle_logging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleLogging((Button)view);
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

    private void changeDrawerColor() {
        new AmbilWarnaDialog(getContext(), settings.getDrawerBgColor(), true,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) { /* Do nothing */ }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        settings.setDrawerBgColor(color);
                        callback.onColorChanged(
                                color, settings.getWidgetBgColor(), settings.getFontFgColor());
                    }
                }).show();
    }

    private void changeWidgetColor() {
        new AmbilWarnaDialog(getContext(), settings.getWidgetBgColor(), true,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) { /* Do nothing */ }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        settings.setWidgetBgColor(color);
                        callback.onColorChanged(
                                settings.getDrawerBgColor(), color, settings.getFontFgColor());
                    }
                }).show();
    }

    private void changeFontColor() {
        new AmbilWarnaDialog(getContext(), settings.getFontFgColor(), false,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) { /* Do nothing */ }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        settings.setFontFgColor(color);
                        callback.onColorChanged(
                                settings.getDrawerBgColor(), settings.getWidgetBgColor(), color);
                    }
                }).show();
    }

    private void toggleLogging(Button btn) {
        LG.setDebug(!LG.isDebug());         // Toggle on/off
        syncLogging(btn);                   // Synchronise button text
    }
    private void syncLogging(Button btn) {
        btn.setText(LG.isDebug() ? R.string.text_debug_off : R.string.text_debug_on);
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
