package com.launcher.silverfish;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class SettingsScreenFragment extends Fragment  {

    //region Fields

    private View rootView;
    private Settings settings;

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
                restartActivity();
            }
        });

        return rootView;
    }

    //endregion

    //region Settings utilities

    // Some settings require the activity to restart to take effect
    void restartActivity() {
        Intent intent = getActivity().getIntent();
        getActivity().finish();
        intent.putExtra(LauncherActivity.START_PAGE, 2); // 2 = Settings screen
        startActivity(intent);
    }

    //endregion
}
