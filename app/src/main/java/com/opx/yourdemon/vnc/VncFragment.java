package com.opx.yourdemon.vnc;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.opx.yourdemon.R;
import com.opx.yourdemon.utils.CommandOutput;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomCommand;
import com.opx.yourdemon.utils.TaskRunner;

import java.util.ArrayList;

public class VncFragment extends Fragment {

    public Core core;
    public TextView statusText;
    public EditText ipInput, portInput;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vnc_viewer, container, false);
        core = new Core(getContext());

        statusText = view.findViewById(R.id.status_text);
        ipInput = view.findViewById(R.id.ip_input);
        portInput = view.findViewById(R.id.port_input);

        portInput.setText("5900");

        MaterialButton connectVnc = view.findViewById(R.id.connect_vnc);
        MaterialButton startVncServer = view.findViewById(R.id.start_vnc_server);

        connectVnc.setOnClickListener(v -> connectVnc());
        startVncServer.setOnClickListener(v -> startVncServer());

        return view;
    }

    private void connectVnc() {
        String ip = ipInput.getText().toString().trim();
        String port = portInput.getText().toString().trim();
        if (ip.isEmpty()) { core.toaster(core.str("please_fill")); return; }

        statusText.setText(core.str("connecting_vnc"));

        String ipFinal = ip;
        String portFinal = port;
        TaskRunner.execute(() -> {
            String result = "";
            try {
                String cmd = core.EXECUTE + "nmap -p " + portFinal + " " + ipFinal + " --open";
                ArrayList<String> list = CommandOutput.execute(cmd, core);
                if (list != null) {
                    boolean found = false;
                    for (String line : list) {
                        if (line.contains("open") || line.contains("vnc")) {
                            result = core.str("vnc_available") + " " + ipFinal + ":" + portFinal;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        result = core.str("vnc_not_available");
                    }
                } else {
                    result = core.str("vnc_not_available");
                }
            } catch (Exception e) {
                result = core.str("error") + ": " + e.getMessage();
            }
            String finalResult = result;
            getActivity().runOnUiThread(() -> statusText.setText(finalResult));
        });
    }

    private void startVncServer() {
        statusText.setText(core.str("starting_vnc_server"));
        TaskRunner.execute(() -> {
            boolean result = false;
            try {
                String display = ":1";
                String cmd = "export DISPLAY=" + display + "; " + core.EXECUTE + "vncserver " + display + " -geometry 1280x720 -depth 24";
                result = CustomCommand.execute(cmd, core);
            } catch (Exception ignored) {}
            boolean finalResult = result;
            getActivity().runOnUiThread(() -> {
                if (finalResult) {
                    statusText.setText(core.str("vnc_server_running"));
                    core.toaster(core.str("vnc_server_running"));
                } else {
                    statusText.setText(core.str("vnc_server_failed"));
                }
            });
        });
    }
}
