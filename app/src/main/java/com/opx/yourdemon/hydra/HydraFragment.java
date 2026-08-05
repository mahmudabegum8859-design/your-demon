package com.opx.yourdemon.hydra;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.opx.yourdemon.R;
import com.opx.yourdemon.utils.CommandOutput;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;

public class HydraFragment extends Fragment {

    public ImageButton search;
    public Boolean now;
    public Core core;
    public Context context;
    public Activity activity;

    public HydraFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hydra_fragment, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);

        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        view.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() { core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });

        TextInputEditText targetInput = view.findViewById(R.id.hydra_target);
        Spinner protocolSpinner = view.findViewById(R.id.hydra_protocol);
        TextInputEditText portInput = view.findViewById(R.id.hydra_port);
        TextInputEditText usernameInput = view.findViewById(R.id.hydra_username);
        TextInputEditText passwordInput = view.findViewById(R.id.hydra_password);
        TextInputEditText threadsInput = view.findViewById(R.id.hydra_threads);
        ImageButton runButton = view.findViewById(R.id.hydra_run);
        TextView output = view.findViewById(R.id.hydra_output);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.hydra_protocols, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        protocolSpinner.setAdapter(adapter);

        runButton.setOnClickListener(v -> {
            String target = String.valueOf(targetInput.getText()).trim();
            String proto = protocolSpinner.getSelectedItem().toString();
            String port = String.valueOf(portInput.getText()).trim();
            String user = String.valueOf(usernameInput.getText()).trim();
            String pass = String.valueOf(passwordInput.getText()).trim();
            String threads = String.valueOf(threadsInput.getText()).trim();

            if (target.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                core.toaster(core.str("fill_all_fields"));
                return;
            }

            StringBuilder cmdBuilder = new StringBuilder();
            cmdBuilder.append("hydra -l ").append(user).append(" -p ").append(pass);
            if (!port.isEmpty()) {
                cmdBuilder.append(" -s ").append(port);
            }
            if (!threads.isEmpty()) {
                cmdBuilder.append(" -t ").append(threads);
            } else {
                cmdBuilder.append(" -t 4");
            }
            cmdBuilder.append(" ").append(target).append(" ").append(proto);

            String fullCmd = Core.EXECUTE + "'" + cmdBuilder.toString() + "'";
            output.setText("");
            new Thread(() -> {
                try {
                    ArrayList<String> result = CommandOutput.execute(fullCmd, core);
                    activity.runOnUiThread(() -> {
                        for (String line : result) {
                            output.append(line + "\n");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        return view;
    }
}
