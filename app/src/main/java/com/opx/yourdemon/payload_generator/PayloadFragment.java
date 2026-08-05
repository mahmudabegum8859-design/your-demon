package com.opx.yourdemon.payload_generator;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.opx.yourdemon.R;
import com.opx.yourdemon.payload_generator.utils.PayloadRunner;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;

public class PayloadFragment extends Fragment {

    public MaterialButton generate;
    public Core core;
    public Context context;
    public Activity activity;

    public PayloadFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payload_fragment, container, false);
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

        TextInputEditText lhostInput = view.findViewById(R.id.lhost_input);
        TextInputEditText lportInput = view.findViewById(R.id.lport_input);
        TextInputEditText filenameInput = view.findViewById(R.id.filename_input);
        Spinner payloadSpinner = view.findViewById(R.id.payload_spinner);
        Spinner formatSpinner = view.findViewById(R.id.format_spinner);
        TextView statusOutput = view.findViewById(R.id.payload_status);
        generate = view.findViewById(R.id.generate_payload);

        String[] payloadTypes = {
                "windows/meterpreter/reverse_tcp",
                "linux/x86/meterpreter/reverse_tcp",
                "android/meterpreter/reverse_tcp",
                "linux/x64/meterpreter/reverse_tcp",
                "osx/x64/meterpreter/reverse_tcp",
                "python/meterpreter/reverse_tcp"
        };

        String[] outputFormats = {
                "exe", "elf", "apk", "py", "php", "jar", "aspx", "dll"
        };

        ArrayAdapter<String> payloadAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, payloadTypes);
        payloadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payloadSpinner.setAdapter(payloadAdapter);

        ArrayAdapter<String> formatAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, outputFormats);
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(formatAdapter);

        generate.setOnClickListener(view1 -> {
            String lhost = String.valueOf(lhostInput.getText());
            String lport = String.valueOf(lportInput.getText());
            String payload = payloadSpinner.getSelectedItem().toString();
            String format = formatSpinner.getSelectedItem().toString();
            String filename = String.valueOf(filenameInput.getText());

            if (lhost.isEmpty() || lport.isEmpty()) {
                core.toaster(core.str("please_fill"));
                return;
            }

            final String fFilename = filename.isEmpty() ? "payload" : filename;

            statusOutput.setText(core.str("generating_payload"));
            new Thread(() -> {
                try {
                    ArrayList<String> result = PayloadRunner.execute(payload, lhost, lport, format, fFilename, core);
                    activity.runOnUiThread(() -> {
                        StringBuilder sb = new StringBuilder();
                        for (String line : result) {
                            sb.append(line).append("\n");
                        }
                        statusOutput.setText(sb.toString());
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        return view;
    }
}
