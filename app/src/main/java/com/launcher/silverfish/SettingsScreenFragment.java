package com.launcher.silverfish;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class SettingsScreenFragment extends Fragment  {

    //region Interfaces

    public interface SettingChanged {
        void onWidgetVisibilityChanged(boolean visible);
        void onWidgetChangeRequested();
    }

    //endregion

    //region Fields

    private View rootView;
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

        rootView = inflater.inflate(R.layout.activity_settings, container, false);

        Button toggleWidgetVisibilityButton = (Button)
                rootView.findViewById(R.id.toggle_widget_visibility_button);

        toggleWidgetVisibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean visible = !settings.isWidgetVisible();
                settings.setWidgetVisible(visible);
                if (visible) {
                    Toast.makeText(getContext(), R.string.widget_now_visible, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.widget_now_invisible, Toast.LENGTH_SHORT).show();
                }
                callback.onWidgetVisibilityChanged(visible);
            }
        });

        Button changeWidgetButton = (Button)
                rootView.findViewById(R.id.change_widget_button);

        changeWidgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onWidgetChangeRequested();
            }
        });

        return rootView;
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
